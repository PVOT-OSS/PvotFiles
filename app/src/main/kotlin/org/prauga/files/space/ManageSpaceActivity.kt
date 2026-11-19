// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.space

import android.app.ActivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.button.PvotButton
import com.prauga.pvot.designsystem.components.button.PvotButtonStyle
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotSpacing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.prauga.files.FilesTheme
import org.prauga.files.R

@AndroidEntryPoint
class ManageSpaceActivity : PvotBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPvotContent {
            FilesTheme {
                ManageSpace(onBack = ::finish, onClearCache = ::clearCache, onClearAll = ::clearAll)
            }
        }
    }

    private fun clearCache() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                cacheDir.listFiles().orEmpty().forEach { it.deleteRecursively() }
            }
            Toast.makeText(applicationContext, R.string.space_cache_cleared, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun clearAll() {
        getSystemService(ActivityManager::class.java).clearApplicationUserData()
    }
}

@Composable
private fun ManageSpace(onBack: () -> Unit, onClearCache: () -> Unit, onClearAll: () -> Unit) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.space_title),
                onBack = onBack,
                compact = false,
            )
        },
        bottomBar = {},
        overlay = {
            PvotDialog(
                visible = confirm,
                onDismissRequest = { confirm = false },
                title = stringResource(R.string.space_clear_all_title),
                message = stringResource(R.string.space_clear_all_message),
                destructive = true,
            ) {
                PvotDialogAction(
                    label = stringResource(R.string.space_clear_all),
                    onClick = onClearAll,
                )
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(PvotSpacing.L),
            verticalArrangement = Arrangement.spacedBy(PvotSpacing.M),
        ) {
            Text(stringResource(R.string.space_message), style = MaterialTheme.typography.bodyLarge)
            PvotButton(text = stringResource(R.string.space_clear_cache), onClick = onClearCache)
            PvotButton(
                text = stringResource(R.string.space_clear_all),
                onClick = { confirm = true },
                style = PvotButtonStyle.Text,
            )
        }
    }
}
