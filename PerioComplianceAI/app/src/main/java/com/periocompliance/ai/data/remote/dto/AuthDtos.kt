package com.periocompliance.ai.data.remote.dto

import com.periocompliance.ai.domain.model.User
import kotlinx.serialization.Serializable

/**
 * Wire format. These mirror the backend's JSON exactly and go no further than the data layer --
 * the repository maps them to domain models, so a change to the API shape stops here.
 */

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class EmailRequest(val email: String)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val emailVerified: Boolean,
) {
    fun toDomain() = User(
        uid = id,
        email = email,
        displayName = name,
        isEmailVerified = emailVerified,
    )
}

/** What /auth/register, /auth/login and /auth/refresh all return. */
@Serializable
data class SessionDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class UserResponse(val user: UserDto)

/**
 * The error envelope: `{ "error": { "code": "...", "message": "..." } }`.
 *
 * `code` is the contract. `message` is for humans reading logs -- the app never shows it, because
 * the UI renders its own copy from strings.xml via AuthError.
 */
@Serializable
data class ApiErrorEnvelope(val error: ApiErrorBody)

@Serializable
data class ApiErrorBody(
    val code: String,
    val message: String? = null,
)
