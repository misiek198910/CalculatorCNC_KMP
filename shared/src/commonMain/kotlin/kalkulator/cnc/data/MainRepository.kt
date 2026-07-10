package kalkulator.cnc.data

import kalkulator.cnc.data.dao.CncToolDao
import kalkulator.cnc.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kalkulator.cnc.data.dao.SubscriptionDao
import kalkulator.cnc.entity.ThreadEntity
import kotlinx.coroutines.flow.map
import kalkulator.cnc.data.dao.ThreadDao
import calculatorcnc.shared.generated.resources.Res
import kalkulator.cnc.entity.UserToolEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kalkulator.cnc.data.dao.UserToolDao
import kalkulator.cnc.data.domain.CncCategory
import kalkulator.cnc.entity.CncToolEntity

class MainRepository(
    private val apiService: ApiService,
    private val cncToolDao: CncToolDao,
    private val subscriptionDao: SubscriptionDao,
    private val threadDao: ThreadDao,
    private val userToolDao: UserToolDao
) {

    suspend fun syncDatabase(systemLanguage: String, onProgress: (Int) -> Unit): Boolean {
        initThreadsIfEmpty()

        return try {
            val records = apiService.getCncDatabase(lang = systemLanguage, since = null) { progress ->
                onProgress(progress)
            }

            if (records != null) {
                val entities = records.map { record ->
                    CncToolEntity(
                        articleNumber = record.articleNumber ?: "",
                        toolCategory = record.toolCategory ?: "",
                        toolModelName = record.toolModelName ?: "",
                        materialGroup = record.materialGroup ?: "",
                        diameter = record.diameter ?: 0.0,
                        flutesCount = record.flutesCount ?: 0,
                        vcMin = record.vcMin ?: 0.0,
                        vcMax = record.vcMax ?: 0.0,
                        feedStep = record.feedStep ?: 0.0,
                        categoryCode = record.categoryCode ?: "UNKNOWN"
                    )
                }

                cncToolDao.deleteAll()
                cncToolDao.insertAll(entities)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun initThreadsIfEmpty() {
        try {
            val count = threadDao.getThreadsCount()

            if (count == 0) {
                val bytes = Res.readBytes("files/threads.json")
                val jsonString = bytes.decodeToString()

                val jsonParser = Json { ignoreUnknownKeys = true }
                val threads = jsonParser.decodeFromString<List<ThreadEntity>>(jsonString)

                threadDao.insertAll(threads)
            }
        } catch (e: Exception) {
        }
    }

    suspend fun getPrivacyPolicy(serviceName: String): String? {
        return try {
            apiService.getPrivacyPolicy(serviceName)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun hasTools(): Boolean {
        return cncToolDao.getCount() > 0
    }

    fun getCategoriesByMaterial(material: String): Flow<List<CncCategory>> {
        return cncToolDao.getCategoriesByMaterial(material)
    }

    fun getDiametersByCategoryAndMaterial(category: String, material: String): Flow<List<Double>> {
        return cncToolDao.getDiametersByCategoryAndMaterial(category, material)
    }

    fun getModelsBySpecifics(category: String, material: String, diameter: Double): Flow<List<String>> {
        return cncToolDao.getModelsBySpecifics(category, material, diameter)
    }

    suspend fun getParams(modelName: String, materialGroup: String, diameter: Double): CncToolEntity? {
        return cncToolDao.getParams(modelName, materialGroup, diameter)
    }

    private val _isMetricFlow = MutableStateFlow(true)
    val isMetricFlow: Flow<Boolean> = _isMetricFlow

    val isPremiumFlow: Flow<Boolean> = subscriptionDao.observePremiumStatus().map { it ?: false }

    suspend fun getThreads(spinnerIdx: Int, tableIdx: Int): List<ThreadEntity> {
        return threadDao.getThreads(spinnerIdx, tableIdx)
    }

    fun getAllUserTools(): Flow<List<UserToolEntity>> {
        return userToolDao.getAllTools()
    }

    suspend fun deleteUserTool(tool: UserToolEntity) {
        userToolDao.deleteTool(tool)
    }

    suspend fun getUserToolById(id: Int): UserToolEntity? {
        return userToolDao.getToolById(id)
    }

    suspend fun insertUserTool(tool: UserToolEntity) {
        userToolDao.insertTool(tool)
    }

    suspend fun updateUserTool(tool: UserToolEntity) {
        userToolDao.updateTool(tool)
    }

    suspend fun clearToolsDatabase() {
        cncToolDao.deleteAll()
    }
}