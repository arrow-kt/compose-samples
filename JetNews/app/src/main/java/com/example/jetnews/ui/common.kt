package com.example.jetnews.ui

// Basic Load-Content-Error ADT
sealed class ScreenState<out A> {

    // TODO gotcha#3 cannot add a retry lambda here: retry: () -> Unit
    object Loading : ScreenState<Nothing>()

    data class Content<out A>(val value: A) : ScreenState<A>()

    data class Error(val retry: Unit) : ScreenState<Nothing>()
}
