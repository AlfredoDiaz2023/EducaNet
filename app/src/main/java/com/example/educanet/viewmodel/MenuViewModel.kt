package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.repository.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {

    private val notificacionRepository = NotificacionRepository()

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()

    init {
        checkForUnreadNotifications()
    }

    fun checkForUnreadNotifications() {
        viewModelScope.launch {
            _hasUnreadNotifications.value = notificacionRepository.hayNotificacionesSinLeer()
        }
    }
}