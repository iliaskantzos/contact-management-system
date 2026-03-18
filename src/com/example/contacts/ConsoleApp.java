package com.example.contacts;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleApp {

    private static void printMenu() {
        System.out.println("\n===== Διαχείριση Επαφών =====");
        System.out.println("1) Προσθήκη επαφής");
        System.out.println("2) Αφαίρεση επαφής (με email ή τηλέφωνο)");
        System.out.println("3) Αναζήτηση επαφών");
        System.out.println("4) Προβολή όλων των επαφών");
        System.out.println("5) Επαναφόρτωση από αρχείο");
        System.out.println("0) Έξοδος");
        System.out.print("Επιλογή: ");
    }

    private static String prompt(Scanner sc, String label) {
        System.out.print(label + ": ");
        return sc.nextLine();
    }

    public static void main(String[] args) {
        // Αποθήκευση στο ./data/contacts.csv (σχετικό με τον φάκελο του project)
        Path storage = Path.of("data", "contacts.csv");
        ContactManager manager = new ContactManager(storage);

        System.out.println("Αρχείο αποθήκευσης: " + storage.toAbsolutePath());
        System.out.println("Φορτώθηκαν " + manager.getAll().size() + " επαφές.");

        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": { // add
                    String name = prompt(sc, "Όνομα");
                    String email = prompt(sc, "Email");
                    String phone = prompt(sc, "Τηλέφωνο (π.χ. +30 69XXXXXXXX)");
                    String address = prompt(sc, "Διεύθυνση");

                    try {
                        Contact c = new Contact(name, email, phone, address);
                        manager.add(c);
                        System.out.println("✅ Η επαφή προστέθηκε επιτυχώς.");
                    } catch (IllegalArgumentException ex) {
                        System.out.println("❌ Σφάλμα: " + ex.getMessage());
                    }
                    break;
                }
                case "2": { // remove
                    String term = prompt(sc, "Δώσε email ή τηλέφωνο της επαφής για διαγραφή");
                    try {
                        manager.removeByEmailOrPhone(term);
                        System.out.println("🗑️ Η επαφή αφαιρέθηκε.");
                    } catch (NoSuchElementException ex) {
                        System.out.println("❌ Σφάλμα: " + ex.getMessage());
                    }
                    break;
                }
                case "3": { // search
                    System.out.println("Κριτήριο: name | email | phone");
                    String criterion = prompt(sc, "Κριτήριο");
                    String value = prompt(sc, "Τιμή αναζήτησης");

                    List<Contact> results = manager.search(criterion, value);
                    if (results.isEmpty()) {
                        System.out.println("— Δεν βρέθηκαν αποτελέσματα —");
                    } else {
                        System.out.println("Βρέθηκαν " + results.size() + " αποτελέσματα:\n");
                        for (int i = 0; i < results.size(); i++) {
                            Contact c = results.get(i);
                            System.out.println("[" + (i + 1) + "]");
                            System.out.println(c);
                            System.out.println("------------------------------");
                        }
                    }
                    break;
                }
                case "4": { // list all
                    List<Contact> all = manager.getAll();
                    if (all.isEmpty()) {
                        System.out.println("Η λίστα επαφών είναι άδεια.");
                    } else {
                        System.out.println("Σύνολο επαφών: " + all.size());
                        System.out.println();
                        int i = 1;
                        for (Contact c : all) {
                            System.out.println("[" + i++ + "]");
                            System.out.println(c);
                            System.out.println("------------------------------");
                        }
                    }
                    break;
                }
                case "5": { // reload
                    manager.loadFromFile();
                    System.out.println("🔄 Επαναφόρτωση ολοκληρώθηκε. Επαφές: " + manager.getAll().size());
                    break;
                }
                case "0":
                    System.out.println("Αντίο! 👋");
                    return;
                default:
                    System.out.println("Μη έγκυρη επιλογή. Προσπάθησε ξανά.");
            }
        }
    }
}
