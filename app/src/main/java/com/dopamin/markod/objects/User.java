package com.dopamin.markod.objects;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private int points;
    private String token;
    private String id;
    private String social_id;
    private UserLoginType userLoginType;

    private UserLoginType convertToLoginType(String type) {
        if (type.equalsIgnoreCase("facebook_user"))
            return UserLoginType.FACEBOOK_USER;
        else
            return UserLoginType.LOCAL_USER;
    }

    public User(String id, String firstName, String lastName, String email, String userLoginType, String social_id, int points) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.points = points;
        this.id = id;
        this.social_id = social_id;
        this.userLoginType = convertToLoginType(userLoginType);
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

    public String getSocial_id() {
        return social_id;
    }

    public void setSocial_id(String social_id) {
        this.social_id = social_id;
    }

    public UserLoginType getUserLoginType() {
        return userLoginType;
    }

    public void setUserLoginType(UserLoginType userLoginType) {
        this.userLoginType = userLoginType;
    }
}
