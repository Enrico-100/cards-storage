package com.example.cards_app.communication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cards_app.Card
import com.example.cards_app.LocalCardsRepository
import com.example.cards_app.RecoveryStep
import com.example.cards_app.models.RecoveryInitiateRequest
import com.example.cards_app.models.RecoveryOption
import com.example.cards_app.models.RecoveryResetRequest
import com.example.cards_app.models.SendRecoveryCodeRequest
import com.example.cards_app.models.User
import com.example.cards_app.models.VerificationRequest
import com.example.cards_app.models.VerificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val recoveryOptions: List<RecoveryOption>? = null
)

class CommunicationViewModel(
    private val localCardsRepo: LocalCardsRepository
) : ViewModel() {

    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()
    
    private var sessionUsername: String? = null
    private var sessionPassword: String? = null

    private var _loggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loggedIn = _loggedIn.asStateFlow()

    // token will be implemented later, making functional design first
    //private val _token = MutableStateFlow<String?>(null)
    //val token = _token.asStateFlow()

    private var _recoveryStep : MutableStateFlow<RecoveryStep> = MutableStateFlow(RecoveryStep.ENTER_USERNAME)
    val recoveryStep = _recoveryStep.asStateFlow()



    fun validatePassword(password: String): Boolean {
        if (password.length < 8) {
            _uiState.value = RegisterUiState(error = "Password must be at least 8 characters long.")
            return false
        } else if (!password.any { it.isDigit() }) {
            _uiState.value = RegisterUiState(error = "Password must contain at least one number.")
            return false
        } else if (!password.any { it.isUpperCase() }) {
            _uiState.value = RegisterUiState(error = "Password must contain at least one uppercase letter.")
            return false
        } else if (!password.any { it.isLowerCase() }) {
            _uiState.value = RegisterUiState(error = "Password must contain at least one lowercase letter.")
            return false
        }
        return true
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearState() {
        _uiState.value = RegisterUiState()
    }

    fun performLogIn(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val userResponse = userRepository.getUser(username, password)
                if (userResponse.isSuccessful) {
                    // SUCCESS: Store the FULL user object returned from the server.
                    // This overwrites any previous state with complete, accurate data.
                    _user.value = userResponse.body()
                    sessionUsername = username
                    sessionPassword = password
                    _loggedIn.value = true
                    _uiState.value = RegisterUiState(isSuccess = true)
                } else if(userResponse.code() == 401) {
                    // This is the most common failure: invalid credentials.
                    val errorMessage = "Invalid username or password"
                    _uiState.value = RegisterUiState(error = errorMessage)
                    Log.e("CommunicationViewModel", "Login failed: $errorMessage")
                }else{
                    // This would be an unexpected error (e.g., server issue after auth).
                    val error = userResponse.errorBody()?.string() ?: "Failed to fetch user details."
                    _uiState.value = RegisterUiState(error = error)
                    Log.e("CommunicationViewModel", "User fetch failed: $error")
                }

            } catch (e: Exception) {
                // This catches network issues like no internet.
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun performRegistration(user: User) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val localCards = localCardsRepo.cardsFlow.first()
                val userWithCards = user.copy(cards = localCards)
                val response = userRepository.registerUser(userWithCards)
                if (response.isSuccessful) {
                    _user.value = User(
                        username = response.body()?.username ?: user.username,
                        email = user.email,
                        phoneNumber = user.phoneNumber,
                        passwordHash = null,
                        name = response.body()?.name,
                        cards = localCards,
                        emailVerified = false,
                        phoneVerified = false
                    )
                    sessionUsername = user.username
                    sessionPassword = user.passwordHash
                    _uiState.value = RegisterUiState(
                        isSuccess = true,
                        error = "Registration successful"
                    )
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Registration failed"
                    _uiState.value = RegisterUiState(error = "Registration failed: $errorMessage")
                    Log.e("CommunicationViewModel", "Registration failed: $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun performLogout() {
        _user.value = null
        sessionUsername = null
        sessionPassword = null
        _loggedIn.value = false
        _uiState.value = RegisterUiState()
    }

    fun deleteUser() {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                if (_user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.deleteUser(sessionUsername!!, sessionPassword!!)
                    if (response.isSuccessful) {
                        _uiState.value = RegisterUiState(isSuccess = true)
                        performLogout()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Deletion failed"
                        _uiState.value = RegisterUiState(error = errorMessage)
                        Log.e("CommunicationViewModel", "Deletion failed: $errorMessage")
                    }
                } else {
                    _uiState.value = RegisterUiState(error = "User logged out")
                    Log.e("CommunicationViewModel", "User logged out or ID not found for deletion")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun updateUser(newUser: User) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                if (_user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.updateUser(sessionUsername!!, sessionPassword!!, newUser)
                    if (response.isSuccessful) {
                        _user.value = response.body()
                        if (!newUser.passwordHash.isNullOrBlank()){
                            sessionPassword = newUser.passwordHash
                        }
                        _uiState.value = RegisterUiState(
                            isSuccess = true,
                            error = "Update successful"
                        )
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        _uiState.value = RegisterUiState(error = errorMessage ?: "Update failed")
                        Log.e("CommunicationViewModel", "Update failed: $errorMessage")
                    }
                } else {
                    _uiState.value = RegisterUiState(error = "User logged out")
                    Log.e("CommunicationViewModel", "User logged out or ID not found for update")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun syncCards(cards: List<Card>) {
        viewModelScope.launch {
            if (sessionUsername == null || sessionPassword == null) {
                Log.w("CommunicationViewModel", "Sync failed: User not logged in.")
                _uiState.value = RegisterUiState(error = "You must be logged in to sync cards.")
                return@launch
            }

            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val updatedUser = User(
                    username = sessionUsername!!,
                    cards = cards
                )
                val response = userRepository.updateUser(sessionUsername!!, sessionPassword!!, updatedUser)
                if (response.isSuccessful) {
                    _user.value = response.body()
                    _uiState.value = RegisterUiState(
                        isSuccess = true,
                        error = "Cards synced successfully"
                    )
                    Log.d("CommunicationViewModel", "Cards synced successfully")
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Cards sync failed"
                    _uiState.value = RegisterUiState(error = errorMessage)
                    Log.e("CommunicationViewModel", "Cards sync failed: $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun verifyEmail(code: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                if (_user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.verifyUser(sessionUsername!!, sessionPassword!!,
                        VerificationRequest(code, VerificationType.EMAIL))
                    if (response.isSuccessful) {
                        Log.d("CommunicationViewModel", "Email verification successful")
                        val userResponse = userRepository.getUser(sessionUsername!!, sessionPassword!!)
                        if (userResponse.isSuccessful) {
                            _user.value = userResponse.body()
                            _uiState.value = RegisterUiState(
                                isSuccess = true,
                                error = "Email verification successful"
                            )
                        } else {
                            val errorMessage = userResponse.errorBody()?.string() ?: "Failed to fetch user details."
                            _uiState.value = RegisterUiState(error = errorMessage)
                            Log.e("CommunicationViewModel", "User fetch failed: $errorMessage")
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Verification failed"
                        _uiState.value = RegisterUiState(error = errorMessage)
                        Log.e("CommunicationViewModel", "Verification failed: $errorMessage")
                    }
                } else {
                    _uiState.value = RegisterUiState(error = "User logged out")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun verifyPhone(code: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                if (_user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.verifyUser(sessionUsername!!, sessionPassword!!,
                        VerificationRequest(code, VerificationType.PHONE))
                    if (response.isSuccessful) {
                        Log.d("CommunicationViewModel", "Phone verification successful")
                        val userResponse = userRepository.getUser(sessionUsername!!, sessionPassword!!)
                        if (userResponse.isSuccessful) {
                            _user.value = userResponse.body()
                            _uiState.value = RegisterUiState(
                                isSuccess = true,
                                error = "Phone verification successful"
                            )
                        } else {
                            val errorMessage = userResponse.errorBody()?.string() ?: "Failed to fetch user details."
                            _uiState.value = RegisterUiState(error = errorMessage)
                            Log.e("CommunicationViewModel", "User fetch failed: $errorMessage")
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Verification failed"
                        _uiState.value = RegisterUiState(error = errorMessage)
                        Log.e("CommunicationViewModel", "Verification failed: $errorMessage")
                    }
                } else {
                    _uiState.value = RegisterUiState(error = "User logged out")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun resendVerificationCodes() {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                if (_user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.resendVerificationCodes(sessionUsername!!, sessionPassword!!)
                    if (response.isSuccessful) {
                        Log.d("CommunicationViewModel", "Verification codes resent successfully")
                        _uiState.value = RegisterUiState(
                            isSuccess = true,
                            error = "Verification codes resent successfully"
                        )
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Resend failed"
                        _uiState.value = RegisterUiState(error = errorMessage)
                        Log.e("CommunicationViewModel", "Resend failed: $errorMessage")
                    }
                } else {
                    _uiState.value = RegisterUiState(error = "User logged out")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    // recovery
    fun initiateRecovery(username: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val response = userRepository.initiateRecovery(RecoveryInitiateRequest(username))
                if (response.isSuccessful) {
                    if (response.body().isNullOrEmpty()) {
                        _uiState.value = RegisterUiState(error = "No recovery options available")
                    } else {
                        _uiState.value = RegisterUiState(isSuccess = true, recoveryOptions = response.body())
                        _recoveryStep.value = RecoveryStep.CHOOSE_CHANNEL
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Recovery failed"
                    _uiState.value = RegisterUiState(error = errorMessage)
                    Log.e("CommunicationViewModel", "Recovery failed: $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun sendRecoveryCode(username: String, channel: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val verificationType = enumValueOf<VerificationType>(channel.uppercase())
                val response = userRepository.sendRecoveryCode(
                    SendRecoveryCodeRequest(
                        username,
                        verificationType
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState(isSuccess = true)
                    _recoveryStep.value = RecoveryStep.ENTER_CODE_AND_RESET
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Recovery code sending failed"
                    _uiState.value = RegisterUiState(error = errorMessage)
                    Log.e("CommunicationViewModel", "Recovery code sending failed: $errorMessage")
                }
            } catch (e: IllegalArgumentException) {
                _uiState.value = RegisterUiState(error = "Invalid channel: $channel")
                Log.e("CommunicationViewModel", "Invalid channel: $channel", e)
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }
    fun resetPassword(username: String, recoveryCode: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val response = userRepository.resetPassword(
                    RecoveryResetRequest(
                        username,
                        recoveryCode,
                        newPassword
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState(isSuccess = true)
                    _recoveryStep.value = RecoveryStep.SUCCESS
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Password reset failed"
                    _uiState.value = RegisterUiState(error = errorMessage)
                    Log.e("CommunicationViewModel", "Password reset failed: $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }
    fun resetRecoveryStep() {
        _recoveryStep.value = RecoveryStep.ENTER_USERNAME
        _uiState.value = RegisterUiState()
    }

}
