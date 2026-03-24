package org.delcom.pam_p5_ifs23012.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.delcom.pam_p5_ifs23012.R
import org.delcom.pam_p5_ifs23012.helper.ConstHelper
import org.delcom.pam_p5_ifs23012.helper.RouteHelper
import org.delcom.pam_p5_ifs23012.helper.SuspendHelper
import org.delcom.pam_p5_ifs23012.helper.ToolsHelper
import org.delcom.pam_p5_ifs23012.network.todos.data.ResponseUserData
import org.delcom.pam_p5_ifs23012.ui.components.BottomNavComponent
import org.delcom.pam_p5_ifs23012.ui.components.LoadingUI
import org.delcom.pam_p5_ifs23012.ui.components.TopAppBarComponent
import org.delcom.pam_p5_ifs23012.ui.components.TopAppBarMenuItem
import org.delcom.pam_p5_ifs23012.ui.theme.DelcomTheme
import org.delcom.pam_p5_ifs23012.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_p5_ifs23012.ui.viewmodels.AuthUIState
import org.delcom.pam_p5_ifs23012.ui.viewmodels.AuthViewModel
import org.delcom.pam_p5_ifs23012.ui.viewmodels.ProfileUIState
import org.delcom.pam_p5_ifs23012.ui.viewmodels.TodoActionUIState
import org.delcom.pam_p5_ifs23012.ui.viewmodels.TodoViewModel
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    todoViewModel: TodoViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateTodo by todoViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<ResponseUserData?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true

        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }

        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken

        // Reset state jika perlu
        if(uiStateTodo.profile is ProfileUIState.Success){
            profile = (uiStateTodo.profile as ProfileUIState.Success).data
            isLoading = false
            return@LaunchedEffect
        }

        todoViewModel.getProfile(authToken ?: "")
    }

    LaunchedEffect(uiStateTodo.profile) {
        if(uiStateTodo.profile !is ProfileUIState.Loading){
            isLoading = false
            if(uiStateTodo.profile is ProfileUIState.Success){
                profile = (uiStateTodo.profile as ProfileUIState.Success).data
            } else {
                RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            }
        }
    }

    // Penanganan notifikasi hasil update profil
    LaunchedEffect(uiStateTodo.profileChange) {
        when (val state = uiStateTodo.profileChange) {
            is TodoActionUIState.Success -> {
                isLoading = false
                SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.SUCCESS, state.message)
                todoViewModel.getProfile(authToken ?: "") // Refresh data profil setelah sukses
            }
            is TodoActionUIState.Error -> {
                isLoading = false
                SuspendHelper.showSnackBar(snackbarHost, SuspendHelper.SnackBarType.ERROR, state.message)
            }
            else -> {}
        }
    }

    fun onLogout(token: String){
        isLoading = true
        authViewModel.logout(token)
        todoViewModel.clearData()
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if(isLoading || profile == null){
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(
            text = "Profile",
            icon = Icons.Filled.Person,
            route = ConstHelper.RouteNames.Profile.path
        ),
        TopAppBarMenuItem(
            text = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            route = null,
            onClick = { onLogout(authToken ?: "") }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Profile",
            showBackButton = false,
            customMenuItems = menuItems
        )

        Box(modifier = Modifier.weight(1f)) {
            ProfileUI(
                profile = profile!!,
                onSaveProfile = { name, username, about ->
                    isLoading = true
                    todoViewModel.putUserMe(authToken ?: "", name, username, about)
                },
                onSavePassword = { oldPassword, newPassword ->
                    isLoading = true
                    todoViewModel.putUserMePassword(authToken ?: "", oldPassword, newPassword)
                },
                onPhotoPicked = { multipartBody ->
                    isLoading = true
                    todoViewModel.putUserMePhoto(authToken ?: "", multipartBody)
                }
            )
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun ProfileUI(
    profile: ResponseUserData,
    onSaveProfile: (String, String, String?) -> Unit,
    onSavePassword: (String, String) -> Unit,
    onPhotoPicked: (MultipartBody.Part) -> Unit
){
    val context = LocalContext.current

    // State untuk memunculkan Dialog
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditPasswordDialog by remember { mutableStateOf(false) }

    // State untuk Form Edit Profil
    var name by remember { mutableStateOf(profile.name) }
    var username by remember { mutableStateOf(profile.username) }
    var about by remember { mutableStateOf(profile.about ?: "") }

    // State untuk Form Ubah Password
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Logika Pemilih Gambar dari Galeri
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile_", ".jpg", context.cacheDir)
            inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }

            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            onPhotoPicked(body)
        }
    }

    // --- DIALOG EDIT PROFIL ---
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(text = "Edit Profil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = about,
                        onValueChange = { about = it },
                        label = { Text("Tentang Saya") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveProfile(name, username, about.ifBlank { null })
                    showEditProfileDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Batal") }
            }
        )
    }

    // --- DIALOG UBAH KATA SANDI ---
    if (showEditPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showEditPasswordDialog = false },
            title = { Text(text = "Ubah Kata Sandi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Kata Sandi Lama") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Kata Sandi Baru") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSavePassword(oldPassword, newPassword)
                        showEditPasswordDialog = false
                        oldPassword = ""
                        newPassword = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Ubah Sandi") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditPasswordDialog = false
                    oldPassword = ""
                    newPassword = ""
                }) { Text("Batal") }
            }
        )
    }

    // --- TAMPILAN UTAMA PROFIL (READ-ONLY) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Foto Profil
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = ToolsHelper.getUserImage(profile.id),
                contentDescription = "Photo Profil",
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Foto",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Ketuk foto untuk mengubah", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Nama Lengkap
        Text(
            text = profile.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Username
        Text(
            text = "@${profile.username}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Tentang Saya
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (!profile.about.isNullOrBlank()) profile.about else "Belum ada informasi Tentang Saya.",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = if (!profile.about.isNullOrBlank()) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Tombol Edit Profil
        Button(
            onClick = {
                name = profile.name
                username = profile.username
                about = profile.about ?: ""
                showEditProfileDialog = true
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profil", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Tombol Ubah Password
        Button(
            onClick = { showEditPasswordDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ubah Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewProfileUI(){
    DelcomTheme {
        ProfileUI(
            profile = ResponseUserData(
                id = "",
                name = "Joey Cristo Thruli",
                username = "ifs23012",
                about = "Mahasiswa Informatika",
                createdAt = "",
                updatedAt = ""
            ),
            onSaveProfile = { _, _, _ -> },
            onSavePassword = { _, _ -> },
            onPhotoPicked = { _ -> }
        )
    }
}