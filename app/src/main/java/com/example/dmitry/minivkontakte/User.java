package com.example.dmitry.minivkontakte;

/**
 * Created by Dmitry on 21.08.2015.
 */
public class User {

    private final String name;
    private final String avatar100;
    private final int uid;

    public User(String name, int uid, String avatar100) {
        this.name = name;

        this.avatar100 = avatar100;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar100;
    }

    public int getUid() {
        return uid;
    }
}
