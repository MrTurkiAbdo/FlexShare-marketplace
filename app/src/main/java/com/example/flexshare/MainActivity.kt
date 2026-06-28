package com.example.flexshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flexshare.presentation.AddListingScreen
import com.example.flexshare.presentation.DetailScreen
import com.example.flexshare.presentation.HomeViewModel
import com.example.flexshare.presentation.ListingCard
import com.example.flexshare.ui.theme.FlexShareTheme


enum class FormMode { CREATE, EDIT }
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlexShareTheme {
                val state by viewModel.uiState.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val selectedCategory by viewModel.selectedCategory.collectAsState()

                var isShowingForm by remember { mutableStateOf(false) }
                var currentFormMode by remember { mutableStateOf(FormMode.CREATE) }

                var isShowingAddForm by remember { mutableStateOf(false) }

                // Define our matching list of categories
                val categories = listOf("All", "Tools", "Garden", "Electronics", "Automotive")

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                else if (isShowingForm) {
                    AddListingScreen(
                        existingListing = if (currentFormMode == FormMode.EDIT) state.selectedListing else null,
                        onBackClick = { isShowingForm = false },
                        onSaveClick = { title, description, price, ownerName, category, photoUri ->
                            if (currentFormMode == FormMode.EDIT) {
                                viewModel.editListingOnServer(state.selectedListing!!.id, title, description, price, ownerName, category)
                            } else {
                                viewModel.uploadNewListing(title, description, price, ownerName, category, photoUri)
                            }
                            isShowingForm = false
                        }
                    )
            } else if (state.selectedListing == null) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("FlexShare Market", fontWeight = FontWeight.ExtraBold) }
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    currentFormMode = FormMode.CREATE
                                    isShowingForm = true },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add listing")
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // 1. Search Bar Layout
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                placeholder = { Text("Search tools, gear, items...") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium
                            )

                            // 2. Horizontally Scrollable Category Pills Row
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { categoryName ->
                                    FilterChip(
                                        selected = selectedCategory == categoryName,
                                        onClick = { viewModel.onCategorySelected(categoryName) },
                                        label = { Text(text = categoryName) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            // 3. Main Catalog Feed List
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.listings) { individualListing ->
                                    ListingCard(
                                        listing = individualListing,
                                        onItemClick = { viewModel.selectListing(individualListing) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    DetailScreen(
                        listing = state.selectedListing!!,
                        onBackClick = { viewModel.selectListing(null) },
                        onDeleteClick = { id -> viewModel.deleteListingFromServer(id) },
                        onEditClick = {
                            // 🔻 THIS TELLS THE APP TO OPEN THE FORM PRE-POPULATED!
                            currentFormMode = FormMode.EDIT
                            isShowingForm = true
                        },
                        onBookClick = { renterName, days -> // 👈 ADD THIS CALLBACK BLOCK
                            viewModel.requestRental(
                                listingId = state.selectedListing!!.id,
                                renterName = renterName,
                                days = days,
                                onComplete = { success, message ->
                                    // Show a quick native alert notification to the user
                                    android.widget.Toast.makeText(this@MainActivity, message, android.widget.Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}