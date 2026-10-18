package com.yifeplayte.maxfreeform.activity.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.yifeplayte.maxfreeform.utils.Preferences
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SPSwitch(
    key: String,
    @StringRes titleId: Int,
    @StringRes summaryId: Int? = null,
    defaultValue: Boolean = false,
    enabled: Boolean = true,
) {
    var checked by remember(key) {
        mutableStateOf(Preferences.getBoolean(key, defaultValue))
    }
    SwitchPreference(
        title = stringResource(titleId),
        summary = summaryId?.let { stringResource(it) },
        checked = checked,
        enabled = enabled,
        onCheckedChange = {
            checked = it
            Preferences.putBoolean(key, it)
        },
    )
}
