package com.example.catalogue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.catalogue.ui.navigation.AppNav
import com.example.catalogue.ui.theme.CatalogueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as CatalogueApp).container
        setContent {
            CatalogueTheme {
                AppNav(container)
            }
        }
    }
}
