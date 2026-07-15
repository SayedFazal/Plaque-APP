package com.periocompliance.ai.domain.validation

/**
 * Field rules from the Module 1 spec. Pure Kotlin -- no Android, no Compose, no Firebase -- so the
 * whole thing is unit-testable without an emulator. See ValidatorsTest.
 */

enum class FieldError {
    Required,
    NameTooShort,
    NameTooLong,
    EmailMalformed,
    PasswordTooShort,
    PasswordTooLong,
    PasswordNeedsUppercase,
    PasswordNeedsLowercase,
    PasswordNeedsDigit,
    PasswordNeedsSpecial,
    ConfirmDoesNotMatch,
}

enum class PasswordStrength { None, Weak, Medium, Strong }

object Rules {
    const val NAME_MIN = 3
    const val NAME_MAX = 40
    const val PASSWORD_MIN = 8
    const val PASSWORD_MAX = 32
}

/**
 * Practical RFC 5322: one @, a local part, a dotted domain, no whitespace, TLD of 2+.
 * The full grammar allows quoted strings and comments that no dental practice will ever type, and
 * matching it exactly rejects nothing useful while inviting catastrophic backtracking.
 */
private val EMAIL_REGEX = Regex(
    "^[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+" +
        "(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*" +
        "@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?" +
        "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+\$",
)

fun validateName(raw: String): FieldError? {
    val name = raw.trim()
    return when {
        name.isEmpty() -> FieldError.Required
        name.length < Rules.NAME_MIN -> FieldError.NameTooShort
        name.length > Rules.NAME_MAX -> FieldError.NameTooLong
        else -> null
    }
}

fun validateEmail(raw: String): FieldError? {
    val email = raw.trim()
    return when {
        email.isEmpty() -> FieldError.Required
        !EMAIL_REGEX.matches(email) -> FieldError.EmailMalformed
        else -> null
    }
}

/**
 * Returns the *first* unmet requirement, not all of them. Showing a dentist five red lines at once
 * is a worse experience than walking them up one step at a time.
 */
fun validatePassword(password: String): FieldError? = when {
    password.isEmpty() -> FieldError.Required
    password.length < Rules.PASSWORD_MIN -> FieldError.PasswordTooShort
    password.length > Rules.PASSWORD_MAX -> FieldError.PasswordTooLong
    password.none { it.isUpperCase() } -> FieldError.PasswordNeedsUppercase
    password.none { it.isLowerCase() } -> FieldError.PasswordNeedsLowercase
    password.none { it.isDigit() } -> FieldError.PasswordNeedsDigit
    password.none { !it.isLetterOrDigit() } -> FieldError.PasswordNeedsSpecial
    else -> null
}

fun validateConfirmPassword(password: String, confirm: String): FieldError? = when {
    confirm.isEmpty() -> FieldError.Required
    confirm != password -> FieldError.ConfirmDoesNotMatch
    else -> null
}

/**
 * Strength is a separate axis from validity: a password can satisfy every rule and still be
 * mediocre. Scored on the four character classes plus length, so the meter keeps climbing after
 * the password has technically become acceptable -- which is the only thing that makes a strength
 * meter worth showing.
 */
fun passwordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.None

    var score = 0
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    if (password.length >= 12) score++
    if (password.length >= 16) score++

    return when {
        password.length < Rules.PASSWORD_MIN -> PasswordStrength.Weak
        score <= 3 -> PasswordStrength.Weak
        score == 4 -> PasswordStrength.Medium
        else -> PasswordStrength.Strong
    }
}
