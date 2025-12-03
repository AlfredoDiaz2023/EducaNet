package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.VideoApoyo
import com.example.educanet.repository.VideoApoyoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoApoyoViewModel : ViewModel() {
    private val repository = VideoApoyoRepository()

    private val _videos = MutableStateFlow<List<VideoApoyo>>(emptyList())
    val videos: StateFlow<List<VideoApoyo>> = _videos.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        obtenerVideos()
    }

    private fun obtenerVideos() {
        viewModelScope.launch {
            _cargando.value = true
            val resultado = repository.obtenerVideosDeApoyo()
            _videos.value = resultado.videos
            _cargando.value = false
        }
    }

    fun cargarVideos() {
        obtenerVideos()
    }
}
