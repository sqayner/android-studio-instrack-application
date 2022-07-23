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
import com.github.instagram4j.instagram4j.exceptions.ExceptionallyHandler;
import com.github.instagram4j.instagram4j.responses.accounts.LoginResponse;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.Callable;

import dev.sqayner.instrack.App;
import dev.sqayner.instrack.R;
import dev.sqayner.instrack.dialogs.LoadingDialog;
import dev.sqayner.instrack.dialogs.CodeEntryDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements Callable<String> {

    private EditText usernameEt, passwordEt;
    private Button loginBtn;
    private LoadingDialog loadingDialog;
    private CodeEntryDialog twoFADialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Locale.setDefault(Locale.US);

        usernameEt = findViewById(R.id.login_username_et);
        passwordEt = findViewById(R.id.login_password_et);
        loginBtn = findViewById(R.id.login_btn);

        loadingDialog = new LoadingDialog(this);
        twoFADialog = new CodeEntryDialog(LoginActivity.this);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();

                login(usernameEt.getText().toString(), passwordEt.getText().toString()).subscribe(new Observer<IGClient>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull IGClient igClient) {
                        if (igClient.isLoggedIn()) {
                            try {
                                File clientFile = new File(getFilesDir(), "client");
                                File cookieFile = new File(getFilesDir(), "cookie");
                                igClient.serialize(clientFile, cookieFile);
                                igClient.setExceptionallyHandler(new ExceptionallyHandler() {
                                    @Override
                                    public <T> T handle(Throwable throwable, Class<T> type) {
                                        Toast.makeText(LoginActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        return null;
                                    }
                                });
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }

                            App.client = igClient;
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Giriş Başarısız", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
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
                                .onTwoFactor(new IGClient.Builder.LoginHandler() {
                                    @Override
                                    public LoginResponse accept(IGClient client, LoginResponse t) {
                                        return IGChallengeUtils.resolveTwoFactor(client, t, LoginActivity.this);
                                    }
                                })
                                .onChallenge(new IGClient.Builder.LoginHandler() {
                                    @Override
                                    public LoginResponse accept(IGClient client, LoginResponse t) {
                                        return IGChallengeUtils.resolveChallenge(client, t, LoginActivity.this);
                                    }
                                })
                                .login();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public String call() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!twoFADialog.isShowing())
                    twoFADialog.show();
            }
        });

        String code = "";

        do
            if (twoFADialog.getCode() != null && !twoFADialog.getCode().equals("") && twoFADialog.getCode().length() == 6) {
                code = twoFADialog.getCode();
                twoFADialog.setCode("");
            }
        while (code.equals(""));
        return code;
    }
}