package com.xiao.biometricmanagerlib.interfaces;

public interface IFingerCallback {

    void onError(String error);

    void onHelp(String help);

    void onSucceed();

    void onFailed();

    void onCancel();
    //监听指纹变化
    void onChange();
}
