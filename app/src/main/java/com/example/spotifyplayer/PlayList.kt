package com.example.spotifyplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spotifyplayer.ui.theme.SpotifyPlayerTheme

class PlayList : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpotifyPlayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Header()

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val navigate = Intent(this@PlayList, PlayList2::class.java)
                                startActivity(navigate)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowBack,
                                    contentDescription = "Click to share",
                                    tint = Color.Red
                                )
                            }
                            TwoButton()
                            IconButton(onClick = {
                                val navigate = Intent(this@PlayList, PlayList2::class.java)
                                startActivity(navigate)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowForward,
                                    contentDescription = "Click to share",
                                    tint = Color.Red
                                )
                            }
                        }
                        Box(contentAlignment = Alignment.BottomCenter){
                            BottomPlay()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TwoButton(){
    Column(
        modifier = Modifier.height(400.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("Create a Playlist",
            modifier = Modifier.padding(30.dp),
            textAlign = TextAlign.Justify
        )
        Row(){
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
                    .border(border = BorderStroke(10.dp, color = Color.Black))
            ) {
                Button(onClick = {

                }) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(50.dp))

            Box(
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
                    .border(border = BorderStroke(10.dp, color = Color.Black))
            ) {
                Button(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun Header() {
    Column(
        Modifier
            .height(300.dp)
            .fillMaxSize(1f)
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(75.dp))
        AsyncImage(
            model = "http://placekitten.com/200/300",
            contentDescription = "Cat Picture",
            modifier = Modifier.height(150.dp),
            alignment = Alignment.Center
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Song Title Here",
            modifier = Modifier
                .height(35.dp)
                .border(1.dp, Color.Blue, RectangleShape),
            fontSize = 24.sp
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
fun BottomPlay(){
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .background(color = Color.LightGray),
        contentAlignment = Alignment.BottomCenter
    ){
        Text(text = "hello world")
    }
}