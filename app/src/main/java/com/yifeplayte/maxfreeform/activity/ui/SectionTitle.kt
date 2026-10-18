package com.yifeplayte.maxfreeform.activity.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.hook.utils.SYSTEM_SERVER_PACKAGE_NAME
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 分区分头请求重启的回调。
 *
 * 由页面持有对话框状态，分区自身只发起请求，避免列表项回收时丢失弹窗状态。
 */
val LocalRestartRequest = compositionLocalOf<(List<String>) -> Unit> { {} }

/**
 * 分区分头：标题 + 该分区对应应用的重启按钮。
 *
 * system_server 不参与 killall：它靠重启系统（或模块热重载）生效，而且设备上
 * 存在同名无关进程，误杀会带来副作用。因此只以 system_server 为目标的分区
 * 不显示按钮。
 */
@Composable
fun SectionTitle(@StringRes titleId: Int, packages: List<String>) {
    val requestRestart = LocalRestartRequest.current
    val targets = remember(packages) {
        packages.filter { it != SYSTEM_SERVER_PACKAGE_NAME }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallTitle(
            text = stringResource(titleId),
            modifier = Modifier.weight(1f),
            textColor = MiuixTheme.colorScheme.onBackgroundVariant,
        )
        if (targets.isNotEmpty()) {
            IconButton(onClick = { requestRestart(targets) }) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = stringResource(R.string.restart),
                    tint = MiuixTheme.colorScheme.onBackgroundVariant,
                )
            }
        }
    }
}
