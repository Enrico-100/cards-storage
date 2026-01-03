package com.example.cards_app.communication

import com.example.cards_app.models.RecoveryInitiateRequest
import com.example.cards_app.models.RecoveryOption
import com.example.cards_app.models.RecoveryResetRequest
import com.example.cards_app.models.SendRecoveryCodeRequest
import com.example.cards_app.models.User
import com.example.cards_app.models.UserCreationResponse
import com.example.cards_app.models.VerificationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    /**
     * Gets the ID of the currently authenticated user.
     * Corresponds to: @GetMapping("/me") in UserController.
     * Returns the user's ID upon successful authentication.
     */
    @GET("api/users/me")
    suspend fun getMyId(
        @Header("Authorization") authHeader: String
    ): Response<Long>

    /**
     * Gets the full user details for the currently authenticated user.
     * Corresponds to: @GetMapping("/{id}") in UserController.
     * Note: The Java backend enforces that the {id} must match the authenticated user.
     */
    @GET("api/users/{id}")
    suspend fun getUser(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<User>

    /**
     * Creates a new user (registration).
     * Corresponds to: @PostMapping in UserController.
     * @param user A 'User' object containing registration details.
     * The backend expects a JSON body with fields like username, passwordHash, email, etc.
     */
    @POST("api/users")
    suspend fun createUser(
        @Body user: User
    ): Response<UserCreationResponse>

    /**
     * GET /api/users
     * Gets a list of all users. Requires Admin role.
     * @return A Response containing a list of all User objects.
     */
    @GET("api/users")
    suspend fun getAllUsers(
        @Header("Authorization") authHeader: String,
    ): Response<List<User>>

    /**
     * PUT /api/users/{id}
     * Updates an existing user's information.
     * The backend enforces that the {id} must match the authenticated user's ID.
     * @param id The ID of the user to update.
     * @param user A User object containing the fields to be updated (e.g., passwordHash, name, email).
     * @return A Response containing the updated User object.
     */
    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Body user: User
    ): Response<User>

    /**
     * DELETE /api/users/{id}
     * Deletes a user.
     * The backend enforces that the {id} must match the authenticated user's ID.
     * @param id The ID of the user to delete.
     * @return A Response with no body, indicating success (200 OK) or failure.
     */
    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<Unit> // Response<Unit> is used for empty successful responses

    /**
     * POST /api/users/{id}/verify
     * Submits a verification code for a user.
     * @param id The ID of the user to verify.
     * @param verificationRequest A body containing the 'type' and 'code' for verification.
     * @return A Response containing a success message string.
     */
    @POST("api/users/{id}/verify")
    suspend fun verifyUser(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Body verificationRequest: VerificationRequest
    ): Response<Unit>

    /**
     * POST /api/recovery/initiate
     * Initiates the password recovery process for a user.
     * @param request Body containing the username.
     * @return A list of masked recovery options (e.g., ["ema**@g****.com", "123****789"]).
     */
    @POST("api/recovery/initiate")
    suspend fun initiateRecovery(
        @Body request: RecoveryInitiateRequest
    ): Response<List<RecoveryOption>>

    /**
     * POST /api/recovery/send-code
     * Requests a recovery code to be sent to a specific channel (e.g., "EMAIL" or "PHONE").
     * @param request Body containing the username and the chosen channel.
     * @return A success message string.
     */
    @POST("api/recovery/send-code")
    suspend fun sendRecoveryCode(
        @Body request: SendRecoveryCodeRequest
    ): Response<String>

    /**
     * POST /api/recovery/reset
     * Resets the user's password using a valid recovery code.
     * @param request Body containing username, recovery code, and the new password.
     * @return A success message string.
     */
    @POST("api/recovery/reset")
    suspend fun resetPassword(
        @Body request: RecoveryResetRequest
    ): Response<String>

}