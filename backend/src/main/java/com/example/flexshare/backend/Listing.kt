package com.example.flexshare.backend

import kotlinx.serialization.Serializable

@Serializable
data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val pricePerDay: Double,
    val ownerName: String,
    val category: String,
    val condition: String,
    val imageUrl: String? = null
)