package com.example.flexshare.data

import com.example.flexshare.domain.model.Booking
import com.example.flexshare.domain.model.Listing
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.*

interface FlexShareApi {

    @GET("v1/listings")
    suspend fun getListings(): List<Listing>

    @POST("v1/listings")
    suspend fun createListing(@Body newListing: Listing): Listing

    // 🗑️ Delete Route: Targets an item by its path ID parameter
    @DELETE("v1/listings/{id}")
    suspend fun deleteListing(@Path("id") id: String): Any

    // 📝 Update Route: Targets an item by ID and sends the updated payload body
    @PUT("v1/listings/{id}")
    suspend fun updateListing(@Path("id") id: String, @Body updatedListing: Listing): Listing

    // 📅 NEW: Send the booking request payload to the backend server
    @POST("v1/bookings")
    suspend fun bookListing(@Body bookingRequest: Booking): Booking
}