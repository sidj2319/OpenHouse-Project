package com.example.catalogue.ui.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.catalogue.ui.common.CatalogueTopBar
import com.example.catalogue.ui.common.UiStateContent
import com.example.catalogue.ui.common.categoryLabel
import com.example.catalogue.ui.common.formatPrice
import com.example.catalogue.ui.common.formatRating

@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onEnquire: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { CatalogueTopBar("Product details", onBack) }) { padding ->
        UiStateContent(state, onRetry = viewModel::load, modifier = Modifier.padding(padding)) { product ->
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(product.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(formatPrice(product.price), style = MaterialTheme.typography.titleLarge)
                Text(
                    "${formatRating(product)} · ${categoryLabel(product.category)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                Text(product.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { onEnquire(product.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Enquire")
                }
            }
        }
    }
}
