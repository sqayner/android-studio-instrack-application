package dev.sqayner.instrack.activities;

import static dev.sqayner.instrack.activities.MainActivity.followers;
import static dev.sqayner.instrack.activities.MainActivity.followings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.instagram4j.instagram4j.models.user.Profile;
import com.github.instagram4j.instagram4j.requests.friendships.FriendshipsActionRequest;
import com.github.instagram4j.instagram4j.responses.IGResponse;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import dev.sqayner.instrack.App;
import dev.sqayner.instrack.R;
import dev.sqayner.instrack.adapters.UserRecyclerViewAdapter;
import dev.sqayner.instrack.dialogs.LoadingDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfileListActivity extends AppCompatActivity {

    public static TYPES TYPE = null;
    private RecyclerView rvNotFollowingMeUsers;
    private ImageButton IbBack;
    private TextView TvTitle;
    private LoadingDialog loadingDialog;
    private UserRecyclerViewAdapter userRecyclerViewAdapter;
    private List<Profile> profiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        loadingDialog = new LoadingDialog(this);

        IbBack = findViewById(R.id.profile_list_ib_back);
        IbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TvTitle = findViewById(R.id.profile_list_tv_title);

        rvNotFollowingMeUsers = findViewById(R.id.main_rv_notfollowingmeusers);

        if (TYPE == null)
            finish();

        profiles = new ArrayList<>();

        userRecyclerViewAdapter = new UserRecyclerViewAdapter(profiles, ProfileListActivity.this);

        switch (TYPE) {
            case THOSE_WHO_DO_NOT_FOLLOW_ME:
                userRecyclerViewAdapter.setActionButtonType(UserRecyclerViewAdapter.ActionButtonTypes.UNFOLLOW);
                for (Profile user : followings) {
                    Profile usr = findInList(followers, user);
                    if (usr == null) {
                        profiles.add(user);
                    }
                }

                TvTitle.setText(MessageFormat.format("Beni Takip Etmeyeneler ({0})", profiles.size()));
                break;
            case THE_ONES_I_DONT_FOLLOW:
                userRecyclerViewAdapter.setActionButtonType(UserRecyclerViewAdapter.ActionButtonTypes.FOLLOW);
                for (Profile user : followers) {
                    Profile usr = findInList(followings, user);
                    if (usr == null) {
                        profiles.add(user);
                    }
                }

                TvTitle.setText(MessageFormat.format("Geri Takip Etmediklerim ({0})", profiles.size()));
                break;
            case MUTUAL_FOLLOWING:
                userRecyclerViewAdapter.setActionButtonType(UserRecyclerViewAdapter.ActionButtonTypes.UNFOLLOW);
                for (Profile user : followings) {
                    Profile usr = findInList(followers, user);
                    if (usr != null) {
                        profiles.add(user);
                    }
                }

                TvTitle.setText(MessageFormat.format("Karşılıklı Takipleştiklerim ({0})", profiles.size()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + TYPE);
        }

        rvNotFollowingMeUsers.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ProfileListActivity.this);
        rvNotFollowingMeUsers.setLayoutManager(mLayoutManager);
        rvNotFollowingMeUsers.setAdapter(userRecyclerViewAdapter);

        userRecyclerViewAdapter.notifyDataSetChanged();

        userRecyclerViewAdapter.setOnItemClickListener(new UserRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onClick(Profile user, int position) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.instagram.com/" + user.getUsername()));
                startActivity(intent);
            }
        });

        userRecyclerViewAdapter.setOnFollowButtonClickListener(new UserRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onClick(Profile user, int position) {
                igAction(user.getPk(), FriendshipsActionRequest.FriendshipsAction.CREATE).subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        loadingDialog.show();
                    }

                    @Override
                    public void onNext(@NonNull String s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        loadingDialog.dismiss();

                        profiles.remove(position);
                        userRecyclerViewAdapter.notifyItemRemoved(position);
                        userRecyclerViewAdapter.notifyItemRangeChanged(position - 1, profiles.size());
                        updateTitle();
                    }
                });
            }
        });

        userRecyclerViewAdapter.setOnUnfollowButtonClickListener(new UserRecyclerViewAdapter.OnClickListener() {
            @Override
            public void onClick(Profile user, int position) {
                igAction(user.getPk(), FriendshipsActionRequest.FriendshipsAction.DESTROY).subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        loadingDialog.show();
                    }

                    @Override
                    public void onNext(@NonNull String s) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        loadingDialog.dismiss();

                        profiles.remove(position);
                        userRecyclerViewAdapter.notifyItemRemoved(position);
                        userRecyclerViewAdapter.notifyItemRangeChanged(position - 1, profiles.size());
                        removeFromFollowings(user);
                        updateTitle();
                    }
                });
            }
        });
    }

    private void updateTitle() {


        switch (TYPE) {
            case THOSE_WHO_DO_NOT_FOLLOW_ME:
                TvTitle.setText(MessageFormat.format("Beni Takip Etmeyeneler ({0})", profiles.size()));
                break;
            case THE_ONES_I_DONT_FOLLOW:
                TvTitle.setText(MessageFormat.format("Geri Takip Etmediklerim ({0})", profiles.size()));
                break;
            case MUTUAL_FOLLOWING:
                TvTitle.setText(MessageFormat.format("Karşılıklı Takipleştiklerim ({0})", profiles.size()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + TYPE);
        }
    }

    private void addToFollowings(Profile user) {
        followings.add(user);
    }

    private void removeFromFollowings(Profile user) {
        for (int i = 0; i < followings.size(); i++) {
            Profile profile = followings.get(i);
            if (profile.getUsername().equals(user.getUsername())) {
                followings.remove(i);
                break;
            }
        }
    }

    private Profile findInList(List<Profile> followers, Profile user) {
        for (Profile profile : followers) {
            if (profile.getUsername().equals(user.getUsername()))
                return profile;
        }
        return null;
    }

    private Observable<String> igAction(Long pk, FriendshipsActionRequest.FriendshipsAction action) {
        return Observable
                .fromCallable(new Callable<String>() {
                    @Override
                    public String call() {
                        IGResponse response = new FriendshipsActionRequest(pk, action)
                                .execute(App.client).join();
                        return response.getStatus();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public enum TYPES {
        THOSE_WHO_DO_NOT_FOLLOW_ME,
        THE_ONES_I_DONT_FOLLOW,
        MUTUAL_FOLLOWING
    }
}