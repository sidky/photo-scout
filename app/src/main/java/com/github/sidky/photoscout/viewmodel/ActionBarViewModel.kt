package com.github.sidky.photoscout.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class VisibleScreen {
    LIST, MAP, DETAIl
}

data class ActionBarState(val searchText: String? = null, val screen: VisibleScreen = VisibleScreen.LIST)

class ActionBarViewModel : ViewModel() {
    val state = MutableLiveData<ActionBarState>()

    fun setActionBarScreen(screen: VisibleScreen) {
        val currentState = state.value ?: ActionBarState()
        state.postValue(currentState.copy(screen = screen))
    }
}