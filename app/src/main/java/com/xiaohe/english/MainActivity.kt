package com.xiaohe.english

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xiaohe.english.ui.nav.AppRoot
import com.xiaohe.english.ui.theme.XiaoheMD3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as XiaoheApp
        setContent {
            XiaoheMD3Theme {
                AppRoot(isLoggedIn = app.tokenManager.isLoggedIn())
            }
        }
    }
}
