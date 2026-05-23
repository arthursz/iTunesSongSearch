package com.arthurzettler.musiclibrary.data.local

import androidx.room.withTransaction
import com.arthurzettler.musiclibrary.data.local.database.AppDatabase
import javax.inject.Inject

class RoomDatabaseTransactionRunner @Inject constructor(
    private val database: AppDatabase
) : DatabaseTransactionRunner {

    override suspend fun <R> runInTransaction(block: suspend () -> R): R {
        return database.withTransaction(block)
    }
}
