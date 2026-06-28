package com.example.flexshare.domain.model
data class Booking(
    val id: String = "",
    val listingId: String,
    val listingTitle: String = "",
    val renterName: String,
    val days: Int,
    val pricePerDay: Double,
    val bookingDate: String = ""
){
    val totalPrice: Double get() = pricePerDay * days
}
