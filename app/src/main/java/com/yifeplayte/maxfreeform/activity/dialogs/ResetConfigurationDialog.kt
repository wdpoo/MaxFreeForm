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
import com.yifeplayte.maxfreeform.utils.SharedPreferences
import com.yifeplayte.maxfreeform.utils.reloadAllTargets
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.LocalDismissState
import kotlin.system.exitProcess

@Composable
fun ResetConfigurationDialog(showDialog: MutableState<Boolean>) {
    OverlayDialog(
        title = stringResource(R.string.warning),
        summary = stringResource(R.string.reset_configuration_tips),
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
                    SharedPreferences.mSP?.edit()?.clear()?.apply()
                    reloadAllTargets()
                    Toast.makeText(
                        appContext, appContext.getString(R.string.finished), Toast.LENGTH_SHORT
                    ).show()
                    dismissState?.invoke()
                    exitProcess(0)
                }
            )
        }
    }
}
