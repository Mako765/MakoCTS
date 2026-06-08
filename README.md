# MakoCTS

**Trigger Google Circle to Search — and automatically re-link the Google assistant when it stops responding.**

MakoCTS is a small Android app that triggers Circle to Search (CtS) on devices where it
keeps "dying" in the background.

Its key difference from other triggers: when triggering fails because the system lost the
Google voice-interaction binding, **MakoCTS repairs the binding itself** instead of leaving
you to do it by hand in Settings.

> ### Tested device
> MakoCTS was built and tested specifically on the **vivo X200 Ultra (Chinese version,
> OriginOS, Android 15)**. On that device it works reliably.
>CONFIRMED, WORKS ON X300U
> It **may also work on other Chinese-market phones** (vivo / iQOO, Xiaomi / Redmi / POCO,
> OPPO, etc.) that rely on the same VIS trigger — but this is **not guaranteed**. Some
> device-specific values (e.g. the assistant component and the internal trigger tag) are
> tuned for the X200 Ultra and may differ elsewhere. Treat use on other devices as
> "try it and see". If the trigger itself doesn't work on your device, neither will any
> trigger that uses this method — see [MiCTS](https://github.com/parallelcc/MiCTS), which
> this project is based on.

---

## Why this exists

On many non-Pixel / Chinese ROMs, Circle to Search works through the **VoiceInteractionService
(VIS)**, which requires Google to be the default assistant *and* for that binding to stay
alive. Aggressive background killing breaks the binding, and CtS stops triggering
("trigger failed"). The usual manual fix is:

> Settings → Default digital assistant app → switch to *None* → switch back to *Google*.

MakoCTS automates exactly that fix.

---

## What it does

- **Triggers Circle to Search** via `IVoiceInteractionManagerService.showSessionFromSession`
  (the same VIS mechanism used by MiCTS in non-root mode).
- **Self-heals on failure:** if triggering fails *and* the app has been granted
  `WRITE_SECURE_SETTINGS`, it briefly clears and restores the `voice_interaction_service`
  secure setting (the programmatic equivalent of the *None → Google* toggle), then triggers
  again — no Settings screen, no manual steps.
- Works from the **app icon**, a **Quick Settings tile**, or any launcher/side-bar/gesture
  tool that can open an app.

> Without the permission, MakoCTS still triggers normally; it just can't auto-repair.

---

## Requirements

- Android 10 (API 29) or newer. Built and verified on **vivo X200 Ultra (OriginOS, Android 15)**.
- **Google** set as the default digital assistant app.
- Latest Google app installed; recommended: enable autostart and set the Google app to
  *unrestricted* battery / no background restrictions.
- No root required.
- Circle to Search must be available for your device through Google in the first place
  (on the X200 Ultra it is). On devices where Google has not enabled CtS, no VIS-based
  trigger can force it without root.

---

## Installation

1. Download the latest APK from the [Releases](../../releases) page.
2. Install it (allow installation from unknown sources if prompted).
3. (Optional but recommended) Grant the self-heal permission — see below.

---

## Granting the self-heal permission (`WRITE_SECURE_SETTINGS`)

This permission lets the app re-link the assistant by itself. It is a one-time step and
**does not require root**. It is granted via ADB. The grant is permanent and survives reboots.

> ⚠️ If you ever **uninstall** the app, the permission is lost and must be granted again.

The command is always:

```
pm grant com.mako.cts android.permission.WRITE_SECURE_SETTINGS
```

### Option A — With a computer (USB ADB)

1. On the phone: Settings → About phone → tap *Build number* 7 times to unlock
   **Developer options**.
2. Developer options → enable **USB debugging** (on some vivo/OEM ROMs also enable the
   separate *USB debugging (Security settings)*).
3. Connect the phone to the PC and set the USB mode to **File transfer**.
4. Make sure ADB is installed on the PC ([platform-tools](https://developer.android.com/tools/releases/platform-tools)),
   then run:

   ```
   adb shell pm grant com.mako.cts android.permission.WRITE_SECURE_SETTINGS
   ```

### Option B — Without a computer (wireless ADB, e.g. LADB)

Useful if PC drivers are a problem (common with Chinese vivo devices).

1. Install an on-device ADB shell app such as **LADB** (Play Store / F-Droid).
2. Developer options → enable **Wireless debugging** (keep the phone on Wi-Fi).
3. Open LADB. If the floating window can't appear over Settings, use **Split screen**
   (LADB on top, Settings below) — or enable *Allow screen overlays on settings* in
   Developer options.
4. In Settings → Developer options → Wireless debugging → **Pair device with pairing code**.
5. In LADB enter the **6-digit pairing code** and the **5-digit pairing port** shown there,
   then tap **PAIR**.
6. Once connected, run:

   ```
   pm grant com.mako.cts android.permission.WRITE_SECURE_SETTINGS
   ```

### Verify it worked

```
dumpsys package com.mako.cts | grep WRITE_SECURE
```

You should see `granted=true`.

---

## Usage

- **App icon / Quick Settings tile / side bar:** open MakoCTS over whatever is on screen
  to trigger Circle to Search.
- The "Asistent (popravak)" tile (optional) opens the assistant picker quickly, as a manual
  fallback if you prefer not to grant the permission.

---

## Known limitations

- **Tuned for the vivo X200 Ultra.** Some internal values (the Google assistant component
  and the trigger attribution tag) are hard-coded to what works on that device. On other
  phones they may differ, so triggering or the auto-relink may not work without changes.
- Without root only the **VIS** path is available, so Google must remain the default assistant.
  (Root + LSPosed users can use the CSHelper/CSService path in MiCTS, which avoids this issue
  entirely — see MiCTS.)
- On some OEM "side bar / small window" launchers the app may briefly flash before CtS
  appears; this is the OEM's floating-window container, not the app's own UI.

---

## Credits

The Circle to Search trigger mechanism is based on the work of
**[parallelcc/MiCTS](https://github.com/parallelcc/MiCTS)**. Huge thanks to the MiCTS author
for figuring out and open-sourcing the VIS trigger — MakoCTS would not exist without it.

MakoCTS adds the automatic assistant re-link on top of that trigger.

---

## License

Licensed under the **GNU General Public License v3.0 (GPL-3.0)**, in accordance with the
license of MiCTS, on which the trigger code is based. See [LICENSE](LICENSE).


If you find this app useful, consider supporting the developer ☕
💙 Donate via PayPal: https://paypal.me/makocts
