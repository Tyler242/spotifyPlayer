package com.example.spotifyplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    Column(){
                        Header()
                        Column(modifier = Modifier.height(400.dp)){
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val navigate = Intent(this@PlayList, TwoMainButtons::class.java)
                                    startActivity(navigate)
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowBack,
                                        contentDescription = "Click to share",
                                        tint = Color.Red
                                    )
                                }
                                RecyclerView()
                                IconButton(onClick = {
                                    val navigate = Intent(this@PlayList, GenreUI::class.java)
                                    startActivity(navigate)
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowForward,
                                        contentDescription = "Click to share",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                        BottomPlay()
                    }
                }
            }
        }
    }
}


@Composable
fun Page3(title: String, author: String){

    Box(modifier = Modifier
        .width(320.dp)
        .padding(10.dp)){
        Row(modifier = Modifier
            .background(color = Color.LightGray, shape = RectangleShape)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center){
            Text(text = title)
            Spacer(Modifier.width(10.dp))
            Text(text = author)
            Spacer(Modifier.width(10.dp))
            OutlinedButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.AddBox,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun RecyclerView(names : List<String> = List(1000){"$it"}){

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center){
        LazyColumn(){
            items(items = names){
                    name -> Page3(title = name, author = name)
            }
        }
    }
}