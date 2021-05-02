package com.qcard.data.model;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

public class QUser implements Serializable {

    private String userId;
    private String fullName;
    private String email;
    private String photoUrl;

    public static QUser fromFirebaseUser(FirebaseUser firebaseUser) {
        QUser qUser = new QUser();
        qUser.setFullName(firebaseUser.getDisplayName());
        qUser.setEmail(firebaseUser.getEmail());
        qUser.setUserId(firebaseUser.getUid());
        if (firebaseUser.getPhotoUrl() != null) {
            qUser.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
        }

        return qUser;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
