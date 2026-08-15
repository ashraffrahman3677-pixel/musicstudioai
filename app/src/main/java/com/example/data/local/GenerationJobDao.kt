package com.example.data.local

import androidx.room.*
import com.example.data.model.GenerationJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GenerationJobDao {
    @Query("SELECT * FROM generation_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<GenerationJobEntity>>

    @Query("SELECT * FROM generation_jobs WHERE id = :id")
    suspend fun getJobById(id: String): GenerationJobEntity?

    @Query("SELECT * FROM generation_jobs WHERE status IN ('QUEUED', 'PREPARING', 'GENERATING_INSTRUMENTAL', 'GENERATING_VOCAL', 'MIXING', 'MASTERING') ORDER BY createdAt DESC")
    fun getActiveJobs(): Flow<List<GenerationJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: GenerationJobEntity)

    @Update
    suspend fun updateJob(job: GenerationJobEntity)

    @Query("DELETE FROM generation_jobs WHERE id = :id")
    suspend fun deleteJob(id: String)
}
