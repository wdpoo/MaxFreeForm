package com.yifeplayte.maxfreeform.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yifeplayte.maxfreeform.activity.pages.Main
import com.yifeplayte.maxfreeform.utils.Preferences

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContext = this
        Preferences.init(this)

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent { Main() }
    }
}
