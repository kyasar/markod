package com.dopamin.markod.objects;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private int points;
    private String token;
    private String id;
    private UserLoginType userLoginType;

    public User(String firstName, String lastName, String token, String id, UserLoginType userLoginType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.id = id;
        this.userLoginType = userLoginType;
        this.points = 0;
    }

    public User(String firstName, String lastName, String email, String username, String token, String id, UserLoginType userLoginType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.token = token;
        this.id = id;
        this.userLoginType = userLoginType;
        this.points = 0;
    }

    public User() {
        this.points = 0;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserLoginType getUserLoginType() {
        return userLoginType;
    }

    public void setUserLoginType(UserLoginType userLoginType) {
        this.userLoginType = userLoginType;
    }
}
