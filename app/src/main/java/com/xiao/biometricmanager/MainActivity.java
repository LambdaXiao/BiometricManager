package com.xiao.biometricmanager;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xiao.biometricmanagerlib.BiometricManager;
import com.xiao.biometricmanagerlib.callback.SimpleFingerCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    switch (BiometricManager.checkSupport(MainActivity.this)) {
                        case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            showToast("您的设备不支持指纹");
                            break;
                        case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            showToast("请在系统录入指纹后再验证");
                            break;
                        case androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS:
                            BiometricManager.build()
                                    .setTitle("指纹验证")
                                    .setDes("请按下指纹")
                                    .setNegativeText("取消")
                                    .setFingerCallback(new SimpleFingerCallback() {
                                        @Override
                                        public void onSucceed() {
                                            showToast("验证成功");
                                        }

                                        @Override
                                        public void onFailed() {
                                            showToast("指纹无法识别");
                                        }

                                        @Override
                                        public void onChange() {
                                            showToast("指纹数据发生了变化");
                                        }


                                    })
                                    .create(MainActivity.this)
                                    .authenticate();
                            break;
                        default:
                    }
                }else {
                    showToast("您的设备不支持指纹");
                }
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    BiometricManager.updateFingerData(MainActivity.this);
                    showToast("已同步指纹数据,解除指纹数据变化");
                }
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (FingerManager.hasFingerprintChang(MainActivity.this)) {
//                        showToast("指纹数据已经发生变化");
//                    }else {
//                        showToast("指纹数据没有发生变化");
//                    }
//                }

                test();
            }
        });
    }

    private void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void test (){
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS:
                showToast("App can authenticate using biometrics.");
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                showToast("No biometric features available on this device.");
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                showToast("Biometric features are currently unavailable.");
                break;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                showToast("The user hasn't associated any biometric credentials " +
                        "with their account.");
                break;
            default:
        }
    }
}
