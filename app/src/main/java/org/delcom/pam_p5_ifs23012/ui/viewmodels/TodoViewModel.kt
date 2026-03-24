package org.delcom.pam_p5_ifs23012.ui.viewmodels

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.delcom.pam_p5_ifs23012.network.todos.data.RequestTodo
import org.delcom.pam_p5_ifs23012.network.todos.data.RequestUserChange
import org.delcom.pam_p5_ifs23012.network.todos.data.RequestUserChangePassword
import org.delcom.pam_p5_ifs23012.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23012.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23012.network.todos.service.ITodoRepository
import javax.inject.Inject

sealed interface ProfileUIState {
    data class Success(val data: ResponseUserData) : ProfileUIState
    data class Error(val message: String) : ProfileUIState
    object Loading : ProfileUIState
}

sealed interface TodosUIState {
    data class Success(val data: List<ResponseTodoData>) : TodosUIState
    data class Error(val message: String) : TodosUIState
    object Loading : TodosUIState
}

sealed interface TodoUIState {
    data class Success(val data: ResponseTodoData) : TodoUIState
    data class Error(val message: String) : TodoUIState
    object Loading : TodoUIState
}

sealed interface TodoActionUIState {
    data class Success(val message: String) : TodoActionUIState
    data class Error(val message: String) : TodoActionUIState
    object Loading : TodoActionUIState
}

data class UIStateTodo(
    val profile: ProfileUIState = ProfileUIState.Loading,
    val todos: TodosUIState = TodosUIState.Loading,
    var todo: TodoUIState = TodoUIState.Loading,
    var todoAdd: TodoActionUIState = TodoActionUIState.Loading,
    var todoChange: TodoActionUIState = TodoActionUIState.Loading,
    var todoDelete: TodoActionUIState = TodoActionUIState.Loading,
    var todoChangeCover: TodoActionUIState = TodoActionUIState.Loading,
    var profileChange: TodoActionUIState = TodoActionUIState.Loading
)

@HiltViewModel
@Keep
class TodoViewModel @Inject constructor(
    private val repository: ITodoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UIStateTodo())
    val uiState = _uiState.asStateFlow()

    fun getProfile(authToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profile = ProfileUIState.Loading) }
            val tmpState = runCatching { repository.getUserMe(authToken) }.fold(
                onSuccess = { if (it.status == "success") ProfileUIState.Success(it.data!!.user) else ProfileUIState.Error(it.message) },
                onFailure = { ProfileUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(profile = tmpState) }
        }
    }

    fun putUserMe(authToken: String, name: String, username: String, about: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChange = TodoActionUIState.Loading) }
            val result = runCatching {
                repository.putUserMe(authToken, RequestUserChange(name, username, about))
            }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Error") }
            )
            _uiState.update { it.copy(profileChange = result) }
        }
    }

    fun putUserMePassword(authToken: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChange = TodoActionUIState.Loading) }
            val result = runCatching {
                repository.putUserMePassword(authToken, RequestUserChangePassword(password = oldPassword, newPassword = newPassword))
            }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Error") }
            )
            _uiState.update { it.copy(profileChange = result) }
        }
    }

    fun putUserMePhoto(authToken: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(profileChange = TodoActionUIState.Loading) }
            val result = runCatching { repository.putUserMePhoto(authToken, file) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Error") }
            )
            _uiState.update { it.copy(profileChange = result) }
        }
    }

    fun getAllTodos(authToken: String, search: String? = null, isDone: Boolean? = null, urgency: String? = null, page: Long = 1L) {
        viewModelScope.launch {
            _uiState.update { it.copy(todos = TodosUIState.Loading) }
            val result = runCatching {
                repository.getTodos(authToken, search, isDone, urgency, page, 10)
            }.fold(
                onSuccess = { response ->
                    if (response.status == "success" && response.data != null) TodosUIState.Success(response.data.todos)
                    else TodosUIState.Error(response.message)
                },
                onFailure = { TodosUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todos = result) }
        }
    }

    fun postTodo(authToken: String, title: String, description: String, isDone: Boolean, urgency: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoAdd = TodoActionUIState.Loading) }
            val result = runCatching { repository.postTodo(authToken, RequestTodo(title, description, isDone, urgency)) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Error") }
            )
            _uiState.update { it.copy(todoAdd = result) }
        }
    }

    fun getTodoById(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todo = TodoUIState.Loading) }
            val tmpState = runCatching { repository.getTodoById(authToken, todoId) }.fold(
                onSuccess = { if (it.status == "success") TodoUIState.Success(it.data!!.todo) else TodoUIState.Error(it.message) },
                onFailure = { TodoUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todo = tmpState) }
        }
    }

    fun putTodo(authToken: String, todoId: String, title: String, description: String, isDone: Boolean, urgency: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChange = TodoActionUIState.Loading) }
            val result = runCatching { repository.putTodo(authToken, todoId, RequestTodo(title, description, isDone, urgency)) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Error") }
            )
            _uiState.update { it.copy(todoChange = result) }
        }
    }

    fun putTodoCover(authToken: String, todoId: String, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoChangeCover = TodoActionUIState.Loading) }
            val tmpState = runCatching { repository.putTodoCover(authToken, todoId, file) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoChangeCover = tmpState) }
        }
    }

    fun deleteTodo(authToken: String, todoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(todoDelete = TodoActionUIState.Loading) }
            val tmpState = runCatching { repository.deleteTodo(authToken, todoId) }.fold(
                onSuccess = { if (it.status == "success") TodoActionUIState.Success(it.message) else TodoActionUIState.Error(it.message) },
                onFailure = { TodoActionUIState.Error(it.message ?: "Unknown error") }
            )
            _uiState.update { it.copy(todoDelete = tmpState) }
        }
    }

    // [PERBAIKAN]: Fungsi untuk menghapus semua sisa data profile & todos dari memori
    fun clearData() {
        _uiState.update {
            UIStateTodo() // Mengembalikan state ke kondisi awal bawaan (Loading/Kosong)
        }
    }
}