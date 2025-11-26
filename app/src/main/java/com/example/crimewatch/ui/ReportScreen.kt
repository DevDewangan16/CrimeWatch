// ReportScreen.kt
package com.example.crimewatch.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.crimewatch.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.navigationBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    crimeViewModel: CrimeViewModel,
    navHostController: NavHostController
) {
    // State
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("Detected: (not set)") }
    var pickedMediaUri by remember { mutableStateOf<Uri?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Media picker
    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) pickedMediaUri = uri
    }

    val categories = listOf(
        "Municipal Complaints",
        "Theft",
        "Accident",
        "Harassment",
        "Vandalism",
        "Other"
    )

    // Scaffold + scrollable content
    Scaffold { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(innerPadding)                   // respect scaffold insets
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(scrollState)              // enable scrolling
                .fillMaxWidth()
                .navigationBarsPadding(),                 // keep above navigation bars
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(text = "Report an Incident", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(text = "Help your community by reporting what you see.", fontSize = 16.sp)

            // Category
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
                    onValueChange = { description = it },
                    placeholder = { Text(text = "Describe what happened...") },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent
                    )
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
                    // placeholder location detection
                    locationText = "Detected: Near Central Park"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF3AA76C))
            ) {
                Text(text = "Choose your Location", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Upload
            Text(text = "Upload Media", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = {
                    mediaPicker.launch("*/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF3AA76C))
            ) {
                Text(text = "\uD83D\uDCF7 Add Photo or \uD83C\uDFA5 Add Video", fontSize = 18.sp, color = Color.White)
            }
            pickedMediaUri?.let { uri ->
                Text(text = "Selected: ${uri.lastPathSegment ?: uri}", fontSize = 12.sp, color = Color.Gray)
            }

            // Submit
            Button(
                onClick = {
                    if (selectedCategory.isNullOrBlank()) {
                        showFailureDialog = true
                        return@Button
                    }
                    isSubmitting = true
                    coroutineScope.launch {
                        val success = simulateSubmit(
                            category = selectedCategory!!,
                            description = description,
                            location = locationText,
                            media = pickedMediaUri
                        )
                        isSubmitting = false
                        if (success) showSuccessDialog = true else showFailureDialog = true
                    }
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

            // Extra bottom spacer so content doesn't get hidden by bottom bar
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Category dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            categories = categories,
            onSelect = {
                selectedCategory = it
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Success / Failure
    if (showSuccessDialog) {
        FullScreenMessageDialog(
            titleEmoji = "\u2705",
            title = "Report Submitted",
            message = "Your report has been sent for verification.\nYou’ll be notified once reviewed.",
            positiveLabel = "OK",
            onPositive = {
                showSuccessDialog = false
                navHostController.navigate(CrimeAppScreen.Home.name) {
                    popUpTo(CrimeAppScreen.Report.name) { inclusive = true }
                }
            }
        )
    }

    if (showFailureDialog) {
        FullScreenMessageDialog(
            titleEmoji = "\u26A0",
            title = "Submission Failed",
            message = "Please check your internet connection.",
            positiveLabel = "RETRY",
            onPositive = { showFailureDialog = false }
        )
    }
}

/** Dialog: categories list */
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
                Spacer(modifier = Modifier.size(8.dp))
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

/** Simulated submit (replace with real network call) */
suspend fun simulateSubmit(
    category: String,
    description: String,
    location: String,
    media: Uri?
): Boolean {
    delay(1400L)
    return description.isNotBlank()
}

/** Rounded border modifier */
fun Modifier.borderRounded(red: Boolean = false): Modifier {
    val color = if (red) Color(0xFFE53131) else Color.Gray
    return this
        .clip(RoundedCornerShape(12.dp))
        .border(width = 2.dp, color = color, shape = RoundedCornerShape(12.dp))
}
