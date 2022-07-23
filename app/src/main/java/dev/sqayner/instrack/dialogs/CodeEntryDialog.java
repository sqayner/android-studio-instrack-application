package dev.sqayner.instrack.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mukesh.OtpView;

import dev.sqayner.instrack.R;

public class CodeEntryDialog extends Dialog {

    private TwoFactoryAuthenticationListener twoFactoryAuthenticationListener;
    private Button BtnVerify;
    private OtpView OTP2FaCode;
    private String code;

    public CodeEntryDialog(@NonNull Context context) {
        super(context);
    }

    public void setTwoFactoryAuthenticationListener(TwoFactoryAuthenticationListener twoFactoryAuthenticationListener) {
        this.twoFactoryAuthenticationListener = twoFactoryAuthenticationListener;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_code_entry);
        getWindow().setBackgroundDrawableResource(R.color.transparent);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = (displayMetrics.widthPixels / 100) * 90;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(layoutParams);

        setCancelable(false);

        BtnVerify = findViewById(R.id.two_fa_btn_verify);
        OTP2FaCode = findViewById(R.id.two_fa_otp_code);

        OTP2FaCode.requestFocus();

        BtnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OTP2FaCode.getText() != null) {
                    setCode(OTP2FaCode.getText().toString());
                    if (twoFactoryAuthenticationListener != null)
                        twoFactoryAuthenticationListener.onAuth(OTP2FaCode.getText().toString());
                } else
                    Toast.makeText(getContext(), "Lütfen kodunuzu giriniz.", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        OTP2FaCode.setText("");
    }

    public interface TwoFactoryAuthenticationListener {
        void onAuth(String code);
    }
}
