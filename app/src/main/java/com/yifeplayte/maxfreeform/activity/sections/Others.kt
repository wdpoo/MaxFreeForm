package com.yifeplayte.maxfreeform.activity.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yifeplayte.maxfreeform.R
import com.yifeplayte.maxfreeform.activity.dialogs.ResetConfigurationDialog
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle

fun LazyListScope.others() {
    item {
        SmallTitle(
            text = stringResource(R.string.others),
        )
    }
    item {
        val showResetDialog = remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
        ) {
            BasicComponent(
                title = stringResource(R.string.reset_configuration),
                titleColor = BasicComponentDefaults.titleColor(
                    color = Color.Red
                ),
                onClick = {
                    showResetDialog.value = true
                },
            )
        }
        ResetConfigurationDialog(showResetDialog)
    }
}
