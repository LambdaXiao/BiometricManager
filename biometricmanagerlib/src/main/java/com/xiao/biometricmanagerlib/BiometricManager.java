package com.xiao.biometricmanagerlib;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import com.xiao.biometricmanagerlib.interfaces.IFingerCallback;

import java.util.concurrent.Executor;

import javax.crypto.Cipher;

/**
 * 生物识别管理类，兼容所有Android版本
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class BiometricManager {

    private static BiometricManager fingerManager;

    private static BiometricManagerBuilder mBiometricManagerBuilder;
    private static AppCompatActivity mActivity;
    private static Cipher cipher;
    private static IFingerCallback mFingerCallback;
    private static BiometricPrompt mBiometricPrompt;
    private static BiometricPrompt.PromptInfo promptInfo;
    private static Handler handler;
    private static Executor executor;
    private static final String SECRET_MESSAGE = "Very secret message";

    private static BiometricManager getInstance() {
        if (fingerManager == null) {
            synchronized (BiometricManager.class) {
                if (fingerManager == null) {
                    fingerManager = new BiometricManager();
                }
            }
        }
        return fingerManager;
    }

    public static BiometricManager getInstance(AppCompatActivity activity, BiometricManagerBuilder biometricManagerBuilder) {
        mActivity = activity;
        mBiometricManagerBuilder = biometricManagerBuilder;
        mFingerCallback = mBiometricManagerBuilder.getFingerCallback();

        /*=================指纹加密相关====================*/
        //如果还没有开启指纹数据变化监听，但是指纹数据已经发生了改变，就清除指纹数据变化，重新生成指纹加密库key
        if (!SharePreferenceUtil.isEnableFingerDataChange(mActivity) && hasFingerprintChang(mActivity)) {
            updateFingerData(mActivity);
        } else {
            CipherHelper.getInstance().createKey(mActivity, false);
        }
        cipher = CipherHelper.getInstance().createCipher();
        /*=================指纹加密相关====================*/

        handler = new Handler();
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(mBiometricManagerBuilder.getTitle())
                .setSubtitle(mBiometricManagerBuilder.getDes())
                .setNegativeButtonText(mBiometricManagerBuilder.getNegativeText())
                .build();

        mBiometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(mActivity, errString, Toast.LENGTH_SHORT).show();
                //指纹认证失败五次会报错，会停留几秒钟后才可以重试
                //没有按取消按钮
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    mFingerCallback.onCancel();
                } else {
                    mFingerCallback.onError(errString.toString());
                }

            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
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
                        //针对三星手机，开启了监听才去检测设备指纹库变化
                        if (SharePreferenceUtil.isEnableFingerDataChange(mActivity)) {
                            cipher.doFinal(SECRET_MESSAGE.getBytes());
                        }
                        mFingerCallback.onSucceed();
                        //开启监听设备指纹数据变化
                        SharePreferenceUtil.saveEnableFingerDataChange(mActivity, true);
                    } catch (Exception e) {
                        e.printStackTrace();

                        SharePreferenceUtil.saveFingerDataChange(mActivity, true);
                        mFingerCallback.onChange();

                    }
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //指纹不匹配
                mFingerCallback.onFailed();
            }
        });

        return getInstance();
    }

    public static BiometricManagerBuilder build() {
        return new BiometricManagerBuilder();
    }

    /**
     * 开始监听生物识别
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void authenticate() {

        //检测指纹库是否发生变化
        boolean exceptionState = CipherHelper.getInstance().initCipher(cipher);
        boolean flag = SharePreferenceUtil.isEnableFingerDataChange(mActivity) && (exceptionState || SharePreferenceUtil.isFingerDataChange(mActivity));
        if (flag) {
            SharePreferenceUtil.saveFingerDataChange(mActivity, true);
            mFingerCallback.onChange();
            return;
        }

        //开始指纹认证
        mBiometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
    }

    /**
     * 检查设别是否支持指纹识别
     * BiometricManager.BIOMETRIC_SUCCESS 指纹硬件可用并且已录入指纹数据
     * BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE 没有指纹硬件
     * BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE 指纹硬件不可用
     * BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED 指纹硬件可用但没有录入指纹数据
     *
     * @return
     */
    public static int checkSupport(Context context) {
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(context);
        return biometricManager.canAuthenticate();
    }

    /**
     * 同步指纹数据,解除指纹数据变化问题
     *
     * @param context
     */
    public static void updateFingerData(Context context) {
        CipherHelper.getInstance().createKey(context, true);
        SharePreferenceUtil.saveEnableFingerDataChange(context, false);
        SharePreferenceUtil.saveFingerDataChange(context, false);
    }

    /**
     * 检查设备是否有指纹变化(例外：三星手机必须识别成功回调是才能检测到指纹库是否发生变化)
     *
     * @return
     */
    public static boolean hasFingerprintChang(Context context) {
        if (SharePreferenceUtil.isFingerDataChange(context)) {
            return true;
        }
        CipherHelper.getInstance().createKey(context, false);
        Cipher cipher = CipherHelper.getInstance().createCipher();
        return CipherHelper.getInstance().initCipher(cipher);
    }

}