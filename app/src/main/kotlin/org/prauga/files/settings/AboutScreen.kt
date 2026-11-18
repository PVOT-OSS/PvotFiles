// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.settings

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R

private data class Link(@StringRes val title: Int, @StringRes val summary: Int, val url: String)

private const val SOURCE = "https://github.com/PVOT-OSS/Files"

private val appLinks =
    listOf(
        Link(R.string.about_source, R.string.about_source_summary, SOURCE),
        Link(R.string.about_report_bug, R.string.about_report_bug_summary, "$SOURCE/issues"),
        Link(
            R.string.about_licence,
            R.string.about_licence_summary,
            "https://www.apache.org/licenses/LICENSE-2.0",
        ),
    )

private val libraries =
    listOf(
        Link(
            R.string.about_lib_pvotlib,
            R.string.about_lib_apache,
            "https://github.com/PVOT-OSS/PvotLib",
        ),
        Link(
            R.string.about_lib_androidx,
            R.string.about_lib_apache,
            "https://developer.android.com/jetpack/androidx",
        ),
        Link(R.string.about_lib_kotlin, R.string.about_lib_apache, "https://kotlinlang.org/"),
        Link(R.string.about_lib_dagger, R.string.about_lib_apache, "https://dagger.dev/hilt/"),
        Link(R.string.about_lib_icons, R.string.about_lib_apache, "https://fonts.google.com/icons"),
    )

@Composable
fun AboutScreen(onBack: () -> Unit, bottomBar: @Composable () -> Unit) {
    BackHandler(onBack = onBack)
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val version =
        remember(context) {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }
    val listState = rememberLazyListState()
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.about_title),
                onBack = onBack,
                compact = rememberPvotTopBarCompact(listState),
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        PvotScreen(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
        ) {
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = PvotSpacing.M)) {
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        stringResource(R.string.about_version, version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.about_maintainer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item { Links(stringResource(R.string.about_app), appLinks) { uriHandler.openUri(it) } }
            item {
                Links(stringResource(R.string.about_libraries), libraries) {
                    uriHandler.openUri(it)
                }
            }
        }
    }
}

@Composable
private fun Links(title: String, links: List<Link>, open: (String) -> Unit) {
    PvotListGroup(Modifier.fillMaxWidth(), title = title) {
        links.forEach { link ->
            item {
                PvotListRow(
                    title = stringResource(link.title),
                    supportingText = stringResource(link.summary),
                    onClick = { open(link.url) },
                )
            }
        }
    }
}
