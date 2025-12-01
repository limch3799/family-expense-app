package com.moaga.app.model

data class User(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String? = null,
    val profileImage: String? = null,
    val createdAt: String,
    val updatedAt: String

){

}