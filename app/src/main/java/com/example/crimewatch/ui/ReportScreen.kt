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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    // Media picker: upload only (no auto-submit)
    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            crimeViewModel.uploadMediaOnly(it)
        }
    }

    // React to submitResult changes (trigger dialogs)
    LaunchedEffect(submitResult) {
        when (submitResult) {
            null -> { /* nothing */ }
            "success" -> showSuccessDialog = true
            else -> showFailureDialog = true
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            Text(text = "Report an Incident", fontSize = 26.sp)
            Text(text = "Help your community by reporting what you see.", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Select Categories", fontSize = 18.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { showCategoryDialog = true },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = selectedCategory ?: "Choose category", modifier = Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Add Description", fontSize = 18.sp)
            OutlinedTextField(
                value = description,
                onValueChange = { crimeViewModel.description.value = it },
                placeholder = { Text("Describe what happened...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Location", fontSize = 18.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = locationText, modifier = Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { crimeViewModel.locationText.value = "Detected: Near Central Park" },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Choose your Location")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Upload Media", fontSize = 18.sp)
            Button(
                onClick = { mediaPicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("\uD83D\uDCF7 Add Photo or \uD83C\uDFA5 Add Video")
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Upload status / selected file name
            when {
                isUploading -> Text("Uploading media…", color = Color.Gray, fontSize = 14.sp)
                pickedMediaUrl != null -> Text("Selected: ${pickedMediaUrl!!.substringAfterLast('/')}", color = Color.Gray, fontSize = 14.sp)
                else -> Text("No media selected", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Submit button: disabled during upload or submit
            Button(
                onClick = { crimeViewModel.submitReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = !isUploading && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53131))
            ) {
                when {
                    isUploading -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    isSubmitting -> CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else -> Text("Submit Report", fontSize = 20.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Category dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = listOf("Municipal Complaints", "Theft", "Accident", "Harassment", "Vandalism", "Other"),
            onSelect = {
                crimeViewModel.selectedCategory.value = it
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Success dialog (pressing OK resets submitResult and navigates home)
    if (showSuccessDialog) {
        FullScreenMessageDialog(
            titleEmoji = "\u2705",
            title = "Report Submitted",
            message = "Your report has been sent for verification.\nYou’ll be notified once reviewed.",
            positiveLabel = "OK",
            onPositive = {
                showSuccessDialog = false
                // clear form
                crimeViewModel.selectedCategory.value = null
                crimeViewModel.description.value = ""
                crimeViewModel.pickedMediaUrl.value = null

                // reset submit result so dialog does not reappear
                crimeViewModel.resetSubmitResult()

                // navigate home and clear backstack of Report (if applicable)
                navHostController.navigate(CrimeAppScreen.Home.name) {
                    popUpTo(CrimeAppScreen.Report.name) { inclusive = true }
                }
            }
        )
    }

    // Failure dialog
    if (showFailureDialog) {
        FullScreenMessageDialog(
            titleEmoji = "\u26A0",
            title = "Submission Failed",
            message = submitResult ?: "Please check your internet connection.",
            positiveLabel = "RETRY",
            onPositive = {
                showFailureDialog = false
                // clear error so dialog won't re-open
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
        title = { Text("Categories", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        text = {
            Column {
                categories.forEach { cat ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(cat) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(text = cat, fontSize = 22.sp, modifier = Modifier.padding(8.dp))
                        Divider()
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/** Full screen message dialog (success / failure) */
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = titleEmoji, fontSize = 36.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 28.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        text = { Text(text = message, fontSize = 20.sp) },
        confirmButton = {
            TextButton(onClick = onPositive) {
                Text(text = positiveLabel, fontSize = 20.sp, color = Color(0xFFE53131), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        containerColor = Color.White
    )
}
