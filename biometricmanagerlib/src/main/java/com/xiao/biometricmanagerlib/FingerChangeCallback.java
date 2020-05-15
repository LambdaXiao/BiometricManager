package com.xiao.biometricmanagerlib;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * 指纹变化监听
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public abstract class FingerChangeCallback {

    public void onChange(Context context) {
        SharePreferenceUtil.saveFingerDataChange(context, true);
        onFingerDataChange();
    }

    protected abstract void onFingerDataChange();
}
