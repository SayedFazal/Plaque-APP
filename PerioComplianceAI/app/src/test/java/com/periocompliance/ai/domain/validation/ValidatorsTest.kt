package com.periocompliance.ai.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {

    // --- name -----------------------------------------------------------------

    @Test
    fun `name is required`() {
        assertEquals(FieldError.Required, validateName(""))
        assertEquals(FieldError.Required, validateName("   "))
    }

    @Test
    fun `name must be at least three characters`() {
        assertEquals(FieldError.NameTooShort, validateName("Jo"))
        assertNull(validateName("Joe"))
    }

    @Test
    fun `name must be at most forty characters`() {
        assertNull(validateName("a".repeat(40)))
        assertEquals(FieldError.NameTooLong, validateName("a".repeat(41)))
    }

    @Test
    fun `name length is measured after trimming`() {
        // "  Jo  " is six characters raw but two real ones -- padding must not satisfy the minimum.
        assertEquals(FieldError.NameTooShort, validateName("  Jo  "))
    }

    // --- email ----------------------------------------------------------------

    @Test
    fun `valid emails pass`() {
        listOf(
            "dr.smith@clinic.com",
            "jane.smith+perio@practice.co.uk",
            "a@b.io",
            "first_last@sub.domain.example",
        ).forEach { assertNull("expected $it to be valid", validateEmail(it)) }
    }

    @Test
    fun `malformed emails fail`() {
        listOf(
            "plainaddress",
            "@no-local.com",
            "no-at-sign.com",
            "spaces in@email.com",
            "trailing@dot.",
            "two@@ats.com",
            "no-tld@domain",
        ).forEach { assertEquals("expected $it to be rejected", FieldError.EmailMalformed, validateEmail(it)) }
    }

    // --- password -------------------------------------------------------------

    @Test
    fun `password must satisfy every rule`() {
        assertEquals(FieldError.PasswordTooShort, validatePassword("Ab1!"))
        assertEquals(FieldError.PasswordTooLong, validatePassword("Ab1!".repeat(9)))
        assertEquals(FieldError.PasswordNeedsUppercase, validatePassword("lowercase1!"))
        assertEquals(FieldError.PasswordNeedsLowercase, validatePassword("UPPERCASE1!"))
        assertEquals(FieldError.PasswordNeedsDigit, validatePassword("NoDigitsHere!"))
        assertEquals(FieldError.PasswordNeedsSpecial, validatePassword("NoSpecial123"))
        assertNull(validatePassword("Str0ng!Pass"))
    }

    @Test
    fun `password reports the first unmet rule only`() {
        // Short AND missing everything -- the user is told about the length first.
        assertEquals(FieldError.PasswordTooShort, validatePassword("ab"))
    }

    // --- confirm password -----------------------------------------------------

    @Test
    fun `confirm must match`() {
        assertEquals(FieldError.Required, validateConfirmPassword("Str0ng!Pass", ""))
        assertEquals(
            FieldError.ConfirmDoesNotMatch,
            validateConfirmPassword("Str0ng!Pass", "Str0ng!Pas"),
        )
        assertNull(validateConfirmPassword("Str0ng!Pass", "Str0ng!Pass"))
    }

    // --- strength -------------------------------------------------------------

    @Test
    fun `strength climbs with character classes and length`() {
        assertEquals(PasswordStrength.None, passwordStrength(""))
        assertEquals(PasswordStrength.Weak, passwordStrength("abc"))
        assertEquals(PasswordStrength.Weak, passwordStrength("abcdefgh"))
        // 8 chars, all four classes -> exactly the minimum bar.
        assertEquals(PasswordStrength.Medium, passwordStrength("Abcd123!"))
        // 12+ chars with all four classes clears it.
        assertEquals(PasswordStrength.Strong, passwordStrength("Abcdefg123!@"))
    }

    @Test
    fun `a password below the minimum length is always weak`() {
        // All four classes but only seven characters: rules unmet, so the meter must not flatter it.
        assertEquals(PasswordStrength.Weak, passwordStrength("Ab1!xyz"))
    }
}
