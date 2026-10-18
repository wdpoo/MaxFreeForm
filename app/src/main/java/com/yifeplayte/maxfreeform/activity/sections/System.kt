package com.yifeplayte.maxfreeform.activity.sections

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.AppListActivity
import com.yifeplayte.maxfreeform.activity.ui.SPSwitch
import com.yifeplayte.maxfreeform.activity.ui.SectionTitle
import com.yifeplayte.maxfreeform.hook.utils.SYSTEM_SERVER_PACKAGE_NAME
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.ArrowPreference

fun LazyListScope.system() {
    item {
        SectionTitle(
            titleId = R.string.system,
            packages = listOf(SYSTEM_SERVER_PACKAGE_NAME),
        )
    }
    item {
        val context = LocalContext.current
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
        ) {
            if (!IS_HYPER_OS) SPSwitch(
                key = "unlock_side_hide_freeform",
                titleId = R.string.side_hide_freeform,
                summaryId = R.string.side_hide_freeform_tips,
            )
            SPSwitch(
                key = "remove_small_window_restrictions",
                titleId = R.string.remove_small_window_restrictions,
                summaryId = R.string.remove_small_window_restrictions_tips,
            )
            val unlockForegroundPin = remember { mutableStateOf(false) }
            SPSwitch(
                key = "unlock_foreground_pin",
                titleId = R.string.unlock_foreground_pin,
                switchState = unlockForegroundPin,
            )
            AnimatedVisibility(
                visible = unlockForegroundPin.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                ArrowPreference(
                    title = stringResource(R.string.unlock_foreground_pin_whitelist),
                    onClick = {
                        context.startActivity(
                            Intent(context, AppListActivity::class.java).apply {
                                putExtra(
                                    AppListActivity.EXTRA_KEY,
                                    "unlock_foreground_pin_whitelist",
                                )
                                putExtra(
                                    AppListActivity.EXTRA_TITLE_RES,
                                    R.string.unlock_foreground_pin_whitelist,
                                )
                            }
                        )
                    },
                )
            }
        }
    }
}
