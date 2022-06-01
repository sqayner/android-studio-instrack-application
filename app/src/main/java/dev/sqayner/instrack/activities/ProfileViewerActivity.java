package dev.sqayner.instrack.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.requests.users.UsersUsernameInfoRequest;
import com.github.instagram4j.instagram4j.responses.users.UserResponse;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import dev.sqayner.instrack.App;
import dev.sqayner.instrack.databinding.ActivityProfileViewerBinding;
import dev.sqayner.instrack.dialogs.LoadingDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfileViewerActivity extends AppCompatActivity {

    private Profile user;

    private ActivityProfileViewerBinding binding;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this);

        user = (Profile) getIntent().getExtras().getSerializable("user");

        Picasso.get().load(user.getProfile_pic_url()).into(binding.profilePhoto);

        binding.username.setText(user.getUsername());

        binding.fullname.setVisibility(user.getFull_name().equals("") ? View.GONE : View.VISIBLE);
        binding.fullname.setText(user.getFull_name());

        fetchAccountData();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.openInInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(MessageFormat.format("https://www.instagram.com/{0}", user.getUsername()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void fetchAccountData() {
        getAccount(user.getUsername()).subscribe(new Observer<UserResponse>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                loadingDialog.show();
            }

            @Override
            public void onNext(@NonNull UserResponse userResponse) {
                binding.description.setVisibility(userResponse.getUser().getBiography().equals("") ? View.GONE : View.VISIBLE);
                binding.description.setText(userResponse.getUser().getBiography());

                binding.followersCountTv.setText(MessageFormat.format("{0}\nTakipçi", userResponse.getUser().getFollower_count()));
                binding.followingCountTv.setText(MessageFormat.format("{0}\nTakip", userResponse.getUser().getFollowing_count()));
                binding.mediaCountTv.setText(MessageFormat.format("{0}\nMedya", userResponse.getUser().getMedia_count()));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                loadingDialog.dismiss();
            }
        });
    }

    private Observable<UserResponse> getAccount(String username) {
        return Observable
                .fromCallable(new Callable<UserResponse>() {
                    @Override
                    public UserResponse call() throws ExecutionException, InterruptedException {
                        return App.client.sendRequest(new UsersUsernameInfoRequest(username)).join();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}