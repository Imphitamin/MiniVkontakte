package com.example.dmitry.minivkontakte;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

/**
 * Created by Dmitry on 20.08.2015.
 */

public class LoginActivity extends FragmentActivity {

   private static final String[] sMyScope = new String[] {
           VKScope.FRIENDS,
           VKScope.WALL,
           VKScope.PHOTOS,
           VKScope.NOHTTPS,
           VKScope.MESSAGES,
           VKScope.DOCS
   };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                switch (res) {
                    case LoggedOut:
                        showLogin();
                        break;
                    case LoggedIn:
                        showLogout();
                        break;
                    case Pending:
                        break;
                    case Unknown:
                        break;
                }
            }

            @Override
            public void onError(VKError error) {

            }
        });
    }

    private void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commit();
    }

    private void showLogout() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LogoutFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (VKSdk.isLoggedIn()) {
            showLogout();
        } else {
            showLogin();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь прошел авторизацию
                startMyActivity();
            }

            @Override
            public void onError(VKError error) {

            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startMyActivity() {
        startActivity(new Intent(this, ListScreen.class));
    }

    public static class LoginFragment extends android.support.v4.app.Fragment {
        public LoginFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.login_screen, container, false);
            v.findViewById(R.id.btn_Login).setOnClickListener(new View.OnClickListener() {
                @Override
            public void onClick(View view) {
                    VKSdk.login(getActivity(), sMyScope);
                }
            });
            return v;
        }
    }

    public static class LogoutFragment extends android.support.v4.app.Fragment {
        public LogoutFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.logout_screen, container, false);
            v.findViewById(R.id.btn_Continue).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((LoginActivity) getActivity()).startMyActivity();
                }
            });

            v.findViewById(R.id.btn_Logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.logout();
                    if (!VKSdk.isLoggedIn()) {
                        ((LoginActivity) getActivity()).showLogin();
                    }
                }
            });
            return v;
        }
    }
}
