package com.bbj.models;

public class User {
    private int id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String phone;
    private String role;
    private boolean profileComplete;

    public User(int id, String username, String firstName, String lastName, String role) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public User(int id, String username, String email, String firstName, String lastName, String gender, String phone, String role, boolean profileComplete) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.phone = phone;
        this.role = role;
        this.profileComplete = profileComplete;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }
}
