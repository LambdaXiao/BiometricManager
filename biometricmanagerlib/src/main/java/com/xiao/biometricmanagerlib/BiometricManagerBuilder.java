package com.xiao.biometricmanagerlib;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.xiao.biometricmanagerlib.interfaces.IFingerCallback;

/**
 * FingerManager的建造者类
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class BiometricManagerBuilder {

    //弹窗标题
    private String mTitle;

    //弹窗描述
    private String mDes;

    //取消按钮话术
    private String mNegativeText;

    //指纹识别回调
    private IFingerCallback mFingerCallback;

    public BiometricManagerBuilder setTitle(String title) {
        mTitle = title;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public BiometricManagerBuilder setDes(String des) {
        this.mDes = des;
        return this;
    }

    public String getDes() {
        return mDes;
    }

    public BiometricManagerBuilder setNegativeText(String negativeText) {
        this.mNegativeText = negativeText;
        return this;
    }

    public String getNegativeText() {
        return mNegativeText;
    }

    public BiometricManagerBuilder setFingerCallback(IFingerCallback fingerCallback) {
        this.mFingerCallback = fingerCallback;
        return this;
    }

    public IFingerCallback getFingerCallback() {
        return mFingerCallback;
    }


    public BiometricManager create(AppCompatActivity activity) {
        if (mFingerCallback == null) {
            throw new RuntimeException("CompatFingerManager : FingerCheckCallback can not be null");
        }

        return BiometricManager.getInstance(activity,this);
    }

}
