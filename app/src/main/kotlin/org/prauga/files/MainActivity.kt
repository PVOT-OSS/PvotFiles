// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : PvotBaseActivity() {
    private val launch = mutableStateOf<LaunchTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) open(intent)
        LaunchShortcuts.publish(this)
        setPvotContent {
            PvotAppTheme {
                AppScaffold(launch = launch.value, onLaunchHandled = { launch.value = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        open(intent)
    }

    private fun open(intent: Intent?) {
        val target = LaunchTarget.of(intent) ?: return
        launch.value = target
        LaunchShortcuts.reportUsed(this, target)
    }
}
