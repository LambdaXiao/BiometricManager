package com.xiao.biometricmanager;

import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xiao.biometricmanagerlib.FingerManager;
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
                    switch (FingerManager.checkSupport(MainActivity.this)) {
                        case DEVICE_UNSUPPORTED:
                            showToast("您的设备不支持指纹");
                            break;
                        case SUPPORT_WITHOUT_DATA:
                            showToast("请在系统录入指纹后再验证");
                            break;
                        case SUPPORT:
                            FingerManager.build().setApplication(getApplication())
                                    .setTitle("指纹验证")
                                    .setDes("请按下指纹")
                                    .setNegativeText("取消")
//                                    .setFingerDialogApi23(new MyFingerDialog())//如果你需要自定义android P 以下系统弹窗就设置,注意需要继承BaseFingerDialog，不设置会使用默认弹窗
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
                                    .create()
                                    .startListener(MainActivity.this);
                            break;
                        default:
                    }
                }
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    FingerManager.updateFingerData(MainActivity.this);
                    showToast("已同步指纹数据,解除指纹数据变化");
                }
            }
        });

        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (FingerManager.hasFingerprintChang(MainActivity.this)) {
                        showToast("指纹数据已经发生变化");
                    }else {
                        showToast("指纹数据没有发生变化");
                    }
                }
            }
        });
    }

    private void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }
}
