package com.example.flexshare.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flexshare.data.RetrofitClient
import com.example.flexshare.domain.model.Listing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import com.example.flexshare.domain.model.Booking
import kotlinx.coroutines.launch
import android.net.Uri

class HomeViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Track the currently selected category filter ("All" means no filter)
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _selectedListing = MutableStateFlow<Listing?>(null)

    // Combine all states: listings, search query, loading, selected item, AND category!
    val uiState: StateFlow<HomeUiState> = combine(
        _listings, _searchQuery, _selectedCategory, _isLoading, _selectedListing
    ) { listings, query, category, isLoading, selectedListing ->

        // First, filter by category if one is selected
        var filtered = if (category == "All") {
            listings
        } else {
            listings.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Then, filter by search query text
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        HomeUiState(
            listings = filtered,
            isLoading = isLoading,
            selectedListing = selectedListing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {

        fetchCloudListings()
    }

    fun fetchCloudListings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val cloudData = RetrofitClient.apiService.getListings()
                _listings.value = cloudData
                _isLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun uploadNewListing(title: String, description: String, price: Double, ownerName: String, category: String, photoUri: Uri? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val newElement = Listing(
                    id = "",
                    title = title,
                    description = description,
                    pricePerDay = price,
                    ownerName = ownerName,
                    category = category, // Dynamic category passed from our dropdown input box!
                    condition = "Good"

                )

                val finalImageUrl = photoUri?.toString() ?: "https://images.unsplash.com/photo-1533090161767-e6ffed986c88?w=500"

                RetrofitClient.apiService.createListing(newElement)
                fetchCloudListings()

            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun editListingOnServer(id: String, title: String, description: String, price: Double, ownerName: String, category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val updatedElement = Listing(
                    id = id,
                    title = title,
                    description = description,
                    pricePerDay = price,
                    ownerName = ownerName,
                    category = category,
                    condition = "Good"
                )

                // Execute the PUT network call using your API service
                RetrofitClient.apiService.updateListing(id, updatedElement)

                // Clear current selection out of view state so the UI goes back home refreshed
                selectListing(null)

                // Sync local state list with cloud array
                fetchCloudListings()

            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun deleteListingFromServer(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. Tell the cloud server to delete it
                RetrofitClient.apiService.deleteListing(id)

                // 2. Clear out selection state safely
                selectListing(null)

                // Instantly slice it out of local memory so the UI updates with zero delays
                _listings.update { currentList ->
                    currentList.filter { it.id != id }
                }

                _isLoading.value = false // Done loading!

            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    fun requestRental(listingId: String, renterName: String, days: Int, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val requestPayload = Booking(
                    listingId = listingId,
                    renterName = renterName,
                    pricePerDay = uiState.value.selectedListing?.pricePerDay ?: 0.0,
                    listingTitle = uiState.value.selectedListing?.title ?: "",
                    days = days
                )

                // Execute the network call
                val result = RetrofitClient.apiService.bookListing(requestPayload)

                _isLoading.value = false
                selectListing(null) // Reset back to home screen on success

                // Invoke a callback to let the UI display a success message
                onComplete(true, "Successfully rented ${result.listingTitle.ifBlank { "Item" }} for ${result.days} days! Total: ${result.totalPrice} SAR")

            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
                onComplete(false, "Failed to complete rental booking request. Please check your network connection.")
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun selectListing(listing: Listing?) {
        _selectedListing.value = listing
    }
}