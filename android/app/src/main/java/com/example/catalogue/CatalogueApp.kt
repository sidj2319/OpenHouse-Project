package com.example.catalogue

import android.app.Application

class CatalogueApp : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
