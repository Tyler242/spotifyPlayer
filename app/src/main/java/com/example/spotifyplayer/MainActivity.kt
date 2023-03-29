package com.example.spotifyplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spotifyplayer.ui.theme.SpotifyPlayerTheme
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Artist
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val CLIENT_ID = "9adc315ded0746659f31baa5226bdcbb"
    private val REDIRECT_URI = "spotifyplayer://callback"
    private var token: String? = null
    private val context = this
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data!!
                val bundle: Bundle = intent.getBundleExtra("EXTRA_AUTH_RESPONSE")!!
                if (bundle.containsKey("response")) {
                    val data: AuthorizationResponse? =
                        (bundle.get("response") as AuthorizationResponse?)
                    token = data!!.accessToken
                    println("Access token: $token")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                val builder =
                    AuthorizationRequest.Builder(
                        CLIENT_ID,
                        AuthorizationResponse.Type.TOKEN,
                        REDIRECT_URI
                    ).setScopes(
                        arrayOf(
                            "user-read-private",
                            "user-read-recently-played",
                            "user-top-read",
                            "user-read-currently-playing",
                            "playlist-read-collaborative",
                            "playlist-read-private"
                        )
                    )
                val request = builder.build()
                startForResult.launch(
                    Intent(
                        AuthorizationClient.createLoginActivityIntent(
                            context,
                            request
                        )
                    )
                )
            }
        }

        setContent {
            SpotifyPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Text("Connecting to Spotify")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams
            .Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()
        val context = this

        SpotifyAppRemote.connect(context, connectionParams, ConnectListener { spotifyControl ->
            setContent {
                var isPaused by remember {
                    mutableStateOf(false)
                }
                val listOfTracks = remember {
                    mutableStateListOf<Track>()
                }
                val playerState = spotifyControl.playerApi.subscribeToPlayerState()
                playerState.setEventCallback {
                    isPaused = it.isPaused
                }
                var isFirstLoad by remember {
                    mutableStateOf(true)
                }
                val listState = rememberLazyListState()
                fun LazyListState.isScrolledToEnd(): Boolean {
                    return layoutInfo.visibleItemsInfo.lastOrNull()?.index == listOfTracks.size - 1
                }

                if (listState.isScrolledToEnd() || isFirstLoad) {
                    isFirstLoad = false
                    runBlocking {
                        CoroutineScope(Dispatchers.IO).launch {
                            // TODO: get available genre seeds
                            val genreSeeds: List<String> = getGenreSeeds(token!!)
                            // TODO: get recommended items for LazyColumn
                            val genre = genreSeeds[Random.nextInt(0, genreSeeds.size - 1)]
                            listOfTracks.addAll(getTracks(genre, token!!))
                        }
                    }
                }

                SpotifyPlayerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth(1f)
                                .height(100.dp)
                                .background(Color.LightGray),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (listOfTracks.size > 0) {
                                Text("${listOfTracks[0].name}")
                                Text("Artist")
                            } else Text("Loading tracks")
                        }
                        Column(
                            Modifier
                                .fillMaxWidth(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            LazyColumn(
                                Modifier
                                    .fillMaxWidth(1f)
                                    .height(400.dp)
                                    .background(Color.Cyan),
                                state = listState,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                itemsIndexed(listOfTracks) { index, item ->
                                    if (item.uri.contains("track")) {
                                        Button(onClick = {
                                            runBlocking {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    spotifyControl.playerApi.play(item.uri)
                                                }
                                            }
                                        }) {
                                            Text(item.name)
                                        }
                                    }
                                }
                            }
                        }
                        Column(
                            Modifier
                                .fillMaxWidth(1f)
                                .height(50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth(1f)
                                    .height(50.dp)
                                    .background(Color.LightGray),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(onClick = {
                                    runBlocking {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            if (isPaused) {
                                                spotifyControl.playerApi.resume()
                                            } else {
                                                spotifyControl.playerApi.pause()
                                            }
                                        }
                                    }
                                }) {
                                    if (isPaused) {
                                        Text("Play")
                                    } else {
                                        Text("Pause")
                                    }
                                }
                                Button(onClick = {
                                    runBlocking {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val genreSeeds: List<String> = getGenreSeeds(token!!)
                                            // TODO: get recommended items for LazyColumn
                                            val genre =
                                                genreSeeds[Random.nextInt(0, genreSeeds.size - 1)]
                                            listOfTracks.clear()
                                            listOfTracks.addAll(getTracks(genre, token!!))
                                        }
                                    }
                                }) {
                                    Text("Refresh")
                                }
                                Button(onClick = {
                                    runBlocking {
                                        CoroutineScope(Dispatchers.Default).launch {
                                            listOfTracks.removeAt(0)
                                            val nextTrack = listOfTracks[0]
                                            spotifyControl.playerApi.play(nextTrack.uri)
                                        }
                                    }
                                }) {
                                    Text("Next")
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}


class ConnectListener(val onConnectCallback: (SpotifyAppRemote) -> Unit) :
    Connector.ConnectionListener {
    override fun onConnected(spotifyAppRemote: SpotifyAppRemote?) {
        println("Connected! Yay!")
        try {
            onConnectCallback(spotifyAppRemote!!)
        } catch (ex: Exception) {
            println("ERROR")
            ex.printStackTrace()
        }
    }

    override fun onFailure(error: Throwable?) {
        if (error != null) {
            println(error.message!!)
        }
    }
}

fun getGenreSeeds(token: String): List<String> {
    val url =
        URL("https://api.spotify.com/v1/recommendations/available-genre-seeds").openConnection() as HttpURLConnection
    url.setRequestProperty("Content-Type", "application/json")
    url.setRequestProperty("Authorization", "Bearer $token")
    url.setRequestProperty("Accept", "application/json")
    url.doInput = true
    val data = url.inputStream.bufferedReader().use { it.readText() }
    val genre: Genre

    try {
        genre = Gson().fromJson(data, Genre::class.java)
        return genre.genres
    } catch (ex: Exception) {
        throw ex
    }
}

fun getTracks(genre: String, token: String): List<Track> {
    val artistSeed: String = "4NHQUGzhtTLFvgF5SZesLK"
    val trackSeed: String = "0c6xIDDpzE81m2q797ordA"
    val url =
        URL("https://api.spotify.com/v1/recommendations?seed_genres=$genre&seed_artists=$artistSeed&seed_tracks=$trackSeed").openConnection() as HttpURLConnection
    url.setRequestProperty("Content-Type", "application/json")
    url.setRequestProperty("Accept", "application/json")
    url.setRequestProperty("Authorization", "Bearer $token")
    url.doInput = true
    val data = url.inputStream.bufferedReader().use { it.readText() }
    val recommendation: Recommendation

    try {
        recommendation = Gson().fromJson(data, Recommendation::class.java)
        return recommendation.tracks
    } catch (ex: Exception) {
        throw ex
    }
}

fun getCurrentlyPlaying(token: String): Track {
    val currentPlaying: CurrentPlaying
    val endpoint = "https://api.spotify.com/v1/me/player/currently-playing"
    val url = URL(endpoint).openConnection() as HttpURLConnection
    url.setRequestProperty("Content-Type", "application/json")
    url.setRequestProperty("Accept", "application/json")
    url.setRequestProperty("Authorization", "Bearer $token")
    url.doInput = true
    val data = url.inputStream.bufferedReader().use { it.readText() }
    println(data)

    try {
        currentPlaying = Gson().fromJson(data, CurrentPlaying::class.java)
        return currentPlaying.item
    } catch (ex: Exception) {
        throw ex
    }
}

data class Genre(
    @SerializedName("genres") var genres: ArrayList<String> = arrayListOf()
)

data class Recommendation(
    @SerializedName("seeds") var seeds: ArrayList<Any> = arrayListOf(),
    @SerializedName("tracks") var tracks: ArrayList<Track> = arrayListOf()
)

data class Track(
    @SerializedName("album") var album: Any,
    @SerializedName("artists") var artists: ArrayList<Artist> = arrayListOf(),
    @SerializedName("name") var name: String = "",
    @SerializedName("track_number") var trackNumber: Int,
    @SerializedName("type") var type: String = "",
    @SerializedName("uri") var uri: String = ""
)

data class CurrentPlaying(
    @SerializedName("item") var item: Track
)