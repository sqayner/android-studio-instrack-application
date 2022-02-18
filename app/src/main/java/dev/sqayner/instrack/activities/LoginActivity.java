package dev.sqayner.instrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;

import dev.sqayner.instrack.App;
import dev.sqayner.instrack.R;
import dev.sqayner.instrack.dialogs.LoadingDialog;
import dev.sqayner.instrack.dialogs.TwoFADialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEt, passwordEt;
    private Button loginBtn;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Locale.setDefault(Locale.US);

        usernameEt = findViewById(R.id.login_username_et);
        passwordEt = findViewById(R.id.login_password_et);
        loginBtn = findViewById(R.id.login_btn);

        loadingDialog = new LoadingDialog(this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();

                App.username = usernameEt.getText().toString();
                App.password = passwordEt.getText().toString();

                login(App.username, App.password).subscribe(new Observer<IGClient>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull IGClient igClient) {
                        App.client = igClient;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (Objects.requireNonNull(e.getMessage()).trim().equals(""))
                            displayTwoFactorAuthCodeInputDialog();
                        else
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        loadingDialog.dismiss();
                        goMainActivity();
                    }
                });
            }
        });
    }

    private void displayTwoFactorAuthCodeInputDialog() {
        loadingDialog.dismiss();
        TwoFADialog twoFADialog = new TwoFADialog(LoginActivity.this);
        twoFADialog.show();

        twoFADialog.setTwoFactoryAuthenticationListener(new TwoFADialog.TwoFactoryAuthenticationListener() {
            @Override
            public void onAuth(String code) {
                loadingDialog.show();
                loginWithTwoFactorAuth(App.username, App.password, code).subscribe(new Observer<IGClient>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull IGClient igClient) {
                        App.client = igClient;
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (Objects.requireNonNull(e.getMessage()).trim().equals(""))
                            Toast.makeText(LoginActivity.this, "Bilinmeyen bir hata oluştu.", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        loadingDialog.dismiss();
                        goMainActivity();
                    }
                });
            }
        });
    }

    private void goMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        LoginActivity.this.finish();
    }

    private Observable<IGClient> login(@NonNull String username, @NonNull String password) {
        return Observable
                .fromCallable(new Callable<IGClient>() {
                    @Override
                    public IGClient call() throws Exception {
                        return IGClient.builder()
                                .username(username)
                                .password(password)
                                .login();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<IGClient> loginWithTwoFactorAuth(@NonNull String username, @NonNull String password, @NonNull String twoFA) {
        return Observable
                .fromCallable(new Callable<IGClient>() {
                    @Override
                    public IGClient call() throws Exception {
                        return IGClient.builder()
                                .username(username)
                                .password(password)
                                .onTwoFactor((client, response) -> {
                                    return IGChallengeUtils.resolveTwoFactor(client, response, new Callable<String>() {
                                        @Override
                                        public String call() throws Exception {
                                            return twoFA;
                                        }
                                    });
                                })
                                .login();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}