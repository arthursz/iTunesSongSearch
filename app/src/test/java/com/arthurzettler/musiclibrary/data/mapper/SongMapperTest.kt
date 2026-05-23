package com.arthurzettler.musiclibrary.data.mapper

import com.arthurzettler.musiclibrary.data.local.entity.SongEntity
import com.arthurzettler.musiclibrary.data.remote.dto.SongDto
import com.arthurzettler.musiclibrary.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongMapperTest {

    @Test
    fun `SongDto toEntity maps correctly`() {
        val dto = SongDto(
            trackId = 1L,
            trackName = "Get Lucky",
            artistName = "Daft Punk",
            collectionName = "Random Access Memories",
            artworkUrl100 = "https://example.com/100x100.jpg",
            previewUrl = "https://example.com/preview.m4a",
            trackTimeMillis = 248000L,
            collectionId = 100L,
            trackNumber = 8,
            releaseDate = "2013-05-17",
            wrapperType = "track"
        )

        val entity = dto.toEntity()!!

        assertEquals(1L, entity.trackId)
        assertEquals("Get Lucky", entity.trackName)
        assertEquals("Daft Punk", entity.artistName)
        assertEquals("https://example.com/600x600.jpg", entity.artworkUrl)
    }

    @Test
    fun `SongDto toEntity returns null for non-track wrapper type`() {
        val dto = SongDto(
            trackId = 1L,
            trackName = "Collection",
            artistName = "Daft Punk",
            collectionName = "Random Access Memories",
            artworkUrl100 = "https://example.com/100x100.jpg",
            previewUrl = null,
            trackTimeMillis = null,
            collectionId = 100L,
            trackNumber = null,
            releaseDate = null,
            wrapperType = "collection"
        )

        assertNull(dto.toEntity())
    }

    @Test
    fun `SongDto toEntity returns null when trackId is null`() {
        val dto = SongDto(
            trackId = null,
            trackName = "Get Lucky",
            artistName = "Daft Punk",
            collectionName = "Random Access Memories",
            artworkUrl100 = "https://example.com/100x100.jpg",
            previewUrl = "https://example.com/preview.m4a",
            trackTimeMillis = 248000L,
            collectionId = 100L,
            trackNumber = 8,
            releaseDate = "2013-05-17",
            wrapperType = "track"
        )

        assertNull(dto.toEntity())
    }

    @Test
    fun `SongEntity toDomain maps correctly`() {
        val entity = SongEntity(
            trackId = 1L,
            trackName = "Get Lucky",
            artistName = "Daft Punk",
            collectionName = "Random Access Memories",
            artworkUrl = "https://example.com/600x600.jpg",
            previewUrl = "https://example.com/preview.m4a",
            trackTimeMillis = 248000L,
            collectionId = 100L,
            trackNumber = 8
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.trackId)
        assertEquals("Get Lucky", domain.trackName)
        assertEquals("Random Access Memories", domain.collectionName)
    }

    @Test
    fun `Song toEntity maps correctly`() {
        val song = Song(
            trackId = 1L,
            trackName = "Get Lucky",
            artistName = "Daft Punk",
            collectionName = "Random Access Memories",
            artworkUrl = "https://example.com/600x600.jpg",
            previewUrl = "https://example.com/preview.m4a",
            trackTimeMillis = 248000L,
            collectionId = 100L,
            trackNumber = 8
        )

        val entity = song.toEntity(lastPlayedAt = 5000L)

        assertEquals(1L, entity.trackId)
        assertEquals(5000L, entity.lastPlayedAt)
    }

    @Test
    fun `artworkUrl100 is replaced with 600x600`() {
        val dto = SongDto(
            trackId = 1L,
            trackName = "Test",
            artistName = "Test",
            collectionName = "Test",
            artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/100x100bb.jpg",
            previewUrl = "",
            trackTimeMillis = 1000L,
            collectionId = 1L,
            trackNumber = 1,
            releaseDate = "",
            wrapperType = "track"
        )

        val entity = dto.toEntity()!!
        assertEquals(
            "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/600x600bb.jpg",
            entity.artworkUrl
        )
    }
}
