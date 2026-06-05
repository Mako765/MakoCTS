package com.mako.cts

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass

object Cts {
    const val TAG = "MakoCTS"

    private const val GOOGLE_VIS =
        "com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService"
    private const val ATTR_TAG = "hyperOS_home"
    private const val PREFS = "cts_fix"
    private const val KEY_GOOD_VIS = "good_vis"
    private const val KEY_GOOD_ASSIST = "good_assistant"

    @Volatile private var exempted = false
    private fun ensureExempt() {
        if (!exempted) {
            runCatching { HiddenApiBypass.addHiddenApiExemptions("") }
            exempted = true
        }
    }

    fun hasWriteSecure(ctx: Context): Boolean =
        ctx.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
                PackageManager.PERMISSION_GRANTED

    fun isAssistantGoogle(ctx: Context): Boolean {
        val vis = Settings.Secure.getString(ctx.contentResolver, "voice_interaction_service")
        return vis?.contains("googlequicksearchbox") == true
    }

    fun captureIfGood(ctx: Context) {
        val vis = Settings.Secure.getString(ctx.contentResolver, "voice_interaction_service")
        if (!vis.isNullOrBlank()) {
            val assist = Settings.Secure.getString(ctx.contentResolver, "assistant") ?: ""
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_GOOD_VIS, vis)
                .putString(KEY_GOOD_ASSIST, assist)
                .apply()
        }
    }

    fun rebindAssistant(ctx: Context, offMs: Long, settleMs: Long): Boolean {
        val cr = ctx.contentResolver
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        captureIfGood(ctx)
        val goodVis = prefs.getString(KEY_GOOD_VIS, null) ?: GOOGLE_VIS
        val goodAssist = prefs.getString(KEY_GOOD_ASSIST, null).let {
            if (it.isNullOrBlank()) goodVis else it
        }
        return runCatching {
            Settings.Secure.putString(cr, "voice_interaction_service", "")
            Settings.Secure.putString(cr, "assistant", "")
            Thread.sleep(offMs)
            Settings.Secure.putString(cr, "voice_interaction_service", goodVis)
            Settings.Secure.putString(cr, "assistant", goodAssist)
            Thread.sleep(settleMs)
            true
        }.onFailure { Log.e(TAG, "rebind failed", it) }.getOrDefault(false)
    }

    @SuppressLint("PrivateApi")
    fun trigger(): Boolean = runCatching {
        ensureExempt()
        val bundle = Bundle().apply {
            putLong("invocation_time_ms", SystemClock.elapsedRealtime())
            putInt("omni.entry_point", 1)
        }
        val iVims = Class.forName("com.android.internal.app.IVoiceInteractionManagerService")
        val binder = Class.forName("android.os.ServiceManager")
            .getMethod("getService", String::class.java)
            .invoke(null, "voiceinteraction") as IBinder
        val vims = Class.forName("com.android.internal.app.IVoiceInteractionManagerService\$Stub")
            .getMethod("asInterface", IBinder::class.java)
            .invoke(null, binder)
        val ok = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HiddenApiBypass.invoke(iVims, vims, "showSessionFromSession", null, bundle, 7, ATTR_TAG)
        } else {
            HiddenApiBypass.invoke(iVims, vims, "showSessionFromSession", null, bundle, 7)
        }
        ok as? Boolean ?: false
    }.onFailure { Log.e(TAG, "trigger EXCEPTION: ${it.message}", it) }.getOrDefault(false)
}