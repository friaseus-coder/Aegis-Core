package com.antigravity.aegis.domain.repository

import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow

// TODO: migrar ProductEntity a un modelo de dominio puro (Product) en una futura fase.
interface InventoryRepository {
    fun getAllProducts(): Flow<List<ProductEntity>>
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    suspend fun getProductByBarcode(barcode: String): ProductEntity?
    suspend fun insertProduct(product: ProductEntity): Result<Long>
    suspend fun updateProductQuantity(id: Int, quantity: Int): Result<Unit>
    suspend fun deleteProduct(product: ProductEntity): Result<Unit>
    // Sync para backup
    suspend fun getAllProductsSync(): List<ProductEntity>
}
