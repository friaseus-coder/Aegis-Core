package com.antigravity.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.antigravity.aegis.data.model.UserConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface UserConfigDao {
    @Query("SELECT * FROM user_config WHERE id = 1 LIMIT 1")
    fun getUserConfig(): Flow<UserConfig?>

    @Query("SELECT * FROM user_config WHERE id = 1 LIMIT 1")
    suspend fun getUserConfigOneShot(): UserConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(userConfig: UserConfig)

    @Query("UPDATE user_config SET price_per_km = :price WHERE id = 1")
    suspend fun updatePricePerKm(price: Double)
}
