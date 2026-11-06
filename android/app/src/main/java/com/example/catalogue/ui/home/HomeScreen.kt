package com.example.catalogue.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.ui.common.CatalogueTopBar
import com.example.catalogue.ui.common.ProductGrid
import com.example.catalogue.ui.common.SearchBar
import com.example.catalogue.ui.common.UiStateContent
import com.example.catalogue.ui.common.categoryLabel
import com.example.catalogue.ui.common.productItems
import com.example.catalogue.ui.common.sectionHeader

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSearch: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onProductClick: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { CatalogueTopBar("Catalogue") }) { padding ->
        Column(Modifier.padding(padding)) {
            SearchBar(onSearch = onSearch)
            UiStateContent(state, onRetry = viewModel::load) { data ->
                ProductGrid {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(data.categories) { category ->
                                AssistChip(
                                    onClick = { onCategoryClick(category) },
                                    label = { Text(categoryLabel(category)) },
                                )
                            }
                        }
                    }
                    sectionHeader("Top rated")
                    productItems(data.featured, onProductClick)
                }
            }
        }
    }
}
