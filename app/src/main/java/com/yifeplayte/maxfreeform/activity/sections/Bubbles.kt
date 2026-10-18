package com.yifeplayte.maxfreeform.activity.sections

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.MainActivity.Companion.appContext
import com.yifeplayte.maxfreeform.activity.ui.SPSwitch
import com.yifeplayte.maxfreeform.activity.ui.SectionTitle
import com.yifeplayte.maxfreeform.utils.Build.IS_HYPER_OS
import com.yifeplayte.maxfreeform.utils.Terminal
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card

fun LazyListScope.bubbles(onRestart: (List<String>) -> Unit) {
    item {
        SectionTitle(
            titleId = R.string.bubbles,
            packages = if (IS_HYPER_OS) {
                listOf("com.android.systemui")
            } else {
                listOf("com.miui.securitycenter")
            },
            onRestart = onRestart,
        )
    }
    item {
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
        ) {
            SPSwitch(
                key = "remove_conversation_bubble_settings_restriction",
                titleId = R.string.remove_conversation_bubble_settings_restriction,
                summaryId = R.string.remove_conversation_bubble_settings_restriction_tips,
            )
            BasicComponent(
                title = stringResource(R.string.try_to_fix_conversation_bubbles),
                summary = stringResource(R.string.try_to_fix_conversation_bubbles_tips),
                onClick = {
                    Terminal.exec(
                        "pm enable com.miui.securitycenter/com.miui.bubbles.services.BubblesNotificationListenerService"
                    )
                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.finished),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }
}
