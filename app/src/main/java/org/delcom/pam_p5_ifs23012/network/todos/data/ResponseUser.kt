package org.delcom.pam_p5_ifs23012.network.todos.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ResponseUser (
    val user: ResponseUserData
)

@Serializable
data class ResponseUserData(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    @SerialName("about") // WAJIB ADA
    val about: String? = "",
    val createdAt: String? = "",
    val updatedAt: String? = ""
)