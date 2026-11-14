package com.google.antigravity.mobile.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.google.antigravity.mobile.nativeclient.NativeWorkspaceScreen
import com.google.antigravity.mobile.ui.theme.AntigravityTheme

/** Native ACP client. Google credentials remain in the desktop CLI's keyring. */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AntigravityTheme { NativeWorkspaceScreen() }
        }
    }
}
