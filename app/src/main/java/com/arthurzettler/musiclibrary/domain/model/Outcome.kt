package com.arthurzettler.musiclibrary.domain.model

sealed interface Outcome<out T> {
    data class Success<T>(val data: T) : Outcome<T>
    data class Stale<T>(val data: T) : Outcome<T>
    data class Error(val message: String?) : Outcome<Nothing>
}
