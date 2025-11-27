// ReportScreen.kt
package com.example.crimewatch.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    crimeViewModel: CrimeViewModel,
    navHostController: NavHostController
) {
    // ViewModel-backed state
    val selectedCategory by crimeViewModel.selectedCategory.collectAsState()
    val description by crimeViewModel.description.collectAsState()
    val locationText by crimeViewModel.locationText.collectAsState()
    val pickedMediaUrl by crimeViewModel.pickedMediaUrl.collectAsState()
    val isUploading by crimeViewModel.isUploading.collectAsState()
    val isSubmitting by crimeViewModel.isSubmitting.collectAsState()
    val submitResult by crimeViewModel.submitResult.collectAsState()

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }

    // Media picker
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            crimeViewModel.uploadMediaOnly(it)
        }
    }

    // React to submitResult changes
    LaunchedEffect(submitResult) {
        when (submitResult) {
            null -> { /* nothing */ }
            "success" -> showSuccessDialog = true
            else -> showFailureDialog = true
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            // Header Section
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Spacer(modifier = Modifier.fillMaxWidth().height(45.dp))
                Text(
                    text = "Help your community by reporting what you see",
                    fontSize = 15.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Category Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Category",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF5F5F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategory ?: "Choose category",
                                fontSize = 15.sp,
                                color = if (selectedCategory == null) Color(0xFF999999) else Color(0xFF1A1A1A)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF999999)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { crimeViewModel.description.value = it },
                        placeholder = {
                            Text(
                                "Describe what happened in detail...",
                                color = Color(0xFF999999)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA)
                        ),
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Location",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF5F5F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = locationText,
                                fontSize = 15.sp,
                                color = if (locationText == "Location not set") Color(0xFF999999) else Color(0xFF1A1A1A),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { crimeViewModel.locationText.value = "Detected: Near Central Park" },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Detect My Location",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media Upload Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Media (Optional)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    OutlinedButton(
                        onClick = { mediaPicker.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2196F3)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            Color(0xFF2196F3)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Photo or Video",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upload status
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isUploading -> Color(0xFFFFF3E0)
                            pickedMediaUrl != null -> Color(0xFFE8F5E9)
                            else -> Color(0xFFF5F5F5)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isUploading -> Icons.Default.Add
                                    pickedMediaUrl != null -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when {
                                    isUploading -> Color(0xFFFF9800)
                                    pickedMediaUrl != null -> Color(0xFF4CAF50)
                                    else -> Color(0xFF999999)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when {
                                    isUploading -> "Uploading media..."
                                    pickedMediaUrl != null -> "Media uploaded: ${pickedMediaUrl!!.substringAfterLast('/')}"
                                    else -> "No media selected"
                                },
                                fontSize = 13.sp,
                                color = when {
                                    isUploading -> Color(0xFFE65100)
                                    pickedMediaUrl != null -> Color(0xFF2E7D32)
                                    else -> Color(0xFF666666)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { crimeViewModel.submitReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isUploading && !isSubmitting && selectedCategory != null && description.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    disabledContainerColor = Color(0xFFBDBDBD)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp,
                    disabledElevation = 0.dp
                )
            ) {
                when {
                    isUploading || isSubmitting -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isUploading) "Uploading..." else "Submitting...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Submit Report",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Category dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = listOf(
                "Municipal Complaints",
                "Theft",
                "Accident",
                "Harassment",
                "Vandalism",
                "Other"
            ),
            onSelect = {
                crimeViewModel.selectedCategory.value = it
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Success dialog
    if (showSuccessDialog) {
        FullScreenMessageDialog(
            titleEmoji = "✅",
            title = "Report Submitted",
            message = "Your report has been sent for verification.\nYou'll be notified once reviewed.",
            positiveLabel = "OK",
            onPositive = {
                showSuccessDialog = false
                crimeViewModel.selectedCategory.value = null
                crimeViewModel.description.value = ""
                crimeViewModel.pickedMediaUrl.value = null
                crimeViewModel.resetSubmitResult()
                navHostController.navigate(CrimeAppScreen.Home.name) {
                    popUpTo(CrimeAppScreen.Report.name) { inclusive = true }
                }
            }
        )
    }

    // Failure dialog
    if (showFailureDialog) {
        FullScreenMessageDialog(
            titleEmoji = "⚠️",
            title = "Submission Failed",
            message = submitResult ?: "Please check your internet connection.",
            positiveLabel = "RETRY",
            onPositive = {
                showFailureDialog = false
                crimeViewModel.resetSubmitResult()
            }
        )
    }
}

/** Category selection dialog */
@Composable
fun CategorySelectionDialog(
    categories: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Select Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                categories.forEach { cat ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(cat) },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent
                    ) {
                        Text(
                            text = cat,
                            fontSize = 16.sp,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        )
                    }
                    if (cat != categories.last()) {
                        Divider(color = Color(0xFFEEEEEE))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF666666))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/** Full screen message dialog */
@Composable
fun FullScreenMessageDialog(
    titleEmoji: String,
    title: String,
    message: String,
    positiveLabel: String,
    onPositive: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* block */ },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = titleEmoji,
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
        },
        text = {
            Text(
                text = message,
                fontSize = 16.sp,
                color = Color(0xFF666666),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onPositive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = positiveLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}