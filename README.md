#### 说明
指纹认证一般我们期望是这样的:

1. 指纹支付可以和手指绑定，比和微信支付一样：开启时输入一个指纹，每次支付的时候只能用当时绑定的指纹支付，这样可以保证指纹安全。
1. 如果上面的走不通的话，那就只能和招商银行指纹登录一样：开启指纹登录时验证指纹，验证通过之后，以后每次登录都可以通过验证输入的指纹是否是录入系统中的任何一个指纹。 如果你开通指纹后，又在系统中录入了新的指纹，下次用指纹登录招商银行的时候就会被提示指纹发生了变化。


我个人调研指纹认证方案主要是下面两种：

  1. 使用微信开源Soter库

  这个方案的优点就是稳,据说微信的指纹支付就是使用的这个方案,和国产设备厂商合作的.可以定位的具体的哪个手指,可以获取到指纹Id. 不足之处就是不支持华为手机和国外部分厂商(没有和Soter合作的). 虽然微信客户端是支持华为指纹的.但是这个框架是暂时不支持的.(很久之前就说要支持,截至目前仍未支持)

  2. 使用系统官方Api

- 优点：支持所有android 6.0以上的指纹设备(招商银行80%可能性使用的就是这个方案)

- 缺点：
    1. 不能获取指纹Id,不能和手指绑定,同能通过判断指纹库是否变化保证安全
    2. 需要针对android 6.0 和android 9.0 适配 : android 9.0 以下需要自己实现指纹识别弹窗样式 ,但是android 9.0 开始统一由系统弹窗实现(不同厂商可能还不一样)

  **综合考虑我们选择使用系统官方Api实现第二种方案，只支持所有android 6.0 以上的指纹设备**

#### 效果演示

1. android M

- 指纹识别成功

![](https://github.com/LambdaXiao/screenshot/raw/master/screenshots/m-success.gif)

- 指纹验证失败

![](https://github.com/LambdaXiao/screenshot/raw/master/screenshots/m-fail.gif)

- 指纹数据发生了改变

![](https://github.com/LambdaXiao/screenshot/raw/master/screenshots/m-change.gif)

2. android P

- 指纹识别成功

![](https://github.com/LambdaXiao/screenshot/raw/master/screenshots/p-success.gif)
- 指纹验证失败

![](https://github.com/LambdaXiao/screenshot/raw/master/screenshots/p-fail.gif)
- 指纹数据发生了改变

![](https://github.com/LambdaXiao/screenshot/raw/master/screenshots/p-change.gif)

#### 快速集成

在项目下的build.gradle文件中
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

在app下的build.gradle文件中

```
implementation 'com.github.LambdaXiao:StockChart-MPAndroidChart:1.1'
```

具体使用如下：
```
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
```

#### 支持功能

1.   检查设备是否支持指纹：分为三种支持，分别是（1）设备没有指纹识别器 （2）设备有指纹识别器但是没有指纹数据（3）设备有识别器并且有指纹数据，可以进行指纹验证
1. 　监听相应手机指纹库数据发生变化的情况
2. 　指纹数据发生变化后可以调用updateFingerData()方法更新同步变化
1. 　能够对取消指纹识别和指纹识别失败分别进行处理
1. 　需要适配android版本，在android版本大于6.0 小于9.0 的情况下要自己实现指纹识别弹窗。在android P上要使用最新Api调用指纹识别统一弹窗

#### 如何监听指纹数据变化

1. 创建SecretKey对当前指纹数据加密，如果在创建SecretKey后添加新指纹,则会在Cipher初始化时引发KeyPermanentlyInvalidatedException.通过这个异常我们可以知道指纹数据是否发生变化

```
/**
     * @des 初始化Cipher ,根据KeyPermanentlyInvalidatedExceptiony异常判断指纹库是否发生了变化
     *
     */
    public boolean initCipher(Cipher cipher) {
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
            if (cipher == null) {
                cipher = createCipher();
            }
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return false;
        } catch (KeyPermanentlyInvalidatedException e) {
            //指纹库是否发生了变化,这里会抛KeyPermanentlyInvalidatedException
            return true;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);

        }
    }
```

2. 唯一例外的是三星手机不会抛出KeyPermanentlyInvalidatedException这个异常，经过反复测试，发现三星手机可以根据识别成功后的回调方法中检测cipher.doFinal(SECRET_MESSAGE.getBytes())这里的异常判断指纹数据是否发生变化

```
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
                                mFingerCallback.onChange();
                            }
                        }
```


#### 自定义android 9.0以下弹窗

如果你想自定义android M 的指纹识别弹窗,很简单,你只需要:

1. 继承BaseFingerDialog类
1. 在onCreateView中初始化你自己的布局
1. 实现onSucceed()、onFailed（）、onHelp（）、onError（）四个回调就好了，这四个回调建议只做UI相关操作，逻辑操作已经在外部提供了回调接口。
    1. onSucceed ：指纹识别成功，可以直接关闭弹窗
    1. onFailed ： 当识别的手指没有注册时回调,但是可以继续验证
    1. onHelp ： 指纹识别不对,会提示,手指不要大范围移动等信息,可以继续验证
    1. onError ：指纹识别彻底失败,不能继续验证
    1. 一个指纹识别事件序列是这样的： 开始识别 ---> (onHelp / onFaild) (0个或多个) ---> onSucceed / onError

调起指纹识别得时候,将自定义的弹窗设置进去,代码如下,如果你不设置自定义弹窗会使用默认的android M 弹窗
```
FingerManager.build().setApplication(getApplication())
				.setTitle("指纹验证")
				.setDes("请按下指纹")
				.setNegativeText("取消")
				.setFingerDialogApi23(new MyFingerDialog())
				.setFingerCheckCallback()
```

#### 参考链接

https://github.com/googlearchive/android-FingerprintDialog
https://android.ctolib.com/mengcuiguang-FingerDemo.html