package com.antigravity.aegis.presentation.feature.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aegis.domain.model.Client
import com.antigravity.aegis.domain.model.ClientType
import com.antigravity.aegis.domain.usecase.GetClientsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientListUiState(
    val clients: List<Client> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterType: ClientType? = null
)

@HiltViewModel
class ClientListViewModel @Inject constructor(
    private val getClientsUseCase: GetClientsUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<ClientType?>(null)
    
    // Combine search query and filter to produce the final list
    val uiState: StateFlow<ClientListUiState> = combine(
        _searchQuery,
        _filterType
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        getClientsUseCase(query).map { clients ->
            val filtered = if (filter != null) {
                clients.filter { it.tipoCliente == filter }
            } else {
                clients
            }
            ClientListUiState(
                clients = filtered,
                searchQuery = query,
                filterType = filter
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClientListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(type: ClientType?) {
        _filterType.value = type
    }
}
