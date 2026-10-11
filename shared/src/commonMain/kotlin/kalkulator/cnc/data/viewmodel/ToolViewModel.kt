package kalkulator.cnc.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.data.domain.CncCategory
import kalkulator.cnc.entity.CncToolEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch // Wymagany nowy import

class ToolViewModel(private val repository: MainRepository) : ViewModel() {

    val selectedMaterialGroup = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)
    val selectedDiameter = MutableStateFlow<Double?>(null)
    val selectedModel = MutableStateFlow<String?>(null)

    // 🔥 NOWE, BEZPIECZNIEJSZE PODEJŚCIE: Zwykły MutableStateFlow z ręczną aktualizacją
    private val _categories = MutableStateFlow<List<CncCategory>>(emptyList())
    val categories: StateFlow<List<CncCategory>> = _categories.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableDiameters: StateFlow<List<Double>> = combine(
        selectedCategory,
        selectedMaterialGroup
    ) { category, material ->
        if (category != null && material.isNotEmpty()) {
            repository.getDiametersByCategoryAndMaterial(category, material)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableModels: StateFlow<List<String>> = combine(
        selectedCategory,
        selectedMaterialGroup,
        selectedDiameter
    ) { category, material, diameter ->
        if (category != null && diameter != null && material.isNotEmpty()) {
            repository.getModelsBySpecifics(category, material, diameter)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toolParameters: StateFlow<CncToolEntity?> = combine(
        selectedModel,
        selectedMaterialGroup,
        selectedDiameter
    ) { model, material, diameter ->
        if (model != null && diameter != null && material.isNotEmpty()) {
            repository.getParams(model, material, diameter)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setIsoGroup(iso: String) {
        selectedMaterialGroup.value = iso
        selectedCategory.value = null
        selectedDiameter.value = null
        selectedModel.value = null

        viewModelScope.launch {
            repository.getCategoriesByMaterial(iso).collect { list ->
                _categories.value = list
            }
        }
    }

    fun setCategory(category: String) {
        selectedCategory.value = category
        selectedDiameter.value = null
        selectedModel.value = null
    }

    fun setDiameter(diameter: Double) {
        selectedDiameter.value = diameter
        selectedModel.value = null
    }

    fun setModel(model: String) {
        selectedModel.value = model
    }

    fun resetSelection() {
        selectedMaterialGroup.value = ""
        selectedCategory.value = null
        selectedDiameter.value = null
        selectedModel.value = null
        _categories.value = emptyList()
    }
}