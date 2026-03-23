package org.delcom.pam_p5_ifs23012.ui.screens.todos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_p5_ifs23012.R
import org.delcom.pam_p5_ifs23012.helper.ConstHelper
import org.delcom.pam_p5_ifs23012.helper.RouteHelper
import org.delcom.pam_p5_ifs23012.helper.ToolsHelper
import org.delcom.pam_p5_ifs23012.network.todos.data.ResponseTodoData
import org.delcom.pam_p5_ifs23012.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23012.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23012.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23012.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23012.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23012.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23012.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23012.ui.viewmodels.TodoViewModel
import org.delcom.pam_p5_ifs23012.ui.viewmodels.TodosUIState

@Composable
fun TodosScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isMainLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // State Paginasi dan Data (Memenuhi syarat Infinite Scroll)
    var todos by remember { mutableStateOf<List<ResponseTodoData>>(emptyList()) }
    var authToken by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(1L) }
    var isLastPage by remember { mutableStateOf(false) }

    // State Filter: isDone dipetakan menjadi Status Ketersediaan, urgency menjadi Kondisi Barang
    var selectedIsDone by remember { mutableStateOf<Boolean?>(null) }
    var selectedUrgency by remember { mutableStateOf<String?>(null) }

    fun fetchTodosData(resetPage: Boolean = false) {
        if (resetPage) {
            page = 1L
            isLastPage = false
            todos = emptyList()
            isMainLoading = true
        }

        val token = (uiStateAuth.auth as? AuthUIState.Success)?.data?.authToken
        if (token != null) {
            authToken = token
            todoViewModel.getAllTodos(
                authToken = token,
                search = searchQuery.text.ifBlank { null },
                isDone = selectedIsDone,
                urgency = selectedUrgency,
                page = page
            )
        }
    }

    LaunchedEffect(Unit) {
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        fetchTodosData(resetPage = true)
    }

    LaunchedEffect(uiStateTodo.todos) {
        if (uiStateTodo.todos !is TodosUIState.Loading) {
            isMainLoading = false

            if (uiStateTodo.todos is TodosUIState.Success) {
                val newTodos = (uiStateTodo.todos as TodosUIState.Success).data
                if (newTodos.size < 10) {
                    isLastPage = true
                }
                todos = if (page == 1L) newTodos else todos + newTodos
            } else if (uiStateTodo.todos is TodosUIState.Error) {
                if (page == 1L) todos = emptyList()
            }
        }
    }

    fun onLogout(token: String){
        isMainLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isMainLoading) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(
            text = "Profil",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
        TopAppBarMenuItem(
            text = "Keluar",
            icon = Icons.AutoMirrored.Filled.Logout,
            route = null,
            onClick = { onLogout(authToken ?: "") }
        )
    )

    fun onOpen(todoId: String) {
        RouteHelper.to(navController = navController, destination = "todos/${todoId}")
    }

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        // PENJELASAN: Memenuhi syarat elemen navigasi Top App Bar dan Fitur Pencarian (Search)
        TopAppBarComponent(
            navController = navController,
            title = "Katalog Perabotan", // Disesuaikan dengan tema aplikasi
            showBackButton = false,
            customMenuItems = menuItems,
            withSearch = true,
            searchQuery = searchQuery,
            onSearchQueryChange = { query -> searchQuery = query },
            onSearchAction = { fetchTodosData(resetPage = true) }
        )

        // PENJELASAN: Memenuhi syarat implementasi Fitur Filter Data
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                selectedIsDone = when (selectedIsDone) { null -> true; true -> false; false -> null }
                fetchTodosData(resetPage = true)
            }) { Text("Status: ${if (selectedIsDone == null) "Semua" else if (selectedIsDone == true) "Terjual" else "Tersedia"}") }

            TextButton(onClick = {
                // Siklus filter kondisi barang
                selectedUrgency = when (selectedUrgency) { null -> "Mulus"; "Mulus" -> "Normal"; "Normal" -> "Minus"; "Minus" -> null; else -> null }
                fetchTodosData(resetPage = true)
            }) { Text("Kondisi: ${selectedUrgency ?: "Semua"}") }
        }

        Box(modifier = Modifier.weight(1f)) {
            TodosUI(
                todos = todos,
                isLastPage = isLastPage,
                onLoadMore = {
                    page++
                    fetchTodosData(resetPage = false)
                },
                onOpen = ::onOpen
            )

            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.TodosAdd.path) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Perabotan") }
            }
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun TodosUI(
    todos: List<ResponseTodoData>,
    isLastPage: Boolean,
    onLoadMore: () -> Unit,
    onOpen: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(todos) { todo ->
            TodoItemUI(todo, onOpen)
        }

        item {
            // PENJELASAN: Memenuhi syarat Infinite Scroll / Endless Scrolling
            if (!isLastPage && todos.isNotEmpty()) {
                LaunchedEffect(todos.size) {
                    onLoadMore()
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    if (todos.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = "Tidak ada perabotan yang ditemukan!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
    }
}

@Composable
fun TodoItemUI(todo: ResponseTodoData, onOpen: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onOpen(todo.id) },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            AsyncImage(
                model = ToolsHelper.getTodoImage(todo.id, todo.updatedAt ?: ""),
                contentDescription = todo.title,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier.size(70.dp).clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Title merepresentasikan Nama Perabotan
                Text(text = todo.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                // Description merepresentasikan Deskripsi dan Harga
                Text(text = todo.description ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clip(RoundedCornerShape(50))
                            .background(if (todo.isDone) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        // isDone merepresentasikan Status Penjualan
                        Text(
                            text = if (todo.isDone) "Terjual" else "Tersedia",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (todo.isDone) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    // urgency merepresentasikan Kondisi Barang
                    val safeUrgency = todo.urgency ?: "Mulus"
                    val urgencyColor = when(safeUrgency.lowercase()) {
                        "minus" -> MaterialTheme.colorScheme.errorContainer
                        "normal" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val urgencyTextColor = when(safeUrgency.lowercase()) {
                        "minus" -> MaterialTheme.colorScheme.onErrorContainer
                        "normal" -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            .background(urgencyColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = safeUrgency,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = urgencyTextColor
                        )
                    }
                }
            }
        }
    }
}