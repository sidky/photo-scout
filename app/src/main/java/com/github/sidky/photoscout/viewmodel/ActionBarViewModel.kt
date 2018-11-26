package com.github.sidky.photoscout.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ActionBarState(val searchText: String? = null, val isVisible: Boolean = true)

class ActionBarViewModel : ViewModel() {
    val state = MutableLiveData<ActionBarState>()

    fun showActionBar(visible: Boolean) {
        val currentState = state.value ?: ActionBarState()
        state.postValue(currentState.copy(isVisible = visible))
    }
}