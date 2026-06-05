package com.mako.cts

import android.app.Activity
import android.os.Bundle
import kotlin.concurrent.thread

class TriggerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thread {
            Cts.captureIfGood(applicationContext)

            Thread.sleep(85L)   // pusti side bar da se sam zatvori prije okidanja

            var ok = Cts.trigger()

            if (!ok && Cts.hasWriteSecure(applicationContext) && !Cts.isAssistantGoogle(applicationContext)) {
                Cts.rebindAssistant(applicationContext, 500L, 450L)
                ok = Cts.trigger()
            }

            runOnUiThread { finish() }
        }
    }
}