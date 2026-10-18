package com.yifeplayte.maxfreeform.activity.dialogs

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.MainActivity.Companion.appContext
import com.yifeplayte.maxfreeform.hook.PACKAGE_NAME_HOOKED
import com.yifeplayte.maxfreeform.utils.Build.HYPER_OS_VERSION_CODE
import com.yifeplayte.maxfreeform.hook.utils.SYSTEM_SERVER_PACKAGE_NAME
import com.yifeplayte.maxfreeform.utils.Terminal
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.LocalDismissState

@Composable
fun RestartAllScopeDialog(showDialog: MutableState<Boolean>) {
    OverlayDialog(
        title = stringResource(R.string.warning),
        summary = stringResource(R.string.restart_all_scope_tips),
        show = showDialog.value,
        onDismissRequest = { showDialog.value = false },
    ) {
        val dismissState = LocalDismissState.current
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.cancel),
                onClick = { dismissState?.invoke() }
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.done),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                onClick = {
                    PACKAGE_NAME_HOOKED.forEach {
                        // system_server 不能靠 killall 生效；HyperOS 4 的桌面是 native
                        // 进程，没有加载本模块的 Java hook，也不需要重启。
                        if (it != SYSTEM_SERVER_PACKAGE_NAME &&
                            !(HYPER_OS_VERSION_CODE >= 4 && it == "com.miui.home")
                        ) {
                            Terminal.exec("killall $it")
                        }
                    }
                    Toast.makeText(
                        appContext, appContext.getString(R.string.finished), Toast.LENGTH_SHORT
                    ).show()
                    dismissState?.invoke()
                }
            )
        }
    }
}
