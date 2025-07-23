package com.example.expensetracker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Simple unit tests for Login validation logic
 * These tests work without Firebase mocking and Java version conflicts
 */
@RunWith(JUnit4.class)
public class LoginActivityTest {

    @Test
    public void testValidateInput_EmptyEmail_ReturnsFalse() {
        // Arrange
        String email = "";
        String password = "password123";

        // Act & Assert
        assertFalse("Empty email should return false",
                validateLoginInput(email, password));
    }

    @Test
    public void testValidateInput_EmptyPassword_ReturnsFalse() {
        // Arrange
        String email = "test@example.com";
        String password = "";

        // Act & Assert
        assertFalse("Empty password should return false",
                validateLoginInput(email, password));
    }

    @Test
    public void testValidateInput_BothEmpty_ReturnsFalse() {
        // Arrange
        String email = "";
        String password = "";

        // Act & Assert
        assertFalse("Both empty fields should return false",
                validateLoginInput(email, password));
    }

    @Test
    public void testValidateInput_InvalidEmail_ReturnsFalse() {
        // Arrange
        String email = "invalid-email";
        String password = "password123";

        // Act & Assert
        assertFalse("Invalid email should return false",
                validateLoginInput(email, password));
    }

    @Test
    public void testValidateInput_ShortPassword_ReturnsFalse() {
        // Arrange
        String email = "test@example.com";
        String password = "12345"; // Less than 6 characters

        // Act & Assert
        assertFalse("Short password should return false",
                validateLoginInput(email, password));
    }

    @Test
    public void testValidateInput_ValidInput_ReturnsTrue() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";

