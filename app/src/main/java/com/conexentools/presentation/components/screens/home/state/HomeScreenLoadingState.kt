package com.conexentools.presentation.components.screens.home.state

sealed class HomeScreenLoadingState {
  data object ScreenLoading : HomeScreenLoadingState()
  data object Success : HomeScreenLoadingState()
  data class Error(val message: String?) : HomeScreenLoadingState()
}