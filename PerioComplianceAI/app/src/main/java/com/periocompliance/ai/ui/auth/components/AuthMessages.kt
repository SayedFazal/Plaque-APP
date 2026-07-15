package com.periocompliance.ai.ui.auth.components

import androidx.annotation.StringRes
import com.periocompliance.ai.R
import com.periocompliance.ai.domain.model.AuthError
import com.periocompliance.ai.domain.validation.FieldError
import com.periocompliance.ai.domain.validation.PasswordStrength

/**
 * The one place a domain error becomes something a human reads. Keeping it here means a ViewModel
 * never holds a string, and a screen never holds an if-chain over error types.
 */

@StringRes
fun AuthError.messageRes(): Int = when (this) {
    AuthError.Network -> R.string.error_network
    AuthError.InvalidCredentials -> R.string.error_invalid_credentials
    AuthError.EmailAlreadyInUse -> R.string.error_email_in_use
    AuthError.AccountDisabled -> R.string.error_account_disabled
    AuthError.TooManyAttempts -> R.string.error_too_many_attempts
    AuthError.WeakPassword -> R.string.error_weak_password
    AuthError.InvalidEmail -> R.string.error_invalid_email
    AuthError.EmailNotVerified -> R.string.error_email_not_verified
    AuthError.SessionExpired -> R.string.error_session_expired
    is AuthError.Unknown -> R.string.error_unknown
}

@StringRes
fun FieldError.messageRes(): Int = when (this) {
    FieldError.Required -> R.string.field_required
    FieldError.NameTooShort -> R.string.field_name_short
    FieldError.NameTooLong -> R.string.field_name_long
    FieldError.EmailMalformed -> R.string.field_email_malformed
    FieldError.PasswordTooShort -> R.string.field_password_short
    FieldError.PasswordTooLong -> R.string.field_password_long
    FieldError.PasswordNeedsUppercase -> R.string.field_password_upper
    FieldError.PasswordNeedsLowercase -> R.string.field_password_lower
    FieldError.PasswordNeedsDigit -> R.string.field_password_digit
    FieldError.PasswordNeedsSpecial -> R.string.field_password_special
    FieldError.ConfirmDoesNotMatch -> R.string.field_confirm_mismatch
}

@StringRes
fun PasswordStrength.labelRes(): Int? = when (this) {
    PasswordStrength.None -> null
    PasswordStrength.Weak -> R.string.strength_weak
    PasswordStrength.Medium -> R.string.strength_medium
    PasswordStrength.Strong -> R.string.strength_strong
}
