// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.prauga.pvot.designsystem.navigation.PvotTopBarDefaults

@Composable
fun rememberGridTopBarCompact(
    state: LazyGridState,
    threshold: Dp = PvotTopBarDefaults.CompactThreshold,
): Boolean {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    val compact by
        remember(state, thresholdPx) {
            derivedStateOf {
                state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > thresholdPx
            }
        }
    return compact
}
