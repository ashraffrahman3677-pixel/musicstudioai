package com.example.data.local

import androidx.room.*
import com.example.data.model.VoiceProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceProfileDao {
    @Query("SELECT * FROM voice_profiles WHERE status != 'DELETED' ORDER BY createdAt DESC")
    fun getAllVoiceProfiles(): Flow<List<VoiceProfileEntity>>

    @Query("SELECT * FROM voice_profiles WHERE id = :id AND status != 'DELETED'")
    suspend fun getVoiceProfileById(id: String): VoiceProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceProfile(profile: VoiceProfileEntity)

    @Update
    suspend fun updateVoiceProfile(profile: VoiceProfileEntity)

    @Query("UPDATE voice_profiles SET status = 'DELETED' WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("DELETE FROM voice_profiles WHERE id = :id")
    suspend fun purgeVoiceProfile(id: String)
}
