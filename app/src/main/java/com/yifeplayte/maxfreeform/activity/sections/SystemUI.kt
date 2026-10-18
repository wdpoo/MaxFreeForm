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
import com.yifeplayte.maxfreeform.utils.Build.HYPER_OS_VERSION_CODE
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.ArrowPreference

fun LazyListScope.systemUI(onRestart: (List<String>) -> Unit) {
    item {
        SectionTitle(
            titleId = R.string.system_ui,
            packages = listOf("com.android.systemui"),
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
            SPSwitch(
                key = "can_notification_slide",
                titleId = R.string.can_notification_slide,
                summaryId = R.string.can_notification_slide_tips,
            )
            if (IS_HYPER_OS) {
                SPSwitch(
                    key = "unlock_multiple_task",
                    titleId = R.string.unlock_multiple_task,
                    summaryId = R.string.unlock_multiple_task_tips,
                )
                if (HYPER_OS_VERSION_CODE < 3) SPSwitch(
                    key = "hide_freeform_top_bar",
                    titleId = R.string.hide_freeform_top_bar,
                )
                SPSwitch(
                    key = "remove_freeform_top_bar",
                    titleId = R.string.remove_freeform_top_bar,
                )
                ArrowPreference(
                    title = stringResource(R.string.remove_freeform_top_bar_list),
                    summary = stringResource(R.string.remove_freeform_top_bar_list_tips),
                    onClick = {
                        context.startActivity(
                            Intent(context, AppListActivity::class.java).apply {
                                putExtra(
                                    AppListActivity.EXTRA_KEY,
                                    "remove_freeform_top_bar_list",
                                )
                                putExtra(
                                    AppListActivity.EXTRA_TITLE_RES,
                                    R.string.remove_freeform_top_bar_list,
                                )
                            }
                        )
                    },
                )
                SPSwitch(
                    key = "remove_freeform_bottom_bar",
                    titleId = R.string.remove_freeform_bottom_bar,
                )
            }
        }
    }
}
