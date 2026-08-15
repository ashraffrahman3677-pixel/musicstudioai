package com.example.data.repository

import com.example.data.local.CreditDao
import com.example.data.model.CreditTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(private val creditDao: CreditDao) {
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow("Producer Studio")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("telcohauz@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userPlan = MutableStateFlow("Creator Pro") // Free, Creator, Pro, Business
    val userPlan: StateFlow<String> = _userPlan.asStateFlow()

    private val _creditsBalance = MutableStateFlow(120)
    val creditsBalance: StateFlow<Int> = _creditsBalance.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("ms") // "ms" for Malay, "en" for English
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _emergencyKillSwitch = MutableStateFlow(false)
    val emergencyKillSwitch: StateFlow<Boolean> = _emergencyKillSwitch.asStateFlow()

    val transactionHistory: Flow<List<CreditTransactionEntity>> = creditDao.getAllTransactions()

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun toggleEmergencyKillSwitch() {
        _emergencyKillSwitch.value = !_emergencyKillSwitch.value
    }

    suspend fun deductCredits(amount: Int, description: String) {
        val newBalance = (_creditsBalance.value - amount).coerceAtLeast(0)
        _creditsBalance.value = newBalance
        creditDao.insertTransaction(
            CreditTransactionEntity(
                id = UUID.randomUUID().toString(),
                type = "GENERATION_CHARGE",
                amount = -amount,
                balanceAfter = newBalance,
                description = description
            )
        )
    }

    suspend fun addCredits(amount: Int, description: String) {
        val newBalance = _creditsBalance.value + amount
        _creditsBalance.value = newBalance
        creditDao.insertTransaction(
            CreditTransactionEntity(
                id = UUID.randomUUID().toString(),
                type = "REFUND",
                amount = amount,
                balanceAfter = newBalance,
                description = description
            )
        )
    }

    suspend fun purchasePlan(planName: String, creditsAdded: Int) {
        _userPlan.value = planName
        val newBalance = _creditsBalance.value + creditsAdded
        _creditsBalance.value = newBalance
        creditDao.insertTransaction(
            CreditTransactionEntity(
                id = UUID.randomUUID().toString(),
                type = "SUBSCRIPTION",
                amount = creditsAdded,
                balanceAfter = newBalance,
                description = "Upgraded to $planName ($creditsAdded credits)"
            )
        )
    }
}
