package com.xiao.biometricmanagerlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import static android.text.TextUtils.isEmpty;

public class SharePreferenceUtil {
    private static final String DEFAULT_NAME = "finger";

    public static String KEY_IS_FINGER_CHANGE = "is_finger_change";//指纹是否变化了

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(DEFAULT_NAME, Context.MODE_PRIVATE);
    }

    public static void saveFingerDataChange(Context context, Boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_IS_FINGER_CHANGE, value);
        editor.apply();
    }

    public static boolean isFingerDataChange(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_IS_FINGER_CHANGE,false);
    }


}
