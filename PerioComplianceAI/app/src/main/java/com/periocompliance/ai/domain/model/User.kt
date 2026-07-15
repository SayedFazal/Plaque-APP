package com.periocompliance.ai.domain.model

/**
 * The app's user. Deliberately not a FirebaseUser -- nothing above the repository is allowed to
 * know which auth provider we happen to be using.
 */
data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val isEmailVerified: Boolean,
)
