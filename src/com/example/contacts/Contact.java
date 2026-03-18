package com.example.contacts;

import java.util.Objects;

public class Contact {
    private String name;
    private String email;
    private String phone;
    private String address;

    public Contact(String name, String email, String phone, String address) {
        this.name = name.trim();
        this.email = email.trim();
        this.phone = phone.trim();
        this.address = address.trim();
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    public void setName(String name) { this.name = name.trim(); }
    public void setEmail(String email) { this.email = email.trim(); }
    public void setPhone(String phone) { this.phone = phone.trim(); }
    public void setAddress(String address) { this.address = address.trim(); }

    /** Χρήσιμο για σύγκριση διπλοεγγραφών: email ή/και normalized phone */
    public String normalizedPhone() {
        return Validation.normalizePhone(this.phone);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact)) return false;
        Contact contact = (Contact) o;
        // Δύο επαφές θεωρούνται ίδιες αν έχουν ίδιο email ή ίδιο normalized τηλέφωνο
        boolean emailMatch = !this.email.isBlank() && this.email.equalsIgnoreCase(contact.email);
        boolean phoneMatch = !this.normalizedPhone().isBlank() &&
                this.normalizedPhone().equals(contact.normalizedPhone());
        return emailMatch || phoneMatch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email.toLowerCase(),
                normalizedPhone().isBlank() ? "" : normalizedPhone());
    }

    /** CSV escape για πεδία με κόμμα/εισαγωγικά/νέα γραμμή */
    public static String escapeCsv(String value) {
        if (value == null) return "";
        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        return mustQuote ? "\"" + v + "\"" : v;
    }

    /** Μετατροπή σε CSV γραμμή */
    public String toCsvLine() {
        return String.join(",",
                escapeCsv(name),
                escapeCsv(email),
                escapeCsv(phone),
                escapeCsv(address)
        );
    }

    @Override
    public String toString() {
        return "Όνομα: " + name +
                "\nEmail: " + email +
                "\nΤηλέφωνο: " + phone +
                "\nΔιεύθυνση: " + address;
    }
}
