package com.example.hrms.utils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EmployeeUtils {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "@#$%^&+=";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom random = new SecureRandom();

    private EmployeeUtils() {
        // Utility class
    }

    /**
     * Generate unique employee ID
     * Format: EMP-YYYYMMDD-XXXX
     * Example: EMP-20250129-1234
     */
    public static String generateEmployeeId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", random.nextInt(10000));
        return "EMP-" + datePart + "-" + randomPart;
    }

    /**
     * Generate unique employee ID with prefix
     * Format: PREFIX-YYYYMMDD-XXXX
     */
    public static String generateEmployeeId(String prefix) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", random.nextInt(10000));
        return prefix + "-" + datePart + "-" + randomPart;
    }

    /**
     * Generate temporary password
     * Password will contain at least:
     * - 1 uppercase letter
     * - 1 lowercase letter
     * - 1 digit
     * - 1 special character
     * Total length: 12 characters
     */
    public static String generateTemporaryPassword() {
        return generateTemporaryPassword(12);
    }

    /**
     * Generate temporary password with specific length
     */
    public static String generateTemporaryPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }

        StringBuilder password = new StringBuilder(length);

        // Ensure at least one character from each required set
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill the rest with random characters from all sets
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the password to avoid predictable patterns
        return shuffleString(password.toString());
    }

    /**
     * Shuffle a string randomly
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }

    /**
     * Generate username from first and last name
     * Format: firstname.lastname
     * If already exists, append number
     */
    public static String generateUsername(String firstName, String lastName) {
        return (firstName + "." + lastName).toLowerCase().replaceAll("\\s+", "");
    }

    /**
     * Generate username with counter
     */
    public static String generateUsername(String firstName, String lastName, int counter) {
        String baseUsername = generateUsername(firstName, lastName);
        return counter > 0 ? baseUsername + counter : baseUsername;
    }

    /**
     * Validate employee ID format
     */
    public static boolean isValidEmployeeIdFormat(String employeeId) {
        if (employeeId == null) {
            return false;
        }
        // Pattern: EMP-YYYYMMDD-XXXX or PREFIX-YYYYMMDD-XXXX
        return employeeId.matches("^[A-Z]+-\\d{8}-\\d{4}$");
    }

    /**
     * Extract date from employee ID
     */
    public static LocalDate extractDateFromEmployeeId(String employeeId) {
        if (!isValidEmployeeIdFormat(employeeId)) {
            return null;
        }

        String[] parts = employeeId.split("-");
        if (parts.length != 3) {
            return null;
        }

        try {
            return LocalDate.parse(parts[1], DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }
}
