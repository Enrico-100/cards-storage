package com.example.cards_app.communication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cards_app.Card
import com.example.cards_app.LocalCardsRepository
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
        _uiState.value = RegisterUiState()
    }

    fun performLogIn(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            try {
                // Step 1: Authenticate and get the user ID. This is the "login" check.
                val idResponse = userRepository.getMyId(username, password)

                if (idResponse.isSuccessful) {
                    val userId = idResponse.body()
                    if (userId != null) {
                        // Step 2: Now that you're authenticated, fetch the complete user object.
                        val userResponse = userRepository.getUser(userId, username, password)
                        if (userResponse.isSuccessful) {
                            // SUCCESS: Store the FULL user object returned from the server.
                            // This overwrites any previous state with complete, accurate data.
                            _user.value = userResponse.body()
                            sessionUsername = username
                            sessionPassword = password
                            _loggedIn.value = true
                            _uiState.value = RegisterUiState(isSuccess = true)
                        } else {
                            // This would be an unexpected error (e.g., server issue after auth).
                            val error = userResponse.errorBody()?.string() ?: "Failed to fetch user details."
                            _uiState.value = RegisterUiState(error = error)
                            Log.e("CommunicationViewModel", "User fetch failed: $error")
                        }
                    } else {
                        // This is unlikely but safe to handle (API returns 200 OK but no ID).
                        _uiState.value = RegisterUiState(error = "User ID was null after successful auth.")
                        Log.e("CommunicationViewModel", "User ID was null.")
                    }
                } else {
                    // This is the most common failure: invalid credentials.
                    val errorMessage = "Invalid username or password"
                    _uiState.value = RegisterUiState(error = errorMessage)
                    Log.e("CommunicationViewModel", "Login failed: $errorMessage")
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
                        id = response.body()?.userId,
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
                    _uiState.value = RegisterUiState(isSuccess = true)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Registration failed"
                    _uiState.value = RegisterUiState(error = errorMessage)
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
                val userId = _user.value?.id
                if (userId != null && _user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.deleteUser(userId, sessionUsername!!, sessionPassword!!)
                    if (response.isSuccessful) {
                        _uiState.value = RegisterUiState(isSuccess = true)
                        performLogout()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Deletion failed"
                        _uiState.value = RegisterUiState(error = errorMessage)
                        Log.e("CommunicationViewModel", "Deletion failed: $errorMessage")
                    }
                } else {
                    _uiState.value = RegisterUiState(error = "User loged out")
                    Log.e("CommunicationViewModel", "User loged out or ID not found for delition")
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
                val userId = _user.value?.id
                if (userId != null && _user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.updateUser(userId, sessionUsername!!, sessionPassword!!, newUser)
                    if (response.isSuccessful) {
                        _user.value = response.body()
                        if (!newUser.passwordHash.isNullOrBlank()){
                            sessionPassword = newUser.passwordHash
                        }
                        _uiState.value = RegisterUiState(isSuccess = true)
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        _uiState.value = RegisterUiState(error = errorMessage ?: "Update failed")
                        Log.e("CommunicationViewModel", "Update failed: $errorMessage")
                    }
                } else {
                    _uiState.value = RegisterUiState(error = "User loged out")
                    Log.e("CommunicationViewModel", "User loged out or ID not found for update")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = "Network error: ${e.message}")
                Log.e("CommunicationViewModel", "Network error: ${e.message}")
            }
        }
    }

    fun syncCards(cards: List<Card>) {
        viewModelScope.launch {
            val userId = _user.value?.id
            if (userId == null || sessionUsername == null || sessionPassword == null) {
                Log.w("CommunicationViewModel", "Sync failed: User not logged in.")
                _uiState.value = RegisterUiState(error = "You must be logged in to sync cards.")
                return@launch
            }

            _uiState.value = RegisterUiState(isLoading = true)
            try {
                val updatedUser = User(
                    id = userId,
                    username = sessionUsername!!,
                    cards = cards
                )
                val response = userRepository.updateUser(userId, sessionUsername!!, sessionPassword!!, updatedUser)
                if (response.isSuccessful) {
                    _user.value = response.body()
                    _uiState.value = RegisterUiState(isSuccess = true)
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
                val userId = _user.value?.id
                if (userId != null && _user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.verifyUser(userId, sessionUsername!!, sessionPassword!!,
                        VerificationRequest(code, VerificationType.EMAIL))
                    if (response.isSuccessful) {
                        Log.d("CommunicationViewModel", "Email verification successful")
                        val userResponse = userRepository.getUser(userId, sessionUsername!!, sessionPassword!!)
                        if (userResponse.isSuccessful) {
                            _user.value = userResponse.body()
                            _uiState.value = RegisterUiState(isSuccess = true)
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
                    _uiState.value = RegisterUiState(error = "User loged out")
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
                val userId = _user.value?.id
                if (userId != null && _user.value != null && sessionUsername != null && sessionPassword != null) {
                    val response = userRepository.verifyUser(userId, sessionUsername!!, sessionPassword!!,
                        VerificationRequest(code, VerificationType.PHONE))
                    if (response.isSuccessful) {
                        Log.d("CommunicationViewModel", "Phone verification successful")
                        val userResponse = userRepository.getUser(userId, sessionUsername!!, sessionPassword!!)
                        if (userResponse.isSuccessful) {
                            _user.value = userResponse.body()
                            _uiState.value = RegisterUiState(isSuccess = true)
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
                    _uiState.value = RegisterUiState(error = "User loged out")
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

}
