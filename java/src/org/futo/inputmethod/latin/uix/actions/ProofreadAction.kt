package org.futo.inputmethod.latin.uix.actions
import android.view.KeyEvent
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.Action

object ProofreadCore {
    // Placeholder pass: collapse repeated spaces, capitalise sentence starts
    fun fix(s: String): String {
        val t = s.replace(Regex("[ \\t]{2,}"), " ")
        return Regex("(^|[.!?]\\s+)(\\p{Ll})").replace(t) {
            it.groupValues[1] + it.groupValues[2].uppercase()
        }
    }
}

val ProofreadAction = Action(
    icon = R.drawable.icon_spellcheck,
    name = R.string.action_proofread_title,
    simplePressImpl = { manager, _ ->
        val ic = manager.getLatinIMEForDebug().currentInputConnection
        if (ic != null) {
            ic.finishComposingText()
            val selected = ic.getSelectedText(0)?.toString()
            if (!selected.isNullOrEmpty()) {
                val fixed = ProofreadCore.fix(selected)
                if (fixed != selected) {
                    ic.beginBatchEdit()
                    ic.commitText(fixed, 1)
                    ic.endBatchEdit()
                }
            } else {
                val before = ic.getTextBeforeCursor(300, 0)?.toString().orEmpty()
                val fixed = ProofreadCore.fix(before)
                if (before.isNotEmpty() && fixed != before) {
                    ic.beginBatchEdit()
                    ic.deleteSurroundingText(before.length, 0)
                    ic.commitText(fixed, 1)
                    ic.endBatchEdit()
                }
            }
        }
    },
    windowImpl = null,
)
