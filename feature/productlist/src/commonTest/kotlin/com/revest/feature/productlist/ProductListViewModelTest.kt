package com.revest.feature.productlist

import com.revest.domain.model.Product
import com.revest.domain.model.ProductsPage
import com.revest.domain.usecase.*
import com.revest.feature.productlist.presentation.ProductListEvent
import com.revest.feature.productlist.presentation.ProductListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProductListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun sampleProduct(id: Int = 1) = Product(
        id = id, title = "Product $id", description = "desc", price = 10.0 * id,
        discountPercentage = 5.0, rating = 4.0, stock = 100,
        brand = "Brand", category = "phones",
        thumbnail = "https://img/$id.jpg", images = emptyList()
    )

    private fun buildViewModel(
        productsPage: ProductsPage = ProductsPage(listOf(sampleProduct()), 1, 0, 20),
        searchPage: ProductsPage   = ProductsPage(listOf(sampleProduct(2)), 1, 0, 20),
        categories: List<String>   = listOf("phones", "laptops"),
        productsError: Throwable?  = null
    ): ProductListViewModel {
        val productsResult = if (productsError != null)
            Result.failure(productsError)
        else
            Result.success(productsPage)

        return ProductListViewModel(
            getProductsUseCase           = GetProductsUseCase(FakeRepo(productsResult, searchPage, categories)),
            searchProductsUseCase        = SearchProductsUseCase(FakeRepo(productsResult, searchPage, categories)),
            getCategoriesUseCase         = GetCategoriesUseCase(FakeRepo(productsResult, searchPage, categories)),
            getProductsByCategoryUseCase = GetProductsByCategoryUseCase(FakeRepo(productsResult, searchPage, categories))
        )
    }

    @BeforeTest fun setUp()    { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test fun `initial state has loading true`() {
        val vm = buildViewModel()
        // After construction + coroutines run, products should be loaded
        advanceUntilIdle()
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun `loads products on init`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        assertEquals(1, vm.state.value.products.size)
        assertNull(vm.state.value.error)
    }

    @Test fun `loads categories on init`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        assertEquals(listOf("phones", "laptops"), vm.state.value.categories)
    }

    @Test fun `refresh resets pagination`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onEvent(ProductListEvent.Refresh)
        advanceUntilIdle()
        assertEquals(0, vm.state.value.error?.length ?: 0)
        assertTrue(vm.state.value.products.isNotEmpty())
    }

    @Test fun `error state shows message on failure`() = runTest {
        val vm = buildViewModel(productsError = RuntimeException("No network"))
        advanceUntilIdle()
        assertEquals("No network", vm.state.value.error)
        assertTrue(vm.state.value.products.isEmpty())
    }

    @Test fun `dismiss error clears error state`() = runTest {
        val vm = buildViewModel(productsError = RuntimeException("err"))
        advanceUntilIdle()
        vm.onEvent(ProductListEvent.DismissError)
        assertNull(vm.state.value.error)
    }

    @Test fun `category selection resets product list`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onEvent(ProductListEvent.CategorySelected("phones"))
        advanceUntilIdle()
        assertEquals("phones", vm.state.value.selectedCategory)
    }
}

// ── Fake Repo ─────────────────────────────────────────────────────────────────
private class FakeRepo(
    private val productsResult: Result<ProductsPage>,
    private val searchPage: ProductsPage,
    private val categories: List<String>
) : com.revest.domain.repository.ProductRepository {
    override suspend fun getProducts(limit: Int, skip: Int) = productsResult
    override suspend fun getProductById(id: Int) = productsResult.map { it.products.first() }
    override suspend fun searchProducts(query: String) = Result.success(searchPage)
    override suspend fun getProductsByCategory(category: String) = productsResult
    override suspend fun getCategories() = Result.success(categories)
}
