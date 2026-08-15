package com.example.data.local

import androidx.room.*
import com.example.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY createdAt DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE vocalType = 'USER_VOICE' ORDER BY createdAt DESC")
    fun getMyVoiceSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isInstrumental = 1 OR vocalType = 'INSTRUMENTAL' ORDER BY createdAt DESC")
    fun getInstrumentalSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): SongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET title = :newTitle WHERE id = :songId")
    suspend fun renameSong(songId: String, newTitle: String)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSongById(songId: String)
}
