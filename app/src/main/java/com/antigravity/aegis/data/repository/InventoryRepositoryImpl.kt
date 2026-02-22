package com.antigravity.aegis.data.repository

import com.antigravity.aegis.data.local.dao.CrmDao
import com.antigravity.aegis.data.local.entity.ProductEntity
import com.antigravity.aegis.domain.repository.InventoryRepository
import com.antigravity.aegis.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del repositorio de Inventario.
 * Delega al CrmDao (monolítico) las operaciones de productos mientras se migra al DAO dedicado.
 */
@Suppress("DEPRECATION")
class InventoryRepositoryImpl @Inject constructor(
    private val crmDao: CrmDao
) : InventoryRepository {

    override fun getAllProducts(): Flow<List<ProductEntity>> = crmDao.getAllProducts()

    override fun getLowStockProducts(): Flow<List<ProductEntity>> = crmDao.getLowStockProducts()

    override suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        crmDao.getProductByBarcode(barcode)

    override suspend fun insertProduct(product: ProductEntity): Result<Long> = try {
        Result.Success(crmDao.insertProduct(product))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateProductQuantity(id: Int, quantity: Int): Result<Unit> = try {
        Result.Success(crmDao.updateProductQuantity(id, quantity))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteProduct(product: ProductEntity): Result<Unit> = try {
        // CrmDao no tiene deleteProduct — usamos un enfoque alternativo o ignoramos por ahora
        // Se puede añadir una query @Delete al CrmDao o crear un InventoryDao dedicado
        Result.Error(UnsupportedOperationException("deleteProduct no implementado aún — pendiente de InventoryDao dedicado"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getAllProductsSync(): List<ProductEntity> = crmDao.getAllProductsSync()
}
