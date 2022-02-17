package dev.sqayner.instrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.instagram4j.instagram4j.models.user.Profile;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import dev.sqayner.instrack.App;
import dev.sqayner.instrack.R;
import dev.sqayner.instrack.dialogs.LoadingDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static List<Profile> followers, followings;
    private LinearLayout ll1, ll2, ll3;
    private int increase = 0;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Locale.setDefault(Locale.US);

        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        ll1 = findViewById(R.id.main_ll_ll1);
        ll2 = findViewById(R.id.main_ll_ll2);
        ll3 = findViewById(R.id.main_ll_ll3);

        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileListActivity.TYPE = ProfileListActivity.TYPES.MUTUAL_FOLLOWING;
                startActivity(new Intent(MainActivity.this, ProfileListActivity.class));
            }
        });
        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileListActivity.TYPE = ProfileListActivity.TYPES.THE_ONES_I_DONT_FOLLOW;
                startActivity(new Intent(MainActivity.this, ProfileListActivity.class));
            }
        });
        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileListActivity.TYPE = ProfileListActivity.TYPES.THOSE_WHO_DO_NOT_FOLLOW_ME;
                startActivity(new Intent(MainActivity.this, ProfileListActivity.class));
            }
        });

        getFollowers().subscribe(new Observer<List<Profile>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull List<Profile> profiles) {
                followers = profiles;
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                increase();
            }
        });

        getFollowings().subscribe(new Observer<List<Profile>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull List<Profile> profiles) {
                followings = profiles;
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                increase();
            }
        });
    }

    private void increase() {
        increase++;
        if (increase == 2) {
            loadingDialog.dismiss();
        }
    }

    private Observable<List<Profile>> getFollowers() {
        return Observable
                .fromCallable(new Callable<List<Profile>>() {
                    @Override
                    public List<Profile> call() throws ExecutionException, InterruptedException {
                        return App.client
                                .actions()
                                .users()
                                .findByUsername(App.username)
                                .thenApply(userAction -> userAction.followersFeed().stream().flatMap(feedUsersResponse -> feedUsersResponse.getUsers().stream()).collect(Collectors.toList()))
                                .get();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<List<Profile>> getFollowings() {
        return Observable
                .fromCallable(new Callable<List<Profile>>() {
                    @Override
                    public List<Profile> call() throws ExecutionException, InterruptedException {
                        return App.client
                                .actions()
                                .users()
                                .findByUsername(App.username)
                                .thenApply(userAction -> userAction.followingFeed().stream().flatMap(feedUsersResponse -> feedUsersResponse.getUsers().stream()).collect(Collectors.toList()))
                                .get();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}