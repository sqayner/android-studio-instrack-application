package dev.sqayner.instrack.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;

import dev.sqayner.instrack.R;

public class LoadingDialog extends Dialog {

    public Context context;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        setCancelable(false);
    }
}