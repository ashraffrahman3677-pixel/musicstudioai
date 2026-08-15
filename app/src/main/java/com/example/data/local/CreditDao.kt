package com.example.data.local

import androidx.room.*
import com.example.data.model.CreditTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {
    @Query("SELECT * FROM credit_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CreditTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CreditTransactionEntity)
}
