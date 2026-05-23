package com.arthurzettler.musiclibrary.data.local

interface DatabaseTransactionRunner {
    suspend fun <R> runInTransaction(block: suspend () -> R): R
}
