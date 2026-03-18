package com.example.contacts;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ContactManager {
    private final List<Contact> contacts = new ArrayList<>();
    private final Path storagePath;

    public ContactManager(Path storagePath) {
        this.storagePath = storagePath;
        loadFromFile(); // φόρτωμα στην εκκίνηση
    }

    public List<Contact> getAll() {
        return Collections.unmodifiableList(contacts);
    }

    public Optional<Contact> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String e = email.trim().toLowerCase();
        return contacts.stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(e))
                .findFirst();
    }

    public Optional<Contact> findByPhone(String phone) {
        String norm = Validation.normalizePhone(phone);
        if (norm.isBlank()) return Optional.empty();
        return contacts.stream()
                .filter(c -> c.normalizedPhone().equals(norm))
                .findFirst();
    }

    public List<Contact> searchByNameContains(String namePart) {
        String needle = namePart == null ? "" : namePart.trim().toLowerCase();
        return contacts.stream()
                .filter(c -> c.getName().toLowerCase().contains(needle))
                .collect(Collectors.toList());
    }

    public void add(Contact contact) throws IllegalArgumentException {
        // Validation
        if (!Validation.isValidName(contact.getName()))
            throw new IllegalArgumentException("Μη έγκυρο όνομα (τουλάχιστον 2 χαρακτήρες).");
        if (!Validation.isValidEmail(contact.getEmail()))
            throw new IllegalArgumentException("Μη έγκυρο email.");
        if (!Validation.isValidPhone(contact.getPhone()))
            throw new IllegalArgumentException("Μη έγκυρο τηλέφωνο (10–15 ψηφία).");
        if (!Validation.isValidAddress(contact.getAddress()))
            throw new IllegalArgumentException("Μη έγκυρη διεύθυνση (τουλάχιστον 3 χαρακτήρες).");

        // Έλεγχος διπλών
        if (findByEmail(contact.getEmail()).isPresent())
            throw new IllegalArgumentException("Υπάρχει ήδη επαφή με αυτό το email.");
        if (findByPhone(contact.getPhone()).isPresent())
            throw new IllegalArgumentException("Υπάρχει ήδη επαφή με αυτό το τηλέφωνο.");

        contacts.add(contact);
        saveToFile();
    }

    public void removeByEmailOrPhone(String term) throws NoSuchElementException {
        String e = term.trim().toLowerCase();
        String norm = Validation.normalizePhone(term);

        Optional<Contact> found = contacts.stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(e) || c.normalizedPhone().equals(norm))
                .findFirst();

        if (found.isEmpty()) {
            throw new NoSuchElementException("Δεν βρέθηκε επαφή με αυτό το email ή τηλέφωνο.");
        }

        contacts.remove(found.get());
        saveToFile();
    }

    public List<Contact> search(String criterion, String value) {
        switch ((criterion == null ? "" : criterion.trim().toLowerCase())) {
            case "name":
            case "όνομα":
                return searchByNameContains(value);
            case "email":
                return findByEmail(value).map(List::of).orElse(List.of());
            case "phone":
            case "τηλέφωνο":
                return findByPhone(value).map(List::of).orElse(List.of());
            default:
                return List.of();
        }
    }

    /* ---------------- File Persistence (CSV) ---------------- */

    private void ensureFileExists() {
        try {
            if (Files.notExists(storagePath)) {
                if (storagePath.getParent() != null && Files.notExists(storagePath.getParent())) {
                    Files.createDirectories(storagePath.getParent());
                }
                Files.createFile(storagePath);
                // Γράφουμε header για αναγνωσιμότητα (προαιρετικό)
                Files.writeString(storagePath, "name,email,phone,address\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new RuntimeException("Αποτυχία δημιουργίας αρχείου αποθήκευσης: " + e.getMessage(), e);
        }
    }

    public void loadFromFile() {
        ensureFileExists();
        contacts.clear();

        try (BufferedReader br = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { // παράλειψη header
                    isHeader = false;
                    if (line.toLowerCase().startsWith("name,email,phone,address")) continue;
                }
                if (line.isBlank()) continue;
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 4) continue;
                Contact c = new Contact(cols.get(0), cols.get(1), cols.get(2), cols.get(3));
                // Μην κάνεις validation/duplicate check στο load, εμπιστεύσου το αρχείο
                contacts.add(c);
            }
        } catch (IOException e) {
            throw new RuntimeException("Σφάλμα ανάγνωσης αρχείου: " + e.getMessage(), e);
        }
    }

    public void saveToFile() {
        ensureFileExists();

        try (BufferedWriter bw = Files.newBufferedWriter(storagePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write("name,email,phone,address");
            bw.newLine();
            for (Contact c : contacts) {
                bw.write(c.toCsvLine());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Σφάλμα εγγραφής αρχείου: " + e.getMessage(), e);
        }
    }

    /* --- Απλός CSV parser που σέβεται εισαγωγικά (") --- */
    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"'); // escaped "
                        i++;
                    } else {
                        inQuotes = false; // τέλος εισαγωγικών
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    result.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        result.add(cur.toString());
        return result;
    }
}
