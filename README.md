# FUTO Keyboard — AI proofreading fork

A personal fork of [FUTO Keyboard](https://github.com/futo-org/android-keyboard) that adds a one-tap **Proofread** button powered by the Gemini API, and removes a few things I don't use.

> Unofficial. Not affiliated with or endorsed by FUTO. Built for personal, non-commercial use under the original license (see the license file in this repo).

## Thanks

All the keyboard itself — the typing engine, layouts, dictionaries, swipe typing, settings and UI — is the work of **FUTO** and the FUTO Keyboard contributors. This fork only adds and removes a few pieces on top of their work.

- Upstream (GitHub mirror and issue tracker): https://github.com/futo-org/android-keyboard
- Upstream source: https://gitlab.futo.org/keyboard/latinime

## What this fork adds

- **Proofread action**: a spellcheck-icon button in the action row. It fixes spelling, grammar, punctuation and capitalisation using Google's Gemini API, in the language you wrote in.
  - With text selected, it proofreads the selection.
  - With nothing selected, it proofreads the text before the cursor on the current line (up to about 300 characters).
  - The result replaces the text in place.
- **Gemini settings page**: Settings → *AI proofreading (Gemini)* has a field for your API key and an optional model name (default `gemini-3.5-flash-lite`).
- **Offline fallback**: if there is no key, no connection or the request fails, it falls back to a simple local pass that collapses repeated spaces and capitalises sentence starts, and shows a short message saying what went wrong.
- **Status messages and a small local log** (`proofread.log` in the app's private storage) for troubleshooting.
- **CI build**: a GitHub Actions workflow (`build.yml`, run manually) that builds the `unstable` debug APK and publishes it as a draft release.

## What this fork removes or changes

- Removed the payment and "paid version" screens and notices from the settings home.
- Voice input is disabled by default and its settings entry is removed (the underlying module is still in the code so everything keeps compiling).
- Removed 13 built-in themes from the theme picker (AOSP Material Dark/Light, Sunflower, Snowfall, Steel Gray, Emerald, Cotton Candy, Deep Sea Light/Dark, FUTO VI, Construction, High Contrast Yellow, Dynamic Light).
- Hid the app icon from the launcher.
- Added the `INTERNET` permission. Stock FUTO Keyboard has none, so this fork can reach the Gemini API.

## Using it

1. Install the APK and enable the keyboard in system settings (Languages & input → On-screen keyboard → Manage keyboards). The launcher icon is hidden, so there is no app to open.
2. Create a free API key at https://aistudio.google.com and paste it into the keyboard's settings under *AI proofreading (Gemini)*.
3. Add the Proofread button to your action row from the keyboard's action settings if it is not there already.

## Privacy

- Text is sent to the Gemini API **only when you tap Proofread**, never while typing.
- The key is stored in the keyboard's settings on your device.
- On Google's free tier, requests may be used by Google to improve its products. Do not proofread passwords, banking details or other private text.
- The model name can be changed in settings if Google retires the default.

## Not done yet

Tone switching and smart edit are not implemented; only Proofread is.

## License

Same license as upstream — see the license file in this repository. Please respect its terms, including its limits on commercial use.
