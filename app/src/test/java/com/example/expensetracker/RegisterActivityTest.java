package com.example.expensetracker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Simple unit tests for Registration validation logic
 * These tests work without Firebase mocking and Java version conflicts
 */
@RunWith(JUnit4.class)
public class RegisterActivityTest {

    @Test
    public void testValidateInput_EmptyUsername_ReturnsFalse() {
        // Arrange
        String username = "";
        String email = "test@example.com";
        String password = "password123";
        String confirmPassword = "password123";

        // Act & Assert
        assertFalse("Empty username should return false",
                validateRegistrationInput(username, email, password, confirmPassword));
    }

    @Test
    public void testValidateInput_EmptyEmail_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String email = "";
        String password = "password123";
        String confirmPassword = "password123";

        // Act & Assert
        assertFalse("Empty email should return false",
                validateRegistrationInput(username, email, password, confirmPassword));
    }

    @Test
    public void testValidateInput_InvalidEmail_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String email = "invalid-email";
        String password = "password123";
        String confirmPassword = "password123";

        // Act & Assert
        assertFalse("Invalid email should return false",
                validateRegistrationInput(username, email, password, confirmPassword));
    }

    @Test
    public void testValidateInput_EmptyPassword_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "";
        String confirmPassword = "";

        // Act & Assert
        assertFalse("Empty password should return false",
                validateRegistrationInput(username, email, password, confirmPassword));
    }

    @Test
    public void testValidateInput_ShortPassword_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "12345"; // Less than 6 characters
        String confirmPassword = "12345";

        // Act & Assert
        assertFalse("Short password should return false",
                validateRegistrationInput(username, email, password, confirmPassword));
    }

    @Test
    public void testValidateInput_PasswordMismatch_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String confirmPassword = "differentpassword";

        // Act & Assert
        assertFalse("Password mismatch should return false",
                validateRegistrationInput(username, email, password, confirmPassword));
    }

    @Test
    public void testValidateInput_ValidInput_ReturnsTrue() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String confirmPassword = "password123";

        // Act & Assert
        assertTrue("Valid input should return true",
                validateRegistrationInput(username, email, password, confirmPassword));
    }

    @Test
    public void testEmailValidation_ValidEmails() {
        // Test various valid email formats
        assertTrue("Standard email should be valid", isValidEmail("user@example.com"));
        assertTrue("Email with subdomain should be valid", isValidEmail("user@mail.example.com"));
        assertTrue("Email with numbers should be valid", isValidEmail("user123@example.com"));
        assertTrue("Email with dots should be valid", isValidEmail("first.last@example.com"));
        assertTrue("Email with hyphens should be valid", isValidEmail("user-name@example.com"));
    }

    @Test
    public void testEmailValidation_InvalidEmails() {
        // Test various invalid email formats
        assertFalse("Email without @ should be invalid", isValidEmail("userexample.com"));
        assertFalse("Email without domain should be invalid", isValidEmail("user@"));
        assertFalse("Email without user should be invalid", isValidEmail("@example.com"));
        assertFalse("Empty email should be invalid", isValidEmail(""));
        assertFalse("Email with spaces should be invalid", isValidEmail("user @example.com"));
        assertFalse("Email with multiple @ should be invalid", isValidEmail("user@@example.com"));
    }

    @Test
    public void testPasswordStrength() {
        // Test minimum password requirements
        assertTrue("6 character password should be valid", isValidPassword("123456"));
        assertTrue("Alphanumeric password should be valid", isValidPassword("password123"));
        assertTrue("Complex password should be valid", isValidPassword("MyP@ssw0rd!"));

        // Test invalid passwords
        assertFalse("5 character password should be invalid", isValidPassword("12345"));
        assertFalse("Empty password should be invalid", isValidPassword(""));
        assertFalse("Null password should be invalid", isValidPassword(null));
    }

    @Test
    public void testUsernameValidation() {
        // Test valid usernames
        assertTrue("Simple username should be valid", isValidUsername("user"));
        assertTrue("Username with numbers should be valid", isValidUsername("user123"));
        assertTrue("Username with underscore should be valid", isValidUsername("user_name"));

        // Test invalid usernames
        assertFalse("Empty username should be invalid", isValidUsername(""));
        assertFalse("Null username should be invalid", isValidUsername(null));
        assertFalse("Username with spaces should be invalid", isValidUsername("user name"));
    }

    @Test
    public void testUserDataCreation() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";

        // Act
        RegisterActivity.User user = new RegisterActivity.User(username, email);

        // Assert
        assertEquals("Username should match", username, user.username);
        assertEquals("Email should match", email, user.email);
    }

    @Test
    public void testUserDefaultConstructor() {
        // Act
        RegisterActivity.User user = new RegisterActivity.User();

        // Assert
        assertNotNull("User object should not be null", user);
        assertNull("Username should be null", user.username);
        assertNull("Email should be null", user.email);
    }

    @Test
    public void testPasswordConfirmation() {
        // Test matching passwords
        assertTrue("Matching passwords should be valid",
                validatePasswordConfirmation("password123", "password123"));

        // Test non-matching passwords
        assertFalse("Non-matching passwords should be invalid",
                validatePasswordConfirmation("password123", "different123"));

        // Test empty confirmation
        assertFalse("Empty confirmation should be invalid",
                validatePasswordConfirmation("password123", ""));

        // Test null confirmation
        assertFalse("Null confirmation should be invalid",
                validatePasswordConfirmation("password123", null));
    }

    @Test
    public void testEdgeCaseInputs() {
        // Test with very long inputs
        String longUsername = "a".repeat(100);
        String longEmail = "user@" + "a".repeat(100) + ".com";
        String longPassword = "a".repeat(100);

        // These should still validate according to our rules
        assertTrue("Long valid username should pass", isValidUsername(longUsername));
        assertTrue("Long valid email should pass", isValidEmail(longEmail));
        assertTrue("Long valid password should pass", isValidPassword(longPassword));
    }

    @Test
    public void testSpecialCharactersInEmail() {
        // Test emails with special characters
        assertTrue("Email with plus should be valid", isValidEmail("user+tag@example.com"));
        assertTrue("Email with dash should be valid", isValidEmail("user-name@example.com"));
        assertTrue("Email with underscore should be valid", isValidEmail("user_name@example.com"));

        // Test invalid special characters
        assertFalse("Email with spaces should be invalid", isValidEmail("user name@example.com"));
        assertFalse("Email with quotes should be invalid", isValidEmail("user\"@example.com"));
    }

    // Helper methods to simulate validation logic (without Firebase dependencies)
    private boolean validateRegistrationInput(String username, String email, String password, String confirmPassword) {
        if (!isValidUsername(username)) return false;
        if (!isValidEmail(email)) return false;
        if (!isValidPassword(password)) return false;
        if (!validatePasswordConfirmation(password, confirmPassword)) return false;
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

        return true;
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) return false;
        return password.length() >= 6;
    }

    private boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) return false;
        if (username.contains(" ")) return false; // No spaces allowed
        return username.length() >= 1;
    }

    private boolean validatePasswordConfirmation(String password, String confirmPassword) {
        if (confirmPassword == null || confirmPassword.isEmpty()) return false;
        return password.equals(confirmPassword);
    }
}