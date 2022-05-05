package dev.sqayner.instrack;

import android.app.Application;

import com.github.instagram4j.instagram4j.IGClient;

import java.util.Locale;

public class App extends Application {

    public static IGClient client;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
