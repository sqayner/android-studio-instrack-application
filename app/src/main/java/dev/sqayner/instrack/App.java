package dev.sqayner.instrack;

import android.app.Application;

import com.github.instagram4j.instagram4j.IGClient;

import java.util.Locale;

public class App extends Application {

    public static IGClient client;
    public static String username;
    public static String password;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
