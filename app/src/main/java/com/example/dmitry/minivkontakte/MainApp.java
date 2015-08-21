package com.example.dmitry.minivkontakte;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.*;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

public class MainApp extends android.app.Application {

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(@Nullable VKAccessToken oldToken, @Nullable VKAccessToken newToken) {
            if (newToken == null) {
                Toast.makeText(MainApp.this, "Признак доступа(пароль) недействителен", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainApp.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

        Toast.makeText(MainApp.this, "УРА!", Toast.LENGTH_LONG).show();
    }

}
