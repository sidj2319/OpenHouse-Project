package com.example.catalogue.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.ui.common.CatalogueTopBar
import com.example.catalogue.ui.common.ProductGrid
import com.example.catalogue.ui.common.SearchBar
import com.example.catalogue.ui.common.UiStateContent
import com.example.catalogue.ui.common.productItems

@Composable
fun SearchScreen(
    query: String,
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onProductClick: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { CatalogueTopBar("Search results", onBack) }) { padding ->
        Column(Modifier.padding(padding)) {
            SearchBar(onSearch = onSearch, initialQuery = query)
            UiStateContent(state, onRetry = viewModel::load) { products ->
                if (products.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No products match \"$query\".", textAlign = TextAlign.Center)
                    }
                } else {
                    ProductGrid { productItems(products, onProductClick) }
                }
            }
        }
    }
}
