package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "user_default",
    val type: String, // "PURCHASE", "SUBSCRIPTION", "GENERATION_CHARGE", "REFUND", "BONUS"
    val amount: Int,
    val balanceAfter: Int,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
