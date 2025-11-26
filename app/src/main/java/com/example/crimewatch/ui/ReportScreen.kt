// ReportScreen.kt
package com.example.crimewatch.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

/**
 * ReportScreen (ViewModel-driven)
 *
 * - Binds to CrimeViewModel state flows
 * - Calls crimeViewModel.submitReport() to push data (e.g., to Supabase)
 * - Shows success / failure dialogs based on crimeViewModel.submitResult
 *
 * Note: Replace media picking placeholder behavior by uploading to Supabase Storage
 * and setting the returned public URL into crimeViewModel.pickedMediaUrl.value
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    crimeViewModel: CrimeViewModel,
    navHostController: NavHostController
) {
    // Collect ViewModel state
    val selectedCategory by crimeViewModel.selectedCategory.collectAsState()
    val description by crimeViewModel.description.collectAsState()
    val locationText by crimeViewModel.locationText.collectAsState()
    val pickedMediaUrl by crimeViewModel.pickedMediaUrl.collectAsState()

    val isSubmitting by crimeViewModel.isSubmitting.collectAsState()
    val submitResult by crimeViewModel.submitResult.collectAsState()

    // Local UI-only state
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Media picker (returns Uri). For now we store uri.toString() in ViewModel as placeholder.
    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // TODO: upload the Uri to Supabase storage and set the returned public URL into ViewModel:
            // crimeViewModel.pickedMediaUrl.value = uploadedPublicUrl
            // For quick testing, we store the uri string as placeholder:
            crimeViewModel.pickedMediaUrl.value = uri.toString()
        }
    }

    // Categories (can be moved to ViewModel if you want)
    val categories = listOf(
        "Municipal Complaints",
        "Theft",
        "Accident",
        "Harassment",
        "Vandalism",
        "Other"
    )

    // React to submitResult from ViewModel: show dialogs accordingly
    LaunchedEffect(submitResult) {
        when (submitResult) {
            null -> {
                // nothing
            }
            "success" -> {
                showSuccessDialog = true
                // clear result in viewModel if you like (optional)
                // crimeViewModel.submitResult.value = null  // avoid if submitResult is StateFlow read-only
            }
            else -> {
                // any non-null non-"success" text treated as error message
                showFailureDialog = true
            }
        }
    }

    // Main UI
    Scaffold { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState)
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(text = "Report an Incident", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(text = "Help your community by reporting what you see.", fontSize = 16.sp)

            // Category chooser
            Text(text = "Select Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .borderRounded(red = true)
                    .clickable { showCategoryDialog = true },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = selectedCategory ?: "Choose category",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 18.sp,
                    color = if (selectedCategory == null) Color.Gray else Color.Black
                )
            }

            // Description
            Text(text = "Add Description", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .borderRounded(red = true)
                    .padding(6.dp)
            ) {
                TextField(
                    value = description,
                    onValueChange = { crimeViewModel.description.value = it },
                    placeholder = { Text(text = "Describe what happened...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
                )
            }

            // Location
            Text(text = "Location", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .borderRounded(red = true),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = locationText, modifier = Modifier.padding(horizontal = 16.dp), fontSize = 16.sp)
            }

            Button(
                onClick = {
                    // Hook real location logic here (FusedLocationProvider / Map)
                    crimeViewModel.locationText.value = "Detected: Near Central Park"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AA76C))
            ) {
                Text(text = "Choose your Location", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Upload media
            Text(text = "Upload Media", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { mediaPicker.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AA76C))
            ) {
                Text(text = "\uD83D\uDCF7 Add Photo or \uD83C\uDFA5 Add Video", fontSize = 18.sp, color = Color.White)
            }
            pickedMediaUrl?.let { uriStr ->
                Text(text = "Selected: ${uriStr.substringAfterLast('/')}", fontSize = 12.sp, color = Color.Gray)
            }

            // Submit
            Button(
                onClick = {
                    // client-side validation: ensure category and description
                    if (selectedCategory.isNullOrBlank() || description.isBlank()) {
                        // mark failure locally by setting an error result in ViewModel (or show snack)
                        crimeViewModel.submitReport() // submitReport handles validation and will update submitResult
                        return@Button
                    }
                    crimeViewModel.submitReport()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53131))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = "Submit Report", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // ensure content isn't hidden by bottom bar
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Category dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = categories,
            onSelect = {
                crimeViewModel.selectedCategory.value = it
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Success dialog (driven by submitResult=="success")
    if (showSuccessDialog) {
        FullScreenMessageDialog(
            titleEmoji = "\u2705",
            title = "Report Submitted",
            message = "Your report has been sent for verification.\nYou’ll be notified once reviewed.",
            positiveLabel = "OK",
            onPositive = {
                showSuccessDialog = false
                // optionally clear form
                crimeViewModel.selectedCategory.value = null
                crimeViewModel.description.value = ""
                crimeViewModel.pickedMediaUrl.value = null
                // navigate home
                navHostController.navigate(CrimeAppScreen.Home.name) {
                    popUpTo(CrimeAppScreen.Report.name) { inclusive = true }
                }
            }
        )
    }

    // Failure dialog (driven by submitResult != null && != "success")
    if (showFailureDialog) {
        FullScreenMessageDialog(
            titleEmoji = "\u26A0",
            title = "Submission Failed",
            message = submitResult ?: "Please check your internet connection.",
            positiveLabel = "RETRY",
            onPositive = {
                showFailureDialog = false
                // allow user to retry; submitReport() will re-run when triggered
            }
        )
    }
}

/** Category dialog - same as your existing design */
@Composable
fun CategorySelectionDialog(
    categories: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Categories", fontWeight = FontWeight.Bold) },
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
                        androidx.compose.material3.Divider()
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = titleEmoji, fontSize = 36.sp)
                Spacer(modifier = androidx.compose.ui.Modifier.size(8.dp))
                Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = { Text(text = message, fontSize = 20.sp) },
        confirmButton = {
            TextButton(onClick = onPositive) {
                Text(text = positiveLabel, fontSize = 20.sp, color = Color(0xFFE53131), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White
    )
}

/** Rounded border modifier used in UI */
fun Modifier.borderRounded(red: Boolean = false): Modifier {
    val color = if (red) Color(0xFFE53131) else Color.Gray
    return this
        .clip(RoundedCornerShape(12.dp))
        .border(width = 2.dp, color = color, shape = RoundedCornerShape(12.dp))
}
