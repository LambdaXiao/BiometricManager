package com.xiao.biometricmanagerlib.impl;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.xiao.biometricmanagerlib.CipherHelper;
import com.xiao.biometricmanagerlib.FingerChangeCallback;
import com.xiao.biometricmanagerlib.FingerManagerBuilder;
import com.xiao.biometricmanagerlib.IBiometricPrompt;
import com.xiao.biometricmanagerlib.IFingerCallback;
import com.xiao.biometricmanagerlib.SharePreferenceUtil;
import com.xiao.biometricmanagerlib.dialog.BaseFingerDialog;
import com.xiao.biometricmanagerlib.dialog.DefaultFingerDialog;

import javax.crypto.Cipher;

/**
 * Android 6.0及以上指纹认证实现
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class BiometricPromptImpl23 implements IBiometricPrompt {

    private AppCompatActivity mActivity;

    private Cipher mCipher;

    private boolean mSelfCanceled;//用户主动取消指纹识别

    private BaseFingerDialog mFingerDialog;

    private IFingerCallback mFingerCallback;

    private FingerChangeCallback mFingerChangeCallback;

    private static final String SECRET_MESSAGE = "Very secret message";

    public BiometricPromptImpl23(AppCompatActivity activity, BaseFingerDialog fingerDialog,
                                 FingerManagerBuilder fingerManagerController) {
        this.mActivity = activity;
        this.mCipher = CipherHelper.getInstance().createCipher();
        this.mFingerCallback = fingerManagerController.getFingerCallback();
        this.mFingerChangeCallback = fingerManagerController.getFingerChangeCallback();
        this.mFingerDialog = fingerDialog == null ? DefaultFingerDialog.newInstance(fingerManagerController) : fingerDialog;
    }

    /**
     * 开始指纹认证
     * @param cancel
     */
    @Override
    public void authenticate(@NonNull final CancellationSignal cancel) {
        mSelfCanceled = false;
        //检测指纹库是否发生变化
        if (CipherHelper.getInstance().initCipher(mCipher) || SharePreferenceUtil.isFingerDataChange(mActivity)) {
            mFingerChangeCallback.onChange(mActivity);
            return;
        }

        mFingerDialog.setOnDismissListener(new BaseFingerDialog.IDismissListener() {

            @Override
            public void onDismiss() {
                mSelfCanceled = !cancel.isCanceled();
                if (mSelfCanceled) {
                    cancel.cancel();
                    //如果使用的是默认弹窗,就使用cancel回调,否则交给开发者自行处理
                    if (mFingerDialog.getClass() == DefaultFingerDialog.class) {
                        mFingerCallback.onCancel();
                    }
                }
            }
        });
        //Android 9.0以下显示自定义的指纹认证对话框
        if (!mFingerDialog.isAdded()) {
            mFingerDialog.show(mActivity.getSupportFragmentManager(), mFingerDialog.getClass().getSimpleName());
        }
        //开始指纹认证
        FingerprintManager fingerprintManager = (FingerprintManager) mActivity.getSystemService(FingerprintManager.class);
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(mCipher), cancel, 0,
                new FingerprintManager.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errMsgId, CharSequence errString) {
                        super.onAuthenticationError(errMsgId, errString);
                        Toast.makeText(mActivity, errString, Toast.LENGTH_SHORT).show();
                        //指纹认证失败五次会报错，会停留几秒钟后才可以重试
                        cancel.cancel();
                        if (!mSelfCanceled) {
                            mFingerDialog.onError(errString.toString());
                            mFingerCallback.onError(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                        super.onAuthenticationHelp(helpMsgId, helpString);
                        mFingerDialog.onHelp(helpString.toString());
                        mFingerCallback.onHelp(helpString.toString());
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Cipher cipher = result.getCryptoObject().getCipher();
                        if (cipher != null) {
                            try {
                                /*
                                * 用于检测三星手机指纹库变化，
                                * 三星手机指纹库发生变化后前面的initCipher检测不到KeyPermanentlyInvalidatedException
                                * 但是cipher.doFinal(SECRET_MESSAGE.getBytes())会抛出异常
                                * 因此以此监听三星手机的指纹库变化
                                 */
                                cipher.doFinal(SECRET_MESSAGE.getBytes());

                                cancel.cancel();
                                mFingerDialog.onSucceed();
                                mFingerCallback.onSucceed();
                            } catch (Exception e) {
                                e.printStackTrace();
                                mFingerChangeCallback.onChange(mActivity);
                            }
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        //指纹不匹配
                        mFingerDialog.onFailed();
                        mFingerCallback.onFailed();
                    }
                }, null);

    }
}
