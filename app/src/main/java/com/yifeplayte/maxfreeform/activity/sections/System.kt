package com.yifeplayte.maxfreeform.activity.sections

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.AppListActivity
import com.yifeplayte.maxfreeform.activity.ui.SPSwitch
import com.yifeplayte.maxfreeform.activity.ui.SectionTitle
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import com.yifeplayte.maxfreeform.utils.SYSTEM_SERVER_PACKAGE_NAME
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.ArrowPreference

fun LazyListScope.system(onRestart: (List<String>) -> Unit) {
    item {
        SectionTitle(
            titleId = R.string.system,
            packages = listOf(SYSTEM_SERVER_PACKAGE_NAME),
            onRestart = onRestart,
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
            SPSwitch(
                key = "unlock_foreground_pin",
                titleId = R.string.unlock_foreground_pin,
            )
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
