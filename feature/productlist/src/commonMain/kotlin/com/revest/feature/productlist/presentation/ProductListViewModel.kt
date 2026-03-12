package com.revest.feature.productlist.presentation

import com.revest.core.common.BaseViewModel
import com.revest.core.common.AppDispatchers
import com.revest.core.common.stateDelegate
import com.revest.domain.model.Product
import com.revest.domain.usecase.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── State ─────────────────────────────────────────────────────────────────────
data class ProductListState(
    val products: List<Product>  = emptyList(),
    val isLoading: Boolean       = false,
    val error: String?           = null,
    val searchQuery: String      = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val isSearchMode: Boolean    = false,
    val canLoadMore: Boolean     = true,
    val currentPage: Int         = 0,
    val totalProducts: Int       = 0
)

// ── Events ────────────────────────────────────────────────────────────────────
sealed class ProductListEvent {
    data class SearchQueryChanged(val query: String) : ProductListEvent()
    data class CategorySelected(val category: String?) : ProductListEvent()
    data class ProductClicked(val productId: Int) : ProductListEvent()
    object LoadMore : ProductListEvent()
    object Refresh  : ProductListEvent()
    object DismissError : ProductListEvent()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
class ProductListViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase
) : BaseViewModel() {

    // ── Delegate-backed state ─────────────────────────────────────────────────
    private val _stateDelegate = stateDelegate(ProductListState(isLoading = true))
    val state: StateFlow<ProductListState> by _stateDelegate

    private val _searchFlow = MutableStateFlow("")

    init {
        loadCategories()
        loadProducts(reset = true)
        observeSearch()
    }

    fun onEvent(event: ProductListEvent) {
        when (event) {
            is ProductListEvent.SearchQueryChanged -> {
                _stateDelegate.update { it.copy(searchQuery = event.query) }
                _searchFlow.value = event.query
            }
            is ProductListEvent.CategorySelected -> {
                _stateDelegate.update {
                    it.copy(
                        selectedCategory = event.category,
                        products  = emptyList(),
                        currentPage = 0,
                        canLoadMore = true,
                        searchQuery = "",
                        isSearchMode = false,
                        error = null
                    )
                }
                _searchFlow.value = ""
                loadProducts(reset = true)
            }
            ProductListEvent.LoadMore -> {
                val s = _stateDelegate.current
                if (!s.isLoading && s.canLoadMore && !s.isSearchMode) {
                    loadProducts(reset = false)
                }
            }
            ProductListEvent.Refresh -> {
                _stateDelegate.update {
                    it.copy(products = emptyList(), currentPage = 0,
                        canLoadMore = true, error = null)
                }
                loadProducts(reset = true)
            }
            ProductListEvent.DismissError -> _stateDelegate.update { it.copy(error = null) }
            is ProductListEvent.ProductClicked -> { /* handled by NavController */ }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        launch(AppDispatchers.default) {
            _searchFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _stateDelegate.update {
                            it.copy(isSearchMode = false, products = emptyList(),
                                currentPage = 0, canLoadMore = true)
                        }
                        loadProducts(reset = true)
                    } else {
                        performSearch(query.trim())
                    }
                }
        }
    }

    private fun loadProducts(reset: Boolean) {
        val s = _stateDelegate.current
        if (s.isSearchMode) return

        launch(AppDispatchers.io) {
            _stateDelegate.update { it.copy(isLoading = true, error = null) }

            val skip   = if (reset) 0 else s.currentPage * PAGE_SIZE
            val result = if (s.selectedCategory != null) {
                getProductsByCategoryUseCase(s.selectedCategory)
            } else {
                getProductsUseCase(limit = PAGE_SIZE, skip = skip)
            }

            result.fold(
                onSuccess = { page ->
                    _stateDelegate.update {
                        val merged = if (reset) page.products else it.products + page.products
                        it.copy(
                            products       = merged,
                            totalProducts  = page.total,
                            isLoading      = false,
                            currentPage    = if (reset) 1 else it.currentPage + 1,
                            canLoadMore    = page.hasMore,
                            isSearchMode   = false
                        )
                    }
                },
                onFailure = { err ->
                    _stateDelegate.update {
                        it.copy(isLoading = false, error = err.message ?: "Unknown error")
                    }
                }
            )
        }
    }

    private fun performSearch(query: String) {
        launch(AppDispatchers.io) {
            _stateDelegate.update { it.copy(isLoading = true, error = null, isSearchMode = true) }

            searchProductsUseCase(query).fold(
                onSuccess = { page ->
                    _stateDelegate.update {
                        it.copy(products = page.products, totalProducts = page.total,
                            isLoading = false, canLoadMore = false)
                    }
                },
                onFailure = { err ->
                    _stateDelegate.update {
                        it.copy(isLoading = false, error = err.message ?: "Search failed")
                    }
                }
            )
        }
    }

    private fun loadCategories() {
        launch(AppDispatchers.io) {
            getCategoriesUseCase().onSuccess { cats ->
                _stateDelegate.update { it.copy(categories = cats) }
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
