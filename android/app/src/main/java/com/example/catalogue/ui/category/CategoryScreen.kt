package com.example.catalogue.ui.category

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.ui.common.CatalogueTopBar
import com.example.catalogue.ui.common.ProductGrid
import com.example.catalogue.ui.common.UiStateContent
import com.example.catalogue.ui.common.categoryLabel
import com.example.catalogue.ui.common.productItems
import com.example.catalogue.ui.common.sectionHeader

@Composable
fun CategoryScreen(
    categoryName: String,
    viewModel: CategoryViewModel,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val label = categoryLabel(categoryName)

    Scaffold(topBar = { CatalogueTopBar(label, onBack) }) { padding ->
        UiStateContent(state, onRetry = viewModel::load, modifier = Modifier.padding(padding)) { listing ->
            ProductGrid {
                sectionHeader("Top rated in $label")
                productItems(listing.topRated, onProductClick)
                if (listing.others.isNotEmpty()) {
                    sectionHeader("More in $label")
                    productItems(listing.others, onProductClick)
                }
            }
        }
    }
}
