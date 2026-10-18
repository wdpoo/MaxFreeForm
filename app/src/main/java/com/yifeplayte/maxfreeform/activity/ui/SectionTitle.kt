package com.yifeplayte.maxfreeform.activity.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.utils.SYSTEM_SERVER_PACKAGE_NAME
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 分区分头：标题 + 该分区对应应用的重启按钮。
 *
 * system_server 不参与 killall：它靠重启系统生效，而且设备上存在同名无关进程，
 * 误杀会带来副作用。因此只以 system_server 为目标的分区不显示按钮。
 */
@Composable
fun SectionTitle(
    @StringRes titleId: Int,
    packages: List<String>,
    onRestart: (List<String>) -> Unit,
) {
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
            IconButton(onClick = { onRestart(targets) }) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = stringResource(R.string.restart),
                    tint = MiuixTheme.colorScheme.onBackgroundVariant,
                )
            }
        }
    }
}
