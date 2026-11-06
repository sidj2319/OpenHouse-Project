package com.example.catalogue.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.catalogue.AppContainer
import com.example.catalogue.ui.category.CategoryScreen
import com.example.catalogue.ui.category.CategoryViewModel
import com.example.catalogue.ui.common.screenViewModel
import com.example.catalogue.ui.enquiry.EnquiryScreen
import com.example.catalogue.ui.enquiry.EnquiryViewModel
import com.example.catalogue.ui.home.HomeScreen
import com.example.catalogue.ui.home.HomeViewModel
import com.example.catalogue.ui.product.ProductScreen
import com.example.catalogue.ui.product.ProductViewModel
import com.example.catalogue.ui.search.SearchScreen
import com.example.catalogue.ui.search.SearchViewModel

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category/{name}"
    const val SEARCH = "search/{query}"
    const val PRODUCT = "product/{id}"
    const val ENQUIRY = "enquiry/{id}"

    fun category(name: String) = "category/${Uri.encode(name)}"
    fun search(query: String) = "search/${Uri.encode(query)}"
    fun product(id: Int) = "product/$id"
    fun enquiry(id: Int) = "enquiry/$id"
}

@Composable
fun AppNav(container: AppContainer) {
    val navController = rememberNavController()
    val repository = container.repository

    NavHost(navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val viewModel = screenViewModel { HomeViewModel(repository) }
            HomeScreen(
                viewModel = viewModel,
                onSearch = { navController.navigate(Routes.search(it)) },
                onCategoryClick = { navController.navigate(Routes.category(it)) },
                onProductClick = { navController.navigate(Routes.product(it)) },
            )
        }

        composable(
            Routes.CATEGORY,
            arguments = listOf(navArgument("name") { type = NavType.StringType }),
        ) { entry ->
            val name = entry.arguments?.getString("name").orEmpty()
            val viewModel = screenViewModel { CategoryViewModel(repository, name) }
            CategoryScreen(
                categoryName = name,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onProductClick = { navController.navigate(Routes.product(it)) },
            )
        }

        composable(
            Routes.SEARCH,
            arguments = listOf(navArgument("query") { type = NavType.StringType }),
        ) { entry ->
            val query = entry.arguments?.getString("query").orEmpty()
            val viewModel = screenViewModel { SearchViewModel(repository, query) }
            SearchScreen(
                query = query,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSearch = { navController.navigate(Routes.search(it)) },
                onProductClick = { navController.navigate(Routes.product(it)) },
            )
        }

        composable(
            Routes.PRODUCT,
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            val viewModel = screenViewModel { ProductViewModel(repository, id) }
            ProductScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEnquire = { navController.navigate(Routes.enquiry(it)) },
            )
        }

        composable(
            Routes.ENQUIRY,
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            val viewModel = screenViewModel { EnquiryViewModel(repository, id) }
            EnquiryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
    }
}
