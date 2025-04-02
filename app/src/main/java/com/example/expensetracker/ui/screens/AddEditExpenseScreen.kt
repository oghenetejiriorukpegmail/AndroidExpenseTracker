package com.example.expensetracker.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import java.io.File
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.database.entity.Category
import com.example.expensetracker.ui.viewmodel.AddEditExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: AddEditExpenseViewModel = hiltViewModel(),
    // navController: NavController // Add NavController later for navigation
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.date.time // Initialize with current state
    )
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) } // For camera result

    // --- ActivityResultLaunchers ---

    // Launcher for taking a picture
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempImageUri?.let { uri ->
                    viewModel.updateReceiptImagePath(uri.toString())
                }
            }
            // Reset temp URI regardless of success
            tempImageUri = null
        }
    )

    // Launcher for selecting image from gallery
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                // Persist permission if needed for long-term access (optional)
                // val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                // context.contentResolver.takePersistableUriPermission(uri, flag)
                viewModel.updateReceiptImagePath(it.toString())
            }
        }
    )

    // Launcher for camera permission request
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, create temp file URI and launch camera
                try {
                    val photoFile = createImageFile(context)
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider", // Authority must match AndroidManifest
                        photoFile
                    )
                    tempImageUri = photoURI // Store the URI to use in the callback
                    takePictureLauncher.launch(photoURI)
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    println("ERROR: Could not create image file: ${ex.message}")
                    // Optionally show an error message to the user
                }
            } else {
                // Permission denied
                println("WARN: Camera permission denied.")
                // Optionally show a Snackbar or message explaining why the permission is needed
            }
        }
    )


    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (uiState.isEditMode) "Edit Expense" else "Add Expense") })
            // Add navigation icon (back button) later
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()), // Make column scrollable
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount Field
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::updateAmount,
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category Dropdown (Basic implementation - needs refinement)
                CategoryDropdown(
                    categories = uiState.availableCategories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::updateCategory,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Field - Clickable
                Box(modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // No ripple effect
                ) { showDatePickerDialog = true }) {
                    OutlinedTextField(
                        value = dateFormatter.format(uiState.date),
                        onValueChange = { /* Read-only */ },
                        label = { Text("Date") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") }
                    )
                }

                // Notes Field
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp) // Allow multiple lines
                )

                // --- Receipt Image Section ---
                Text("Receipt Image (Optional)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Image Preview
                if (uiState.receiptImagePath != null) {
                    AsyncImage(
                        model = uiState.receiptImagePath,
                        contentDescription = "Selected Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.updateReceiptImagePath(null) }) {
                        Text("Remove Image")
                    }

                }

                // Buttons for Image Source
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            // Check permission and launch camera
                            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Take Photo")
                    }
                    Button(
                        onClick = {
                            // Launch gallery selector
                            selectImageLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                         Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                         Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                         Text("From Gallery")
                    }
                }
                // --- End Receipt Image Section ---

                Spacer(modifier = Modifier.height(16.dp)) // Add space before error/save

                // Error Message Display
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Save Button
                Button(
                    onClick = viewModel::saveExpense,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (uiState.isEditMode) "Update Expense" else "Save Expense")
                    }
                }
            }
        }
    )

    // Date Picker Dialog
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Convert UTC millis to local Date
                            val selectedUtcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            val localCalendar = Calendar.getInstance().apply {
                                clear() // Clear current time parts
                                set(
                                    selectedUtcCalendar.get(Calendar.YEAR),
                                    selectedUtcCalendar.get(Calendar.MONTH),
                                    selectedUtcCalendar.get(Calendar.DAY_OF_MONTH)
                                )
                            }
                            viewModel.updateDate(localCalendar.time)
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Creates a temporary image file in the app's cache directory.
 */
private fun createImageFile(context: Context): File {
    // Create an image file name using timestamp
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    // Get the directory for storing cache files (defined in file_paths.xml)
    val storageDir: File? = File(context.cacheDir, "images").apply { mkdirs() } // Ensure directory exists
    return File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}

// Basic Dropdown for Categories - Needs improvement for better UX
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Select Category",
            onValueChange = {}, // Read-only
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth() // Important for anchoring the dropdown
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}