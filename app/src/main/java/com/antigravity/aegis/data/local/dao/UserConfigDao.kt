package com.antigravity.aegis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.antigravity.aegis.data.local.entity.UserConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface UserConfigDao {
    @Query("SELECT * FROM user_config WHERE id = 1")
    fun getUserConfig(): Flow<UserConfig?>

    @Query("SELECT * FROM user_config WHERE id = 1")
    suspend fun getUserConfigOneShot(): UserConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserConfig(config: UserConfig)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: UserConfig)

    @Query("UPDATE user_config SET pricePerKm = :price WHERE id = 1")
    suspend fun updatePricePerKm(price: Double)

    @Query("UPDATE user_config SET language = :language WHERE id = 1")
    suspend fun updateLanguage(language: String)

    @Query("UPDATE user_config SET themeMode = :mode WHERE id = 1")
    suspend fun updateThemeMode(mode: String)

    @Query("SELECT * FROM user_config")
    suspend fun getAllUserConfigsSync(): List<UserConfig>

    @Query("DELETE FROM user_config")
    suspend fun deleteAllUserConfigs()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserConfigs(configs: List<UserConfig>)
}
