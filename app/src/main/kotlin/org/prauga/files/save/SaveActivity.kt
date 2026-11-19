// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.save

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.prauga.files.R
import org.prauga.files.core.data.IncomingRepository

@AndroidEntryPoint
class SaveActivity : ComponentActivity() {
    @Inject lateinit var incoming: IncomingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        val uris = streamsOf(intent).map(Uri::toString)
        if (uris.isEmpty()) {
            finish()
            return
        }
        val downloads =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
        lifecycleScope.launch {
            val saved = incoming.save(uris, downloads)
            val message =
                if (saved == 0) getString(R.string.save_failed)
                else resources.getQuantityString(R.plurals.save_done, saved, saved)
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun streamsOf(intent: Intent): List<Uri> =
        when (intent.action) {
            Intent.ACTION_SEND ->
                listOfNotNull(
                    IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                )
            Intent.ACTION_SEND_MULTIPLE ->
                IntentCompat.getParcelableArrayListExtra(
                        intent,
                        Intent.EXTRA_STREAM,
                        Uri::class.java,
                    )
                    .orEmpty()
            else -> emptyList()
        }
}