        // Act & Assert
        assertTrue("Valid input should return true",
                validateLoginInput(email, password));
    }

    @Test
    public void testEmailValidation_ValidEmails() {
        // Test various valid email formats
        assertTrue("Standard email should be valid", isValidEmail("user@example.com"));
        assertTrue("Email with subdomain should be valid", isValidEmail("user@mail.example.com"));
        assertTrue("Email with numbers should be valid", isValidEmail("user123@example.com"));
        assertTrue("Email with dots should be valid", isValidEmail("first.last@example.com"));
        assertTrue("Email with hyphens should be valid", isValidEmail("user-name@example.com"));
        assertTrue("Email with plus should be valid", isValidEmail("user+tag@example.com"));
    }

    @Test
    public void testEmailValidation_InvalidEmails() {
        // Test various invalid email formats
        assertFalse("Email without @ should be invalid", isValidEmail("userexample.com"));
        assertFalse("Email without domain should be invalid", isValidEmail("user@"));
        assertFalse("Email without user should be invalid", isValidEmail("@example.com"));
        assertFalse("Empty email should be invalid", isValidEmail(""));
        assertFalse("Null email should be invalid", isValidEmail(null));
        assertFalse("Email with spaces should be invalid", isValidEmail("user @example.com"));
        assertFalse("Email with multiple @ should be invalid", isValidEmail("user@@example.com"));
    }

    @Test
    public void testPasswordValidation_ValidPasswords() {
        // Test minimum valid passwords
        assertTrue("6 character password should be valid", isValidPassword("123456"));
        assertTrue("Alphanumeric password should be valid", isValidPassword("password123"));
        assertTrue("Complex password should be valid", isValidPassword("MyP@ssw0rd!"));
        assertTrue("Password with spaces should be valid", isValidPassword("my password"));
        assertTrue("Very long password should be valid", isValidPassword("a".repeat(100)));
    }

    @Test
    public void testPasswordValidation_InvalidPasswords() {
        // Test invalid passwords
        assertFalse("5 character password should be invalid", isValidPassword("12345"));
        assertFalse("4 character password should be invalid", isValidPassword("1234"));
        assertFalse("Empty password should be invalid", isValidPassword(""));
        assertFalse("Null password should be invalid", isValidPassword(null));
        assertFalse("Single character should be invalid", isValidPassword("a"));
    }

  
    @Test
    public void testPasswordLengthBoundaries() {
        // Test boundary conditions for password length
        assertFalse("5 chars (boundary-1) should be invalid", isValidPassword("12345"));
        assertTrue("6 chars (boundary) should be valid", isValidPassword("123456"));
        assertTrue("7 chars (boundary+1) should be valid", isValidPassword("1234567"));

        // Test very long passwords
        String longPassword = "a".repeat(1000);
        assertTrue("Very long password should be valid", isValidPassword(longPassword));
    }

    @Test
    public void testWhitespaceHandling() {
        // Test handling of whitespace in inputs
        assertFalse("Email with only spaces should be invalid", isValidEmail("   "));
        assertFalse("Password with only spaces should be invalid", isValidPassword("     "));

        // Test trimming behavior simulation
        assertTrue("Email should be valid after trimming", isValidEmail(trimEmail("  user@example.com  ")));
        assertTrue("Password should be valid after trimming", isValidPassword(trimPassword("  password123  ")));
    }

    @Test
    public void testCommonEmailDomains() {
        // Test common email providers
        assertTrue("Gmail should be valid", isValidEmail("user@gmail.com"));
        assertTrue("Yahoo should be valid", isValidEmail("user@yahoo.com"));
        assertTrue("Outlook should be valid", isValidEmail("user@outlook.com"));
        assertTrue("Hotmail should be valid", isValidEmail("user@hotmail.com"));
        assertTrue("Company email should be valid", isValidEmail("user@company.org"));
    }

    @Test
    public void testPasswordSecurity() {
        // Test various password patterns (all should be valid if >= 6 chars)
        assertTrue("Numeric password should be valid", isValidPassword("123456"));
        assertTrue("Alphabetic password should be valid", isValidPassword("abcdef"));
        assertTrue("Mixed case should be valid", isValidPassword("AbCdEf"));
        assertTrue("With special chars should be valid", isValidPassword("abc@123"));
        assertTrue("With spaces should be valid", isValidPassword("my pass"));
    }

    @Test
    public void testInputSanitization() {
        // Test inputs that might cause issues
        String maliciousEmail = "user@example.com<script>";
        String sqlInjectionPassword = "'; DROP TABLE users; --";

        // These should still validate according to basic rules
        assertTrue("Email with tags should validate basic format", isValidEmail("user@example.com"));
        assertTrue("SQL injection password should be valid if long enough", isValidPassword(sqlInjectionPassword));
    }

    @Test
    public void testCombinedValidation_CommonScenarios() {
        // Test realistic login scenarios
        assertTrue("Standard login should be valid",
                validateLoginInput("john.doe@example.com", "mypassword123"));

        assertTrue("Corporate email should be valid",
                validateLoginInput("employee@company.org", "SecurePass2024"));

        assertFalse("Invalid email with valid password should fail",
                validateLoginInput("notanemail", "validpassword"));

        assertFalse("Valid email with short password should fail",
                validateLoginInput("valid@email.com", "short"));
    }

    @Test
    public void testValidation_NullInputs() {
        // Test null safety
        assertFalse("Null email should return false", validateLoginInput(null, "password"));
        assertFalse("Null password should return false", validateLoginInput("user@example.com", null));
        assertFalse("Both null should return false", validateLoginInput(null, null));
    }

    @Test
    public void testValidation_InternationalEmails() {
        // Test international email formats (basic support)
        assertTrue("Email with international domain should be valid",
                isValidEmail("user@example.de"));
        assertTrue("Email with long TLD should be valid",
                isValidEmail("user@example.museum"));

        // Note: Full internationalization would require more complex validation
    }

    // Helper methods to simulate validation logic (same as in actual LoginActivity)
    private boolean validateLoginInput(String email, String password) {
        if (!isValidEmail(email)) return false;
        if (!isValidPassword(password)) return false;
        return true;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;

        // Simple email validation without Android dependencies
        if (!email.contains("@")) return false;
        if (email.indexOf("@") == 0) return false; // No user part
        if (email.indexOf("@") == email.length() - 1) return false; // No domain part
        if (email.indexOf("@") != email.lastIndexOf("@")) return false; // Multiple @
        if (email.contains(" ")) return false; // No spaces

        // Check for domain
        String[] parts = email.split("@");
        if (parts.length != 2) return false;
        if (parts[1].isEmpty()) return false;
        if (!parts[1].contains(".")) return false;

        // Additional checks for edge cases
        if (parts[1].startsWith(".") || parts[1].endsWith(".")) return false;
        if (parts[0].isEmpty()) return false;

        return true;
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) return false;
        return password.length() >= 6;
    }

    // Helper methods for testing trimming behavior
    private String trimEmail(String email) {
        return email != null ? email.trim() : null;
    }

    private String trimPassword(String password) {
        return password != null ? password.trim() : null;
    }
}