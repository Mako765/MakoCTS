package com.mako.cts

import android.content.Intent
import android.provider.Settings
import android.service.quicksettings.TileService

class AssistantTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intents = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (i in intents) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (runCatching { startActivityAndCollapse(i); true }.getOrDefault(false)) return
        }
    }
}