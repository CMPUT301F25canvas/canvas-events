package com.example.lotteryeventsystem;

/**
 * Represents a user in the system with their personal information
 * This class maps to documents in the Firestore 'users' collection
 */
public class User {
    // Firestore document ID that uniquely identifies this user
    private String id;
    // User's full name for display purposes
    private String name;
    // User's email address for contact
    private String email;
    // User's phone number (optional field)
    private String phone;

    /**
     * Default constructor required for Firestore
     * Firestore uses this to create empty objects and then populate fields
     */
    public User() {
        // Empty constructor needed for Firestore data mapping
    }

    /**
     * Convenience constructor for creating User objects with basic information
     * @param id The unique user ID (Firestore document ID)
     * @param name The user's full name
     * @param email The user's email address
     */
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        // Phone remains null unless set separately
    }

    // Getter for user ID
    public String getId() { return id; }

    // Setter for user ID
    public void setId(String id) { this.id = id; }

    // Getter for user's name
    public String getName() { return name; }

    // Setter for user's name
    public void setName(String name) { this.name = name; }

    // Getter for user's email
    public String getEmail() { return email; }

    // Setter for user's email
    public void setEmail(String email) { this.email = email; }

    // Getter for user's phone number
    public String getPhone() { return phone; }

    // Setter for user's phone number
    public void setPhone(String phone) { this.phone = phone; }
}