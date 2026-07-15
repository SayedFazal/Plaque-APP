package com.periocompliance.ai.data.remote

import com.periocompliance.ai.data.remote.dto.EmailRequest
import com.periocompliance.ai.data.remote.dto.LoginRequest
import com.periocompliance.ai.data.remote.dto.RefreshRequest
import com.periocompliance.ai.data.remote.dto.RegisterRequest
import com.periocompliance.ai.data.remote.dto.SessionDto
import com.periocompliance.ai.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): SessionDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): SessionDto

    /**
     * Called by [TokenAuthenticator], not by the repository. It is here rather than on a separate
     * interface only because the shape belongs with the rest of auth; the Retrofit instance that
     * serves it is deliberately a different, interceptor-free one.
     */
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): SessionDto

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshRequest)

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body body: EmailRequest)

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: EmailRequest)

    /** The only way to learn that a pending email address has since been verified. */
    @GET("auth/me")
    suspend fun me(): UserResponse
}
