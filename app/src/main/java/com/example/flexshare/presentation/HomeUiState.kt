package com.example.flexshare.presentation

import com.example.flexshare.domain.model.Listing

data class HomeUiState(
    val listings: List<Listing> = emptyList(),
    val selectedListing: Listing? = null,
    val isLoading: Boolean = false
)