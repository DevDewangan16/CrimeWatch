package com.example.crimewatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    reportsViewModel: ReportsViewModel = viewModel(),
    navHostController: NavHostController
) {
    val reports by reportsViewModel.reports.collectAsState()
    val isLoading by reportsViewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(text = "Recent Incidents Near You", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
            items(reports) { report ->
                ReportCard(report = report,navHostController=navHostController)
            }
        }
    }
}

@Composable
fun ReportCard(report: com.example.crimewatch.data.supabase.ReportDto,navHostController: NavHostController) {
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val ctx = LocalContext.current
                if (!report.media_url.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(report.media_url).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFEFEFEF)))
                }


                Column(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp), horizontalAlignment = Alignment.End) {
                    Text(text = "Badge: Verified", color = Color.White, modifier = Modifier.background(Color(0xFF2E7D32), shape = RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "View Detail...", color = Color(0xFF2196F3), modifier = Modifier.clickable {
                        report.id?.let { id ->
                            navHostController.navigate("ReportDetails/$id")
                        }
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = report.category ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = report.description ?: "", maxLines = 2)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = report.location ?: "", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = report.created_at?.substringBefore("T") ?: "", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
