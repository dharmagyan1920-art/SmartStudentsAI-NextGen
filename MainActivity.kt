package com.smartstudents.nextgen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.smartstudents.nextgen.ui.AppNav
import com.smartstudents.nextgen.ui.SmartStudentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartStudentsTheme {
                AppNav()
            }
        }
    }
}
