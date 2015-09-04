package com.dopamin.markod.objects;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class User implements Parcelable {
    private String _id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private int points;
    private String social_id;
    private String loginType;
    private String encodedProfilePhoto;

    public User(String id, String firstName, String lastName, String email,
                String loginType, String social_id, int points) {
        this._id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.points = points;
        this.social_id = social_id;
        this.loginType = loginType;
    }

    public User() {
        this.points = 0;
    }

    public User(Parcel parcel) {
        this._id = parcel.readString();
        this.firstName = parcel.readString();
        this.lastName = parcel.readString();
        this.email = parcel.readString();
        this.username = parcel.readString();
        this.points = parcel.readInt();
        this.social_id = parcel.readString();
        this.loginType = parcel.readString();
        this.encodedProfilePhoto = parcel.readString();
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

    public String getSocial_id() {
        return social_id;
    }

    public void setSocial_id(String social_id) {
        this.social_id = social_id;
    }

    public String getEncodedProfilePhoto() {
        return encodedProfilePhoto;
    }

    public void setEncodedProfilePhoto(String encodedProfilePhoto) {
        this.encodedProfilePhoto = encodedProfilePhoto;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public void incPoints(int p) {
        this.points += p;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_id);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(email);
        parcel.writeString(username);
        parcel.writeInt(points);
        parcel.writeString(social_id);
        parcel.writeString(loginType);
        parcel.writeString(encodedProfilePhoto);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };
}
