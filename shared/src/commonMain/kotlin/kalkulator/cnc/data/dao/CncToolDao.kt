package kalkulator.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kalkulator.cnc.data.domain.CncCategory
import kalkulator.cnc.entity.CncToolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CncToolDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tools: List<CncToolEntity>)

    @Query("SELECT * FROM cnc_tools")
    fun getAllTools(): Flow<List<CncToolEntity>>

    @Query("SELECT COUNT(*) FROM cnc_tools")
    suspend fun getCount(): Int

    @Query("DELETE FROM cnc_tools")
    suspend fun deleteAll()

    // 1. Kategorie
    @Query("SELECT DISTINCT toolCategory AS name, category_code AS code FROM cnc_tools WHERE materialGroup = :material ORDER BY toolCategory ASC")
    fun getCategoriesByMaterial(material: String): Flow<List<CncCategory>>

    // 2. Średnice na podstawie kategorii i materiału
    @Query("""
        SELECT DISTINCT diameter 
        FROM cnc_tools 
        WHERE toolCategory = :category 
        AND materialGroup = :material 
        ORDER BY diameter ASC
    """)
    fun getDiametersByCategoryAndMaterial(category: String, material: String): Flow<List<Double>>

    // 3. Modele na podstawie kategorii, materiału i średnicy
    @Query("""
        SELECT DISTINCT toolModelName 
        FROM cnc_tools 
        WHERE toolCategory = :category 
        AND materialGroup = :material 
        AND diameter BETWEEN :diameter - 0.001 AND :diameter + 0.001
        ORDER BY toolModelName ASC
    """)
    fun getModelsBySpecifics(category: String, material: String, diameter: Double): Flow<List<String>>

    // 4. Pobranie konkretnych parametrów narzędzia
    @Query("""
        SELECT * FROM cnc_tools 
        WHERE toolModelName = :modelName 
        AND materialGroup = :materialGroup 
        AND diameter BETWEEN :diameter - 0.001 AND :diameter + 0.001 
        LIMIT 1
    """)
    suspend fun getParams(modelName: String, materialGroup: String, diameter: Double): CncToolEntity?
}
