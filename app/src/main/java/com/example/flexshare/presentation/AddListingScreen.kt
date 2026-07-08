package com.example.flexshare.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.flexshare.domain.model.Listing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingScreen(
    existingListing: Listing?,
    onBackClick: () -> Unit,
    // 🚀 Callback signature updated to accept Uri? as the 6th argument
    onSaveClick: (String, String, Double, String, String, Uri?) -> Unit
) {
    // Pre-populate fields if editing, otherwise start fresh
    var title by remember { mutableStateOf(existingListing?.title ?: "") }
    var description by remember { mutableStateOf(existingListing?.description ?: "") }
    var price by remember { mutableStateOf(existingListing?.pricePerDay?.toString() ?: "") }
    var ownerName by remember { mutableStateOf(existingListing?.ownerName ?: "") }

    // 🟢 Defaulting to "Electronics" as our default selected dropdown value
    var category by remember { mutableStateOf(existingListing?.category ?: "Electronics") }

    // 🖼️ State to track selected local photo Uri
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 🚀 Safe system gallery contract picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingListing != null) "Edit Listing" else "List an Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🖼️ Display Box Frame for Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedImageUri)
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = "Selected Photo Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (existingListing?.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(existingListing.imageUrl)
                            .crossfade(true)
                            .allowHardware(false)
                            .build(),
                        contentDescription = "Current Server Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "No Photo Selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(if (selectedImageUri == null && existingListing?.imageUrl == null) "Select Photo" else "Change Photo")
            }

            // Input Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price per day (SAR)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth()
            )

            ItemTypeDropdown(
                selectedType = category,
                onTypeSelected = { chosenCategory ->
                    category = chosenCategory
                }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val parsedPrice = price.toDoubleOrNull() ?: 0.0
                    // 🚀 Packages fields along with the chosen image URI back to MainActivity
                    onSaveClick(title, description, parsedPrice, ownerName, category, selectedImageUri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Publish Listing", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    // These match the categories you filter by in your HomeViewModel
    val categories = listOf("Electronics", "Vehicles", "Tools", "Books", "Services")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true, // Lock keyboard input so they MUST choose from your list
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category) },
                    onClick = {
                        onTypeSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}