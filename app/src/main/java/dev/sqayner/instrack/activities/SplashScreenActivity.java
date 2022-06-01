package dev.sqayner.instrack.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.instagram4j.instagram4j.IGClient;

import java.io.File;
import java.util.concurrent.Callable;

import dev.sqayner.instrack.App;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File client = new File(getFilesDir(), "client");
        File cookie = new File(getFilesDir(), "cookie");

        if (cookie.exists() && client.exists())
            loginSavedAccount().subscribe(new Observer<IGClient>() {
                @Override
                public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                }

                @Override
                public void onNext(@io.reactivex.rxjava3.annotations.NonNull IGClient igClient) {
                    if (igClient.isLoggedIn()) {
                        App.client = igClient;
                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    } else {
                        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                    }
                    finish();
                }

                @Override
                public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {

                }
            });
        else
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
    }

    private Observable<IGClient> loginSavedAccount() {
        return Observable
                .fromCallable(new Callable<IGClient>() {
                    @Override
                    public IGClient call() throws Exception {
                        IGClient deserializedClient = IGClient.deserialize(new File(getFilesDir(), "client"), new File(getFilesDir(), "cookie"));
                        if (deserializedClient.isLoggedIn())
                            return deserializedClient;
                        else
                            deserializedClient.sendLoginRequest();
                        return deserializedClient;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}