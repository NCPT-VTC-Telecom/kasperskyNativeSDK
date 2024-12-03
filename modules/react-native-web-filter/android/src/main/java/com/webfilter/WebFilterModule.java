package com.webfilter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.kaspersky.components.urlchecker.UrlCategory;
import com.kavsdk.KavSdk;
import com.kavsdk.antivirus.Antivirus;
import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseDateTimeException;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdk.license.SdkLicenseNetworkException;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.shared.iface.ServiceStateStorage;
import com.kavsdk.updater.Updater;
import com.kavsdk.webfilter.WebAccessHandler;
import com.kavsdk.webfilter.WebFilterControl;
import com.kavsdk.webfilter.WebFilterControlFactory;
import com.kavsdk.webfilter.WebFilterControlFactoryImpl;
import com.webmodules.AvCompletedListener;
import com.webmodules.DataStorage;
import com.webmodules.SdkInitListener;
import com.webmodules.MyUrlFilterHandler;
import com.webmodules.UrlFilterImpl;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


class WebFilterModule extends ReactContextBaseJavaModule implements SdkInitListener {

    private static final String TAG = "WEB_FILTER_EXAMPLE";
    private volatile WebFilterModule.InitStatus mSdkInitStatus = WebFilterModule.InitStatus.NotInited;
    private Thread mThread;
    private Context mContext;
    private static final int WEB_FILTER_FLAGS = 0x0;
    private static final String LOCAL_HOST    = "127.0.0.1"; //NOPMD
    WebFilterControl mWebFilter;
    private AvCompletedListener mOpCompletedListener;

    public WebFilterModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    public String getName() {
        return "KasperskyWebFilter";
    }

    /** Setting the update */
    @ReactMethod
    public void updateDatabase () {
        final Context updateContext = getReactApplicationContext().getApplicationContext();
        initializeSdk(updateContext, WebFilterModule.this);
        Updater updater = Updater.getInstance(); //
        try {
            updater.updateAntivirusBases((i, i1) -> false);
            Log.i("TAG", "WEB_FILTER_STARTED");
            sendStringEvent(getReactApplicationContext(), "Status", "Completed");
        } catch (SdkLicenseViolationException e) {
            sendStringEvent(getReactApplicationContext(), "Status", "Failed");
            throw new RuntimeException(e);
        }
    }

    /** Update and initialize the SDK */
    @ReactMethod
    public void onCreate() {
        super.initialize();
        final Context context = Objects.requireNonNull(getCurrentActivity()).getApplicationContext();
        Log.i(TAG, "Sample application started");
        new Thread(() -> initializeSdk(context, WebFilterModule.this)).start();
    }

    /** Check for product keys, listener, */
    private void initializeSdk(Context context, SdkInitListener listener)
    {
        final File basesPath = getCurrentActivity().getDir("bases", Context.MODE_PRIVATE);
        ServiceStateStorage generalStorage  = new DataStorage(context, DataStorage.GENERAL_SETTINGS_STORAGE);

        try {
            KavSdk.initSafe(context, basesPath, generalStorage, getNativeLibsPath());

            final SdkLicense license = KavSdk.getLicense();
            if (!license.isValid()) {
                if (!license.isClientUserIDRequired()) {
                    license.activate(null);
                }
            }

        } catch (SdkLicenseNetworkException | SdkLicenseDateTimeException | IOException e) {
            mSdkInitStatus = InitStatus.InitFailed;
            listener.onInitializationFailed("Init failure: " + e.getMessage());
            return;
        } catch (SdkLicenseException e) {
            mSdkInitStatus = InitStatus.NeedNewLicenseCode;
            listener.onInitializationFailed("New license code is required: " + e.getMessage());
            return;
        }

        SdkLicense license = KavSdk.getLicense();
        if (!license.isValid()) {
            mSdkInitStatus = InitStatus.NeedNewLicenseCode;
            listener.onInitializationFailed("New license code is required");
            return;
        }
        Antivirus mAntivirusComponent = AntivirusInstance.getInstance();
        File scanTmpDir = getCurrentActivity().getDir("scan_tmp", Context.MODE_PRIVATE);
        File monitorTmpDir = getCurrentActivity().getDir("monitor_tmp", Context.MODE_PRIVATE);

        try {
            mAntivirusComponent.initAntivirus(context, scanTmpDir.getAbsolutePath(), monitorTmpDir.getAbsolutePath());
        } catch (SdkLicenseViolationException | IOException e) {
            mSdkInitStatus = InitStatus.InitFailed;
            listener.onInitializationFailed(e.getMessage());
            return;
        }

        mSdkInitStatus = InitStatus.InitedSuccesfully;
        sendEvent(getReactApplicationContext(), "Initialize Status", Boolean.parseBoolean(String.valueOf(mSdkInitStatus)));
    }

    public void onInitializationFailed(String reason) {

    }
    @ReactMethod
    public void onSdkInitialized() {
        Context context = getCurrentActivity().getApplicationContext();
        Log.i(TAG, "In startWebFilterWithAllAccess");
        int flags = WEB_FILTER_FLAGS;
        // no wifi-proxy in mini-example
        flags &= ~WebFilterControl.DISABLE_PROXY;
        int webFilterPort = 8082;

        WebFilterControl webFilterControl = null;
        WebFilterControlFactory factory = new WebFilterControlFactoryImpl();
        try {
            webFilterControl = factory.create((WebAccessHandler) (new UrlFilterImpl()), context);
        } catch (SdkLicenseViolationException e) {
            e.printStackTrace();
        }

        assert webFilterControl != null;
        webFilterControl.setCategoriesEnabled(new UrlCategory[] {
                UrlCategory.SocialNet,
                UrlCategory.Malware,
                UrlCategory.Phishing});
        webFilterControl.enable(true);
        Log.i(TAG, "In startWebFilter. Finish ALl OK");
        Log.i(TAG, "This status is: " + webFilterControl);

        mWebFilter = webFilterControl;

        mThread = new Thread() {
            @Override
            public void run() {

                Log.i(TAG, "WebFilter init complete");

                if (mOpCompletedListener != null) {
                    mOpCompletedListener.onAvCompleted("Now open Google Chrome:\n\nSocial networks must be blocked,\nporn sites must be blocked,\nany malware URLs must be blocked\nany other sites are not affected");
                }
                Log.i(TAG, "WebFilter finished");
            }
        };
        mThread.start();
    }

    private enum InitStatus {
        NotInited,
        InitInProgress,
        InitedSuccesfully,
        InsufficientPermissions,
        NeedNewLicenseCode,
        InitAntivirusFailed,
        InitFailed;

        public static boolean isError(InitStatus initStatus) {
            return initStatus != NotInited &&
                    initStatus != InitInProgress &&
                    initStatus != InitedSuccesfully;
        }
    }

    private String getNativeLibsPath() {
        // If you do not want to store native libraries in the application data directory
        // SDK provides the ability to specify another path (otherwise, you can simply omit pathToLibraries parameter).
        // Note: storing the libraries outside the application data directory is not secure as
        // the libraries can be replaced. In this case, the libraries correctness checking is required.
        // Besides, the specified path must be to device specific libraries, i.e. you should care about device architecture.
        final Context context = Objects.requireNonNull(getCurrentActivity()).getApplicationContext();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.applicationInfo.nativeLibraryDir;

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendEvent(ReactContext reactContext, String eventName,  boolean message) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, message);
    }

    /** Push the result to the React Native layer */
    private void sendStringEvent(ReactContext reactContext, String eventName,  String message) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, message);
    }
}