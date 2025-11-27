package com.example.crimewatch.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.crimewatch.data.supabase.ReportDto

@Composable
fun MyReportScreen(
    crimeViewModel: CrimeViewModel,
    navHostController: NavHostController
) {
    LaunchedEffect(crimeViewModel.user) {
        crimeViewModel.loadMyReports()
    }

    val myReports by crimeViewModel.myReports.collectAsState()
    val loading by crimeViewModel.isLoadingMyReports.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2196F3))
            }
            return@Box
        }

        if (myReports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No reports yet", fontWeight = FontWeight.Medium)
            }
            return@Box
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            // Reserve space at top so items don't appear under the TopAppBar.
            // 80.dp is a safe height (top appbar + some margin). Adjust if your appbar height differs.
            contentPadding = PaddingValues(top = 85.dp, bottom = 16.dp)
        ) {

            items(myReports, key = { it.id }) { report ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navHostController.navigate("ReportDetails/${report.id}") }
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {

                    Text(
                        text = report.category ?: "Untitled",
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 🕒 Human friendly time
                    Text(
                        text = formatTimeAgo(report.created_at),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "View details",
                            color = Color(0xFF007AFF)
                        )
                    }
                }

                Divider()
            }
        }
    }
}
