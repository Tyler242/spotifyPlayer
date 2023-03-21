package com.example.spotifyplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.spotifyplayer.utils.RecommendedUtils
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    private val CLIENT_ID = "9adc315ded0746659f31baa5226bdcbb"
    private val REDIRECT_URI = "integratespotify://callback"
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
                var listOfItems: List<ListItem> by remember {
                    mutableStateOf(emptyList())
                }
                val playerState = spotifyControl.playerApi.subscribeToPlayerState()
                playerState.setEventCallback {
                    isPaused = it.isPaused
                }
                SpotifyPlayerTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth(1f)
                                .fillMaxHeight(0.5f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            runBlocking {
                                CoroutineScope(Dispatchers.IO).launch {
                                    // TODO: get available genre seeds
                                    val genreSeeds: List<String> = getGenreSeeds(token!!)
                                    // TODO: get recommended items for LazyColumn
//                                    val url = URL("https://api.spotify.com/v1/recommendations?seed_genres=country").openConnection()
//                                    url.setRequestProperty("Authorization", "Bearer $token")
//                                    url.setRequestProperty("Content-Type", "application/json")
//                                    url.doInput = true
//                                    val inputStream = url.getInputStream()
//                                    val data = inputStream.bufferedReader().use { it.readText() }
//                                    println(data)
                                }
                            }
                        }
                        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                            itemsIndexed(listOfItems) { index, item ->
                                if (item.uri.contains("track") || item.uri.contains("playlist")) {
                                    Button(onClick = {
                                        runBlocking {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                if (item.playable) {
                                                    spotifyControl.playerApi.play(item.uri)
                                                }
                                            }
                                        }
                                    }) {
                                        Text(item.title)
                                    }
                                }
                            }
                        }
                    }
                    Column(
                        Modifier
                            .fillMaxWidth(1f)
                            .fillMaxHeight(0.5f),
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
    val url = URL("https://api.spotify.com/v1/recommendations/available-genre-seeds").openConnection() as HttpURLConnection
    url.setRequestProperty("Content-Type", "application/json")
    url.setRequestProperty("Authorization", "Bearer $token")
    url.setRequestProperty("Accept", "application/json")
    url.doInput = true
    val data = url.inputStream.bufferedReader().use { it.readText() }
    println(data)
    try {
        val jsonData: String? = Gson().toJson(data)
        println(jsonData)

    } catch(ex: Exception) {
        ex.printStackTrace()
    }

    return listOf()
}

data class Genre(
    @SerializedName("genres") var genres : ArrayList<String> = arrayListOf()
)