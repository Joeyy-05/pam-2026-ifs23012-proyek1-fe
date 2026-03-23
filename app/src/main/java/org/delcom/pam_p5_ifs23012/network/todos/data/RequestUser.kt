package org.delcom.pam_p5_ifs23012.network.todos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestUserChange(
    val name: String,
    val username: String,
    @SerialName("about") // WAJIB ADA: Memaksa kunci JSON menjadi "about"
    val about: String? = null
)

@Serializable
data class RequestUserChangePassword (
    val newPassword: String,
    val password: String
)