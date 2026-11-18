// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotPreferenceRow
import com.prauga.pvot.designsystem.components.list.PvotRowChevron
import com.prauga.pvot.designsystem.components.list.PvotRowIconStyle
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import org.prauga.files.R

@Composable
fun HomeScreen(onOpenDevice: () -> Unit, bottomBar: @Composable () -> Unit) {
    PvotScreenScaffold(
        topBar = { PvotTopBar(title = stringResource(R.string.home_title)) },
        bottomBar = bottomBar,
    ) { padding ->
        PvotScreen(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
        ) {
            item {
                PvotListGroup(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.home_locations),
                ) {
                    item {
                        PvotPreferenceRow(
                            title = stringResource(R.string.browse_root_title),
                            onClick = onOpenDevice,
                            icon = {
                                Icon(
                                    painterResource(R.drawable.ic_folder),
                                    contentDescription = null,
                                )
                            },
                            iconStyle = PvotRowIconStyle.Plain,
                            chevron = PvotRowChevron.Arrow,
                        )
                    }
                }
            }
        }
    }
}
