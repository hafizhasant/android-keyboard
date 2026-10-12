package org.futo.inputmethod.latin.uix.actions

import android.content.Context
import android.view.textservice.SentenceSuggestionsInfo
import android.view.textservice.SpellCheckerSession
import android.view.textservice.SuggestionsInfo
import android.view.textservice.TextInfo
import android.view.textservice.TextServicesManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.Action
import java.util.Locale
import kotlin.coroutines.resume

private data class SpellFix(val offset: Int, val length: Int, val word: String)

object ProofreadCore {
    // Placeholder pass: collapse repeated spaces, capitalise sentence starts
    fun fix(s: String): String {
        val t = s.replace(Regex("[ \\t]{2,}"), " ")
        return Regex("(^|[.!?]\\s+)(\\p{Ll})").replace(t) {
            it.groupValues[1] + it.groupValues[2].uppercase()
        }
    }

    // Spelling pass via the system spell checker; returns text unchanged if unavailable
    suspend fun spell(context: Context, locale: Locale, text: String): String {
        val tsm = context.getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE)
            as? TextServicesManager ?: return text
        val fixes = withTimeoutOrNull(3000L) {
            suspendCancellableCoroutine<List<SpellFix>> { cont ->
                var session: SpellCheckerSession? = null
                val listener = object : SpellCheckerSession.SpellCheckerSessionListener {
                    override fun onGetSuggestions(results: Array<out SuggestionsInfo>?) {}
                    override fun onGetSentenceSuggestions(results: Array<out SentenceSuggestionsInfo>?) {
                        val out = ArrayList<SpellFix>()
                        results?.forEach { r ->
                            for (i in 0 until r.suggestionsCount) {
                                val info = r.getSuggestionsInfoAt(i)
                                val typo = (info.suggestionsAttributes and
                                    SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO) != 0
                                if (typo && info.suggestionsCount > 0) {
                                    out.add(SpellFix(r.getOffsetAt(i), r.getLengthAt(i), info.getSuggestionAt(0)))
                                }
                            }
                        }
                        session?.close()
                        if (cont.isActive) cont.resume(out)
                    }
                }
                val s = tsm.newSpellCheckerSession(null, locale, listener, false)
                session = s
                if (s == null) {
                    cont.resume(emptyList())
                } else {
                    cont.invokeOnCancellation { s.close() }
                    s.getSentenceSuggestions(arrayOf(TextInfo(text)), 1)
                }
            }
        } ?: emptyList()
        val words = text.trim().split(Regex("\\s+")).size
        if (fixes.isEmpty() || (words >= 4 && fixes.size * 2 > words)) return text
        val sb = StringBuilder(text)
        for (f in fixes.sortedByDescending { it.offset }) {
            if (f.offset < 0 || f.offset + f.length > text.length) continue
            val orig = text.substring(f.offset, f.offset + f.length)
            if (orig.any { it.isDigit() } || f.word.equals(orig, ignoreCase = true)) continue
            sb.replace(f.offset, f.offset + f.length, f.word)
        }
        return sb.toString()
    }
}

val ProofreadAction = Action(
    icon = R.drawable.icon_spellcheck,
    name = R.string.action_proofread_title,
    simplePressImpl = { manager, _ ->
        val ime = manager.getLatinIMEForDebug()
        val ic0 = ime.currentInputConnection
        if (ic0 != null) {
            ic0.finishComposingText()
            val selected = ic0.getSelectedText(0)?.toString()
            val useSel = !selected.isNullOrEmpty()
            val original = if (useSel) selected.orEmpty()
                else ic0.getTextBeforeCursor(300, 0)?.toString().orEmpty()
            if (original.isNotEmpty()) {
                val ctx = manager.getContext()
                val locale = manager.getActiveLocales().firstOrNull() ?: Locale.getDefault()
                manager.getLifecycleScope().launch {
                    val fixed = ProofreadCore.fix(ProofreadCore.spell(ctx, locale, original))
                    val ic = ime.currentInputConnection
                    if (ic != null && fixed != original) {
                        val unchanged = if (useSel) ic.getSelectedText(0)?.toString() == original
                            else ic.getTextBeforeCursor(original.length, 0)?.toString() == original
                        if (unchanged) {
                            ic.beginBatchEdit()
                            if (!useSel) ic.deleteSurroundingText(original.length, 0)
                            ic.commitText(fixed, 1)
                            ic.endBatchEdit()
                        }
                    }
                }
            }
        }
    },
    windowImpl = null,
)
