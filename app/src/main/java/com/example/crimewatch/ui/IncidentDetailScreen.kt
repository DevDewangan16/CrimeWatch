package com.example.crimewatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext

@Composable
fun IncidentDetailScreen(
    reportId: String,
    reportsViewModel: ReportsViewModel,
    navHostController: NavHostController
) {
    LaunchedEffect(reportId) {
        reportsViewModel.refreshReports()
        reportsViewModel.loadComments(reportId)
        reportsViewModel.loadVotes(reportId)
    }

    val reports by reportsViewModel.reports.collectAsState()
    val commentsMap by reportsViewModel.comments.collectAsState()
    val votesMap by reportsViewModel.votes.collectAsState()

    val report = reports.firstOrNull { it.id == reportId }
    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", color = Color.Gray)
        }
        return
    }

    var commentText by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Incident Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        report?.let { r ->
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val ctx = LocalContext.current
                    if (!r.media_url.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx).data(r.media_url).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Reported by: ${r.user_id ?: "Anonymous"}", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Description: ${r.description ?: ""}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Time: ${r.created_at ?: ""}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Location: ${r.location ?: ""}")
                    Spacer(modifier = Modifier.height(10.dp))

                    val votesList = votesMap[reportId] ?: emptyList()
                    val upvotes = votesList.count { it.vote_type == "upvote" }
                    val fakeVotes = votesList.count { it.vote_type == "fake" }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            currentUser?.let {
                                reportsViewModel.postVote(reportId, it.uid, "upvote") {}
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) {
                            Text("Upvote ($upvotes)", color = Color.White)
                        }

                        Button(onClick = {
                            currentUser?.let {
                                reportsViewModel.postVote(reportId, it.uid, "fake") {}
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))) {
                            Text("Fake ($fakeVotes)", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "\uD83D\uDCAC Comments", fontWeight = FontWeight.SemiBold)
            val comments = commentsMap[reportId] ?: emptyList()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                comments.forEach { c ->
                    Text(text = "${c.user_id}: \"${c.comment}\"")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            BasicTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                decorationBox = { inner ->
                    if (commentText.isEmpty()) Text("Add A Comment...")
                    inner()
                }
            )

            Button(onClick = {
                val uid = currentUser?.uid ?: "anonymous"
                if (commentText.isNotBlank()) {
                    reportsViewModel.postComment(reportId, uid, commentText) { ok ->
                        if (ok) commentText = ""
                    }
                }
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53131))) {
                Text("SEND", color = Color.White)
            }
        } ?: run {
            Text("Report not found", color = Color.Gray)
        }
    }
}
