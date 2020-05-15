package com.xiao.biometricmanagerlib;

import android.os.CancellationSignal;
import android.support.annotation.NonNull;

/**
 * 指纹认证接口
 */
public interface IBiometricPrompt {
    void authenticate(@NonNull CancellationSignal cancel);
}
