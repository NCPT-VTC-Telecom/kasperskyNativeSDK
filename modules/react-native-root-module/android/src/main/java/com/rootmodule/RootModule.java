package com.rootmodule;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.checkroot.DataStorage;
import com.checkroot.SdkInitListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.kavsdk.KavSdk;
import com.kavsdk.antivirus.Antivirus;
import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.rootdetector.RootDetector;
import com.kavsdk.shared.iface.ServiceStateStorage;
import com.kavsdk.updater.Updater;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class RootModule extends ReactContextBaseJavaModule implements SdkInitListener {

    private static final String TAG = "CHECK_ROOT_EXAMPLE";
    private volatile InitStatus mSdkInitStatus = InitStatus.NotInited;
    private Antivirus mAntivirusComponent;
    private Thread scannerThread;

    Updater updater = Updater.getInstance();


    RootModule(ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return "KasperskyRootSDK";
    }

    @ReactMethod
    public String displayName(String string) {
        Log.d("Note: ", "Kaspersky is ready to run");
        return string;
    }


    @ReactMethod
    public void onCreate() {
        Log.i(TAG, "Check root sampling started");

        Thread thread = new Thread(() -> {
            final Context context = Objects.requireNonNull(getCurrentActivity().getApplicationContext());
            try {
                initializeSdk(context, RootModule.this);
                onSdkInitialized();
            } catch (SdkLicenseViolationException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

    }

    @Override
    public void onInitializationFailed(final String reason) {
        Log.e(TAG, "SDK initialization status=" + mSdkInitStatus + ", reason=" + reason);
    }
    
    @ReactMethod
    public boolean onSdkInitialized() {
        Updater updater = Updater.getInstance();

//        Log.i("Root checking", "Root checking init");
//        scannerThread = new Thread() {
//            public void run() {
//                try {
//                    final boolean isRootedDevice = RootDetector.getInstance().checkRoot();
//                    Log.i(TAG, "Is Rooted Device: " + (isRootedDevice ? "YES" : "NO"));
//                    sendEvent(getReactApplicationContext(), "CheckRoot", isRootedDevice);
//                } catch (SdkLicenseViolationException error) {
//                    Log.e(TAG, "Check is device rooted failed due to license violation: " + error.getMessage());
//                }
//            }
//        };
//        scannerThread.start();
        try {
            updater.updateAntivirusBases((i, i1) -> false);
            sendEvent(getReactApplicationContext(), "CheckRoot", RootDetector.getInstance().checkRoot());
            Log.i("Check root", "Check" + RootDetector.getInstance().checkRoot());
            return RootDetector.getInstance().checkRoot();
        } catch (SdkLicenseViolationException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public void onInitSuccess() {
    }

    @Override
    public void onInitFailure(String message) {
    }


    @ReactMethod
    public String initializeSdk(Context context, SdkInitListener listener) throws SdkLicenseViolationException, IOException {

        Log.i(TAG, "SDK initialization started");
        final File basesPath = getCurrentActivity().getApplicationContext().getDir("bases", Context.MODE_PRIVATE);

        Antivirus mAntivirusComponent = AntivirusInstance.getInstance();
        ServiceStateStorage generalStorage = new DataStorage(context, DataStorage.GENERAL_SETTINGS_STORAGE);


        try {
            KavSdk.initSafe(getCurrentActivity().getApplicationContext(), basesPath, generalStorage, getNativeLibsPath());

            final SdkLicense license = KavSdk.getLicense();
            if (!license.isValid()) {
                if (!license.isClientUserIDRequired()) {
                    license.activate(null);
                }
            }


        } catch (SdkLicenseException e) {
            throw new RuntimeException(e);
        }

        SdkLicense license = KavSdk.getLicense();
        if (!license.isValid()) {
            mSdkInitStatus = InitStatus.NeedNewLicenseCode;
            listener.onInitializationFailed("New license code is required");
            return null;
        }


        File scanTempDir = getCurrentActivity().getApplicationContext().getDir("scan_temp", Context.MODE_PRIVATE);
        File monitorTempDir = getCurrentActivity().getApplicationContext().getDir("monitor_temp", Context.MODE_PRIVATE);

        try {
            mAntivirusComponent.initAntivirus(getCurrentActivity().getApplicationContext(),
                    scanTempDir.getAbsolutePath(), monitorTempDir.getAbsolutePath());

        } catch (SdkLicenseViolationException e) {
            mSdkInitStatus = InitStatus.InitFailed;
            listener.onInitializationFailed(e.getMessage());
            return null;
        } catch (IOException ioe) {
            mSdkInitStatus = InitStatus.InitFailed;
            listener.onInitializationFailed(ioe.getMessage());
            return null;
        }

        mSdkInitStatus = InitStatus.InitedSuccesfully;
        listener.onSdkInitialized();
        return "";
    };

        private enum InitStatus {
            NotInited,
            InitInProgress,
            InitedSuccesfully,
            InsufficientPermissions,
            NeedNewLicenseCode,
            InitAntivirusFailed,
            InitFailed, Inited;

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
        try {
            PackageInfo packageInfo = getCurrentActivity().getPackageManager().getPackageInfo(getCurrentActivity().getPackageName(), 0);
            return packageInfo.applicationInfo.nativeLibraryDir;

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @ReactMethod
    public void addListener(String eventName) {
        // React Native will manage listeners through JavaScript
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // React Native will manage removal of listeners through JavaScript
    }

    /** This function is to return the event for React Native to catch
     * and export the function */
    private void sendEvent(ReactContext reactContext, String eventName, @Nullable boolean message) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, message);
    }

}
