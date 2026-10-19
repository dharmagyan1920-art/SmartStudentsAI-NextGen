package com.smartstudents.nextgen.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartstudents.nextgen.data.AssetCurriculumRepository
import com.smartstudents.nextgen.data.CurriculumRepository
import com.smartstudents.nextgen.data.Selection
import com.smartstudents.nextgen.data.SelectionStore
import com.smartstudents.nextgen.model.Curriculum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CurriculumState {
    object Loading : CurriculumState
    data class Error(val message: String) : CurriculumState
    data class Ready(val curriculum: Curriculum) : CurriculumState
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: CurriculumRepository = AssetCurriculumRepository(app.applicationContext)
    private val store = SelectionStore(app.applicationContext)

    private val _state = MutableStateFlow<CurriculumState>(CurriculumState.Loading)
    val state: StateFlow<CurriculumState> = _state.asStateFlow()

    /** null until the saved selection has been read from the device. */
    val selection: StateFlow<Selection?> =
        store.selection.stateIn<Selection?>(viewModelScope, SharingStarted.Eagerly, null)

    init {
        load()
    }

    fun load() {
        _state.value = CurriculumState.Loading
        viewModelScope.launch {
            _state.value = repository.load().fold(
                onSuccess = { CurriculumState.Ready(it) },
                onFailure = { CurriculumState.Error(it.message ?: it.javaClass.simpleName) },
            )
        }
    }

    fun selectBoard(id: String) { viewModelScope.launch { store.setBoard(id) } }
    fun selectClass(level: Int) { viewModelScope.launch { store.setClass(level) } }
    fun selectMedium(id: String) { viewModelScope.launch { store.setMedium(id) } }
    fun selectStream(id: String) { viewModelScope.launch { store.setStream(id) } }

    /** Adds a custom board name and selects it. Custom boards live on this device only. */
    fun addCustomBoard(name: String) {
        viewModelScope.launch { store.addCustomBoard(name)?.let { store.setBoard(it) } }
    }
}
