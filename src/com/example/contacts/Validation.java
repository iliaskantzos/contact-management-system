package com.example.contacts;

import java.util.regex.Pattern;

public class Validation {
    // Απλό, αξιόπιστο regex email (όχι υπερβολικά αυστηρό)
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    // Επιτρέπουμε +, (), space, -, αλλά μετράμε 10–15 ψηφία
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        email = email.trim();
        return !email.isBlank() && EMAIL_REGEX.matcher(email).matches();
    }

    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        // Κρατάμε μόνο ψηφία για σύγκριση/αναζήτηση
        return phone.replaceAll("\\D", "");
    }

    public static boolean isValidPhone(String phone) {
        String digits = normalizePhone(phone);
        return !digits.isBlank() && digits.length() >= 10 && digits.length() <= 15 && DIGITS_ONLY.matcher(digits).matches();
    }

    public static boolean isValidAddress(String address) {
        return address != null && address.trim().length() >= 3;
    }
}
