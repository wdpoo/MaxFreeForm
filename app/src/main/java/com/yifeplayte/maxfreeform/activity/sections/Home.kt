package com.yifeplayte.maxfreeform.activity.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.ui.SPSwitch
import com.yifeplayte.maxfreeform.activity.ui.SectionTitle
import com.yifeplayte.maxfreeform.utils.Build.HYPER_OS_VERSION_CODE
import top.yukonga.miuix.kmp.basic.Card

fun LazyListScope.home(onRestart: (List<String>) -> Unit) {
    // HyperOS 4 的 com.miui.home 是 native 进程，不加载 Java hook
    if (HYPER_OS_VERSION_CODE >= 4) return
    item {
        SectionTitle(
            titleId = R.string.miui_home,
            packages = listOf("com.miui.home"),
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
                key = "add_freeform_shortcut",
                titleId = R.string.add_freeform_shortcut,
                summaryId = R.string.add_freeform_shortcut_tips,
            )
        }
    }
}
