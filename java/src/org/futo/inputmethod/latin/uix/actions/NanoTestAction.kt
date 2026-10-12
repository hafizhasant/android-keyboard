package org.futo.inputmethod.latin.uix.actions

import android.util.Log
import android.widget.Toast
import com.google.mlkit.genai.proofreading.ProofreaderOptions
import com.google.mlkit.genai.proofreading.Proofreading
import com.google.mlkit.genai.proofreading.ProofreadingRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.Action

val NanoTestAction = Action(
    icon = R.drawable.icon_spellcheck,
    name = R.string.action_nano_test_title,
    simplePressImpl = { manager, _ ->
        val ctx = manager.getContext()
        manager.getLifecycleScope().launch {
            val msg = withContext(Dispatchers.IO) {
                try {
                    val options = ProofreaderOptions.builder(ctx)
                        .setInputType(ProofreaderOptions.InputType.KEYBOARD)
                        .setLanguage(ProofreaderOptions.Language.ENGLISH)
                        .build()
                    val pr = Proofreading.getClient(options)
                    try {
                        val status = pr.checkFeatureStatus().get()
                        val req = ProofreadingRequest.builder("this is a short msg").build()
                        val res = pr.runInference(req).get().results
                        "NANO OK status=$status result=${res.firstOrNull()}"
                    } finally {
                        pr.close()
                    }
                } catch (e: Throwable) {
                    val c = e.cause ?: e
                    "NANO FAILED: ${c.javaClass.simpleName}: ${c.message}"
                }
            }
            Log.e("NanoTest", msg)
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
        }
    },
    windowImpl = null,
)
