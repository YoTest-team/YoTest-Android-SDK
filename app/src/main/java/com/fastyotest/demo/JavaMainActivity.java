package com.fastyotest.demo;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.fastyotest.demo.databinding.ActivityMainBinding;
import com.fastyotest.library.YoTestCaptchaVerify;
import com.fastyotest.library.YoTestListener;

public class JavaMainActivity extends AppCompatActivity {
    private final static String TAG = "Demo";

    private ActivityMainBinding viewBinding;
    private YoTestCaptchaVerify yoTestCaptchaVerify;

    @ColorInt
    private int fontColor = 0;
    private AlertDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fontColor = ContextCompat.getColor(this, R.color.purple_500);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        yoTestCaptchaVerify = new YoTestCaptchaVerify(this, yoTestListener);
        viewBinding.btnLogin.setOnClickListener(view -> login());
        viewBinding.showLoading.setOnCheckedChangeListener(
                (compoundButton, b) -> yoTestCaptchaVerify.setShowLoading(b)
        );
        viewBinding.showToast.setOnCheckedChangeListener(
                (compoundButton, b) -> yoTestCaptchaVerify.setShowToast(b)
        );
    }

    private void login() {
        if (!yoTestCaptchaVerify.getShowLoading()) {
            dialog = new AlertDialog.Builder(this).setMessage("加载中...").show();
        }
        yoTestCaptchaVerify.verify();
    }

    private final YoTestListener yoTestListener = new YoTestListener() {
        @Override
        public void onReady(@Nullable String data) {
            Log.d(TAG, "onReady: " + data);
            viewBinding.txtCallback.append("====================\n");
            viewBinding.txtCallback.append(getSpannableString("onReady"));
            viewBinding.txtCallback.append(": " + data + "\n");
        }

        @Override
        public void onShow(@Nullable String data) {
            Log.d(TAG, "onShow: " + data);
            viewBinding.txtCallback.append(getSpannableString("onShow"));
            viewBinding.txtCallback.append(": " + data + "\n");
            if (dialog != null) {
                dialog.dismiss();
            }
        }

        @Override
        public void onSuccess(@NonNull String token, boolean verified) {
            Log.d(TAG, "onSuccess: token=" + token + "; verified=" + verified);
            viewBinding.txtCallback.append(getSpannableString("onSuccess"));
            viewBinding.txtCallback.append(": token=" + token + "; verified=" + verified + "\n");
        }

        @Override
        public void onError(int code, @NonNull String message) {
            Log.d(TAG, "onError: code=" + code + "; message=" + message);
            viewBinding.txtCallback.append(getSpannableString("onError"));
            viewBinding.txtCallback.append(": code=" + code + "; message=" + message + "\n");
        }

        @Override
        public void onClose(@Nullable String data) {
            Log.d(TAG, "onClose: " + data);
            viewBinding.txtCallback.append(getSpannableString("onClose"));
            viewBinding.txtCallback.append(": " + data + "\n");
        }
    };

    private SpannableString getSpannableString(String target) {
        SpannableString spanString = new SpannableString(target);
        ForegroundColorSpan span = new ForegroundColorSpan(fontColor);
        spanString.setSpan(span, 0, target.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanString;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        yoTestCaptchaVerify.destroy();
    }
}
