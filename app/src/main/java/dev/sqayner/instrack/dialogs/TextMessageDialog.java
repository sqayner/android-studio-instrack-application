package dev.sqayner.instrack.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import dev.sqayner.instrack.R;
import dev.sqayner.instrack.databinding.DialogTextMessageBinding;

public class TextMessageDialog extends Dialog {

    private CodeEntryDialog.TwoFactoryAuthenticationListener twoFactoryAuthenticationListener;
    private DialogTextMessageBinding binding;
    private String message, title;

    public TextMessageDialog(@NonNull Context context) {
        super(context);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogTextMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setBackgroundDrawableResource(R.color.transparent);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = (displayMetrics.widthPixels / 100) * 90;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(layoutParams);

        setCancelable(false);

        binding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();
        binding.messageTv.setText(getMessage());
        binding.messageTitleTv.setText(getTitle());
    }

    public interface TwoFactoryAuthenticationListener {
        void onAuth(String code);
    }
}
