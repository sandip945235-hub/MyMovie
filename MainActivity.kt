package com.sandip.mymovie

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen() }
    }
}

@Composable
fun HomeScreen() {
    var allMovies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            allMovies = CsvRepository.fetchMovies()
            loading = false
        }
    }

    val filtered = remember(query, allMovies) {
        if (query.isBlank()) allMovies
        else allMovies.filter { it.title.contains(query, ignoreCase = true) }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "MyMovie",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("फ़िल्म खोजें...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp), // फ़ोन पर ~3, टैबलेट पर ~4
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { movie ->
                    Column(
                        Modifier
                            .padding(6.dp)
                            .clickableMovie {
                                val intent = Intent(context, PlayerActivity::class.java)
                                intent.putExtra("title", movie.title)
                                intent.putExtra("link", movie.playableLink())
                                context.startActivity(intent)
                            }
                    ) {
                        AsyncImage(
                            model = movie.poster,
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(2f / 3f)
                        )
                        Text(
                            movie.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// क्लिक करने योग्य मॉडिफ़ायर हेल्पर
private fun Modifier.clickableMovie(onClick: () -> Unit): Modifier =
    this.then(Modifier.let {
        androidx.compose.foundation.clickable(onClick = onClick)
    })
