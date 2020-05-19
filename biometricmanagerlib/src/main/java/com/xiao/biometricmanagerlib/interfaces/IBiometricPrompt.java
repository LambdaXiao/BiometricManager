package com.xiao.biometricmanagerlib.interfaces;

import android.os.CancellationSignal;
import androidx.annotation.NonNull;

/**
 * 指纹认证接口
 */
public interface IBiometricPrompt {
    void authenticate(@NonNull CancellationSignal cancel);
}
