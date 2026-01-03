package com.example.cards_app.communication

import com.example.cards_app.models.RecoveryInitiateRequest
import com.example.cards_app.models.RecoveryResetRequest
import com.example.cards_app.models.SendRecoveryCodeRequest
import com.example.cards_app.models.User
import com.example.cards_app.models.VerificationRequest
import okhttp3.Credentials
import retrofit2.Response

// The repository is the single source of truth for user-related data.
class UserRepository {

    private val apiService = RetrofitClient.instance

    suspend fun getMyId(username: String, password: String): Response<Long> {
        val authHeader = Credentials.basic(username, password)
        return apiService.getMyId(authHeader)
    }

    suspend fun getUser(id: Long, username: String, password: String): Response<User> {
        val authHeader = Credentials.basic(username, password)
        return apiService.getUser(authHeader,id)
    }

    suspend fun registerUser(newUser: User) = apiService.createUser(newUser)

    suspend fun deleteUser(id: Long, username: String, password: String): Response<Unit> {
        val authHeader = Credentials.basic(username, password)
        return apiService.deleteUser(authHeader,id)
    }

    suspend fun updateUser(id: Long, username: String, password: String, newUser: User): Response<User> {
        val authHeader = Credentials.basic(username, password)
        return apiService.updateUser(authHeader, id, newUser)
    }

    suspend fun verifyUser(id: Long, username: String, password: String, verificationRequest: VerificationRequest): Response<Unit> {
        val authHeader = Credentials.basic(username, password)
        return apiService.verifyUser(authHeader, id, verificationRequest)
    }

    // Recovery methods
    suspend fun initiateRecovery(recoveryInitiateRequest: RecoveryInitiateRequest) = apiService.initiateRecovery(recoveryInitiateRequest)

    suspend fun sendRecoveryCode(sendRecoveryCodeRequest: SendRecoveryCodeRequest) = apiService.sendRecoveryCode(sendRecoveryCodeRequest)

    suspend fun resetPassword(recoveryResetRequest: RecoveryResetRequest) = apiService.resetPassword(recoveryResetRequest)
}
