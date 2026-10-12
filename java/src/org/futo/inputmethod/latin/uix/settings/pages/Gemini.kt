package org.futo.inputmethod.latin.uix.settings.pages

import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.GEMINI_API_KEY
import org.futo.inputmethod.latin.uix.GEMINI_MODEL
import org.futo.inputmethod.latin.uix.settings.SettingTextField
import org.futo.inputmethod.latin.uix.settings.UserSettingsMenu
import org.futo.inputmethod.latin.uix.settings.userSettingDecorationOnly

val GeminiMenu = UserSettingsMenu(
    title = R.string.gemini_settings_title,
    navPath = "gemini", registerNavPath = true,
    settings = listOf(
        userSettingDecorationOnly {
            SettingTextField("Gemini API key", "Paste your key from aistudio.google.com", GEMINI_API_KEY)
        },
        userSettingDecorationOnly {
            SettingTextField("Model (optional)", "gemini-3.1-flash-lite", GEMINI_MODEL)
        },
    )
)
