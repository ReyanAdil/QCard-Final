package com.qcard.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.qcard.data.model.QUser;

public class GlobalData {

    public static final String COL_CATEGORY = "category";
    public static final String COL_SET = "set";
    public static final String COL_CARD = "card";

    public static QUser getCurrentUser(Context context) {
        SharedPreferences session = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        String userString = session.getString("user", "");
        return new Gson().fromJson(userString, QUser.class);
    }

    public static void saveCurrentUser(Context context, QUser firebaseUser) {
        SharedPreferences session = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        String userString = new Gson().toJson(firebaseUser);
        session.edit().putString("user", userString).apply();
    }

    public static void deleteSession(Context context) {
        SharedPreferences session = context.getSharedPreferences("session", Context.MODE_PRIVATE);
        session.edit().clear().apply();
    }
}
