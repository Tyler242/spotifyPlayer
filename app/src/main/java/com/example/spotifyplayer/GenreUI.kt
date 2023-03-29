package com.example.spotifyplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spotifyplayer.ui.theme.SpotifyPlayerTheme

class GenreUI : ComponentActivity() {
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
                                val navigate = Intent(this@GenreUI, PlayList::class.java)
                                startActivity(navigate)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowBack,
                                    contentDescription = "Click to share",
                                    tint = Color.Red
                                )
                            }
                            GenreRecyclerView()
                            IconButton(onClick = {
                                val navigate = Intent(this@GenreUI, TwoMainButtons::class.java)
                                startActivity(navigate)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowForward,
                                    contentDescription = "Click to share",
                                    tint = Color.Red
                                )
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
fun Page2(name: String){
    Row(modifier = Modifier.padding(10.dp)){
        OutlinedButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(100.dp))
        }
        Spacer(modifier = Modifier.width(20.dp))
        OutlinedButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(100.dp))
        }
    }
}

@Composable
fun GenreRecyclerView(names : List<String> = List(4){"$it"}){

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        LazyColumn(){
            items(items = names){
                    name -> Page2(name = name)
            }
        }
    }
}