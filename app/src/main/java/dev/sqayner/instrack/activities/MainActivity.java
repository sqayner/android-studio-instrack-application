package dev.sqayner.instrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.requests.users.UsersInfoRequest;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import dev.sqayner.instrack.App;
import dev.sqayner.instrack.databinding.ActivityMainBinding;
import dev.sqayner.instrack.dialogs.LoadingDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static List<Profile> followers, followings;
    public static Profile currentUser;

    private ActivityMainBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    protected void onStart() {
        super.onStart();

        Locale.setDefault(Locale.US);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingDialog = new LoadingDialog(this);

        binding.mainLlLl1.setOnClickListener(this);
        binding.mainLlLl2.setOnClickListener(this);
        binding.mainLlLl3.setOnClickListener(this);

        fetch();

        final Profile selfProfile = App.client.getSelfProfile();
        binding.usernameTv.setText(selfProfile.getUsername());
        if (selfProfile.getFull_name().equals(""))
            binding.displayNameTv.setVisibility(View.GONE);
        else
            binding.displayNameTv.setText(selfProfile.getFull_name());
        Picasso.get().load(selfProfile.getProfile_pic_url()).into(binding.photoIv);

        binding.refreshIb.setOnClickListener(this);
        binding.logoutIb.setOnClickListener(this);
    }

    private void fetch() {
        loadingDialog.show();

        fetchProfileData().subscribe(new Observer<Boolean>() {
            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Boolean aBoolean) {
                Picasso.get().load(currentUser.getProfile_pic_url()).into(binding.photoIv);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }

            @Override
            public void onComplete() {
                loadingDialog.dismiss();
            }
        });
    }

    private Observable<Boolean> fetchProfileData() {
        return Observable
                .fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        currentUser = App.client.sendRequest(new UsersInfoRequest(App.client.getSelfProfile().getPk())).join().getUser();

                        followers = App.client.actions().users().findByUsername(App.client.getSelfProfile().getUsername()).thenApply(userAction -> userAction.followersFeed().stream().flatMap(feedUsersResponse -> feedUsersResponse.getUsers().stream()).collect(Collectors.toList())).get();
                        followings = App.client.actions().users().findByUsername(App.client.getSelfProfile().getUsername()).thenApply(userAction -> userAction.followingFeed().stream().flatMap(feedUsersResponse -> feedUsersResponse.getUsers().stream()).collect(Collectors.toList())).get();

                        return true;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == binding.mainLlLl1.getId()) {
            ProfileListActivity.TYPE = ProfileListActivity.TYPES.MUTUAL_FOLLOWING;
            startActivity(new Intent(MainActivity.this, ProfileListActivity.class));
        } else if (v.getId() == binding.mainLlLl2.getId()) {
            ProfileListActivity.TYPE = ProfileListActivity.TYPES.THE_ONES_I_DONT_FOLLOW;
            startActivity(new Intent(MainActivity.this, ProfileListActivity.class));
        } else if (v.getId() == binding.mainLlLl3.getId()) {
            ProfileListActivity.TYPE = ProfileListActivity.TYPES.THOSE_WHO_DO_NOT_FOLLOW_ME;
            startActivity(new Intent(MainActivity.this, ProfileListActivity.class));
        } else if (v.getId() == binding.refreshIb.getId()) {
            fetch();
        } else if (v.getId() == binding.logoutIb.getId()) {
            File client = new File(getFilesDir(), "client");
            File cookie = new File(getFilesDir(), "cookie");

            if (cookie.delete() && client.delete()) {
                App.client = null;

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}