package com.example.spotifyplayer.utils

import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RecommendedUtils {
    var tracks: MutableList<ListItem> = mutableListOf()
    var complete: Boolean = false
    fun getSectionChildren(item: ListItem, spotifyControl: SpotifyAppRemote): List<ListItem> {
        val children: MutableList<ListItem> = mutableListOf()
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                spotifyControl.contentApi.getChildrenOfItem(item, 20, 0).setResultCallback {
                }
            }
        }
        return children.toList()
    }
}