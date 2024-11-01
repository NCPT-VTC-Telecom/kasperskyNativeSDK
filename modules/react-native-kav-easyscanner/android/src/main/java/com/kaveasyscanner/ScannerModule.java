package com.kaveasyscanner;

import static android.os.Build.VERSION.SDK_INT;
import static android.provider.Settings.System.getString;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.easyscanner.SdkInitListener;
import com.easyscanner.AvCompletedListener;
import com.easyscanner.DataStorage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.kavsdk.KavSdk;
import com.kavsdk.antivirus.Antivirus;
import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.easyscanner.EasyMode;
import com.kavsdk.antivirus.easyscanner.EasyResult;
import com.kavsdk.antivirus.easyscanner.EasyScanner;
import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseDateTimeException;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdk.license.SdkLicenseNetworkException;
import com.kavsdk.license.SdkLicenseViolationException;

import com.kavsdk.shared.iface.ServiceStateStorage;
import com.kavsdk.updater.Updater;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ScannerModule extends ReactContextBaseJavaModule implements SdkInitListener {

    private static final String TAG = "EASY_SCANNER_EXAMPLE";
    // Create thread for processing
    private Thread scannerThread;
    private volatile InitStatus mSdkInitStatus = InitStatus.NotInited;
    private Antivirus mAntivirusComponent;
    private Thread mScanThread;
    private AvCompletedListener mAvCompletedListener;

    /** Status on accessing file */
    private boolean hasAccessToAllFiles = false;
    private boolean hasLocationPermission = false;

    /** Update permission code */
    private static final int ALL_FILES_PERMISSION_REQ_CODE = 4;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    ScannerModule(ReactApplicationContext context) {
        super(context);
    }

    @NonNull
    @Override
    public String getName() {
        return "KasperskyScannerSDK";
    }

    @ReactMethod
    public void displayName() {
        Log.d("Note: ", "Kaspersky Scanner is ready to run");

    }


    @ReactMethod
    public void onCreate(String mode) {
        Log.i(TAG, "Scanner sampling started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Context context = getReactApplicationContext().getApplicationContext();
                try {
                    initializeSdk(context, ScannerModule.this);
                    onSdkInitialized(mode);
                } catch (SdkLicenseViolationException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }


    @ReactMethod
    public void initializeSdk(Context context, SdkInitListener listener) throws SdkLicenseViolationException, IOException {


        final File basesPath = getCurrentActivity().getApplicationContext().getDir("bases", Context.MODE_PRIVATE);
        ServiceStateStorage generalStorage  = new DataStorage(getCurrentActivity().getApplicationContext(), DataStorage.GENERAL_SETTINGS_STORAGE);

        try {
            KavSdk.initSafe(getCurrentActivity().getApplicationContext(), basesPath, generalStorage, getNativeLibsPath());

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

        mAntivirusComponent = AntivirusInstance.getInstance();

        File scanTmpDir = getCurrentActivity().getApplicationContext().getDir("scan_tmp", Context.MODE_PRIVATE);
        File monitorTmpDir = getCurrentActivity().getApplicationContext().getDir("monitor_tmp", Context.MODE_PRIVATE);

        try {
            mAntivirusComponent.initAntivirus(getCurrentActivity().getApplicationContext(),
                    scanTmpDir.getAbsolutePath(), monitorTmpDir.getAbsolutePath());

        } catch (SdkLicenseViolationException e) {
            mSdkInitStatus = InitStatus.InitFailed;
            listener.onInitializationFailed(e.getMessage());
            return;
        } catch (IOException ioe) {
            mSdkInitStatus = InitStatus.InitFailed;
            listener.onInitializationFailed(ioe.getMessage());
            return;
        }

        mSdkInitStatus = InitStatus.InitedSuccesfully;
        listener.onSdkInitialized();
    }



    public void onInitializationFailed(final String reason) {
        Log.e(TAG, "SDK initialization status=" + mSdkInitStatus + ", reason=" + reason);
    }

    @Override
    public void onSdkInitialized() {

    }

    /** Check update with a dedicated update function*/
    @ReactMethod
    public void updateDatabase() throws SdkLicenseViolationException, IOException {
        initializeSdk(getCurrentActivity().getApplicationContext(), ScannerModule.this);
        Updater updater = Updater.getInstance();

        try {
            updater.updateAntivirusBases((i, i1) -> false);
            sendEvent(getReactApplicationContext(), "Status", "Thành công");
        } catch (SdkLicenseViolationException e) {
            sendEvent(getReactApplicationContext(), "Status", "Thất bại");
            throw new RuntimeException(e);

        }
    }


    @ReactMethod
    public void getPermissionClick() {
        Context context = getCurrentActivity().getApplicationContext();
        hasAccessToAllFiles = getFileAccessStatus();
        if (!hasAccessToAllFiles && SDK_INT == 29) {
            Log.i(TAG, "Requesting RW to Storage. API 29");
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    PERMISSIONS_STORAGE,
                    ALL_FILES_PERMISSION_REQ_CODE

            );
        }

        if (!hasAccessToAllFiles && SDK_INT >= Build.VERSION_CODES.R) {
            Log.i(TAG, "Requesting Manage All Storage. API 30+");
            requestAllFilesAccessPermission();
        }

        if (hasAccessToAllFiles) {
//            appendLogln("", getString(R.string.all_files_access_status) + hasAccessToAllFiles);
        }
    }

    public void requestAllFilesAccessPermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getCurrentActivity().getPackageName()));
            startActivityForResult(intent, ALL_FILES_PERMISSION_REQ_CODE);
            System.out.println("Request Access file permission");
        } catch (ActivityNotFoundException ignore) {

        }
    }

    private void startActivityForResult(Intent intent, int allFilesPermissionReqCode) {
    }

    private void startActivityForResult() {
    }


    @ReactMethod
    public void onSdkInitialized(String mode) throws SdkLicenseViolationException {
        Log.i(TAG, "EasyScanner started");

        mScanThread = new Thread() {
            @Override
            public void run() {
                /** Updating database to test on Android Device */
                Updater updater = Updater.getInstance();
                try {
                    updater.updateAntivirusBases((i, i1) -> false);
                } catch (SdkLicenseViolationException e) {
                    throw new RuntimeException(e);
                }

                /** Check permission */
                boolean hasAccess = true;
                int permission = ActivityCompat.checkSelfPermission(getCurrentActivity().getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                hasAccess = (permission == PackageManager.PERMISSION_GRANTED);

                /** Initialize scanning components*/
                EasyScanner easyScanner = mAntivirusComponent.createEasyScanner();
                easyScanner.scan(EasyMode.valueOf(mode));
                final EasyResult result = easyScanner.getResult();

                Log.d(TAG, "Scan finished ");
                Log.d(TAG, "- Files found: "         + result.getFilesScanned());
                Log.d(TAG, "- Files calculated: "    + result.getFilesCount());
                Log.d(TAG, "- Objects scanned: "     + result.getObjectsScanned());
                Log.d(TAG, "- Objects skipped: "     + result.getObjectsSkipped());
                Log.d(TAG, "- Viruses found: "       + result.getMalwareList().size());
                Log.d(TAG, "- Riskware found: "      + result.getRiskwareList().size());
                Log.d(TAG, "- Rooted: "              + result.isRooted());


                // Export the RESULT
                /** For the references for sending all as a full result */
                WritableArray resultsArray = Arguments.createArray();
                resultsArray.pushString("Files found: "         + result.getFilesScanned());
                resultsArray.pushString("Files calculated: "    + result.getFilesCount());
                resultsArray.pushString("Object found: "        + result.getObjectsScanned());
                resultsArray.pushString("Object skipped: "      + result.getObjectsSkipped());
                resultsArray.pushString("Malware found: "       + result.getMalwareList().size());
                resultsArray.pushString("Risk ware found: "     + result.getRiskwareList().size());

                sendEvent(getReactApplicationContext(), "AllResult", resultsArray.toString());

                /** Send the exported result to each scanning success*/
                sendEvent(getReactApplicationContext(), "FilesFound",       String.valueOf(result.getFilesScanned()));
                sendEvent(getReactApplicationContext(), "FilesCalculated",  String.valueOf(result.getFilesCount()));
                sendEvent(getReactApplicationContext(), "ObjectFound",      String.valueOf(result.getObjectsScanned()));
                sendEvent(getReactApplicationContext(), "ObjectSkipped",    String.valueOf(result.getObjectsSkipped()));
                sendEvent(getReactApplicationContext(), "VirusFound",       String.valueOf(result.getMalwareList().size()));
                sendEvent(getReactApplicationContext(), "VirusFound",       String.valueOf(result.getRiskwareList().size()));


                if (mAvCompletedListener != null) {
                    mAvCompletedListener.onAvCompleted("Scan complete!\n\n"
                            + "- Files found: "      + result.getFilesScanned() + "\n"
                            + "- Files calculated: " + result.getFilesCount() + "\n"
                            + "- Objects scanned: "  + result.getObjectsScanned() + "\n"
                            + "- Objects skipped: "  + result.getObjectsSkipped() + "\n"
                            + "- Viruses found: "    + result.getMalwareList().size() + "\n"
                            + "- Riskware found: "   + result.getRiskwareList().size() + "\n"
                            + "- Rooted: "           + (result.isRooted() ? "YES" : "NO"));
                }
                Log.i(TAG, "EasyScanner finished");
            }
        };
        mScanThread.start();
    }


    public void onInitSuccess() {

    }

    // AddListener and removeListeners are required for event subscription in RN
    @ReactMethod
    public void addListener(String eventName) {
        // React Native will manage listeners through JavaScript
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // React Native will manage removal of listeners through JavaScript
    }

    public void onInitFailure(String message) {

    }


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

    /** Constructor of getNativeLibsPath() */
    public String getNativeLibsPath() {
        try {
            PackageInfo packageInfo = getCurrentActivity().getPackageManager().getPackageInfo(getCurrentActivity().getPackageName(), 0);
            return packageInfo.applicationInfo.nativeLibraryDir;
        } catch (PackageManager.NameNotFoundException error) {
            throw new RuntimeException(error);
        }
    }

    // Method to send log messages to JS via event emitter
    private void sendEvent(ReactContext reactContext, String eventName, @Nullable String message) {
        List<String> messageList = Collections.singletonList(message);
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, message);
    }

    private boolean getFileAccessStatus() {
        boolean hasAccess = true;
        if (SDK_INT >= 30) {
            hasAccess = Environment.isExternalStorageManager();
        }

        if (SDK_INT == 29) {
            int permission = ActivityCompat.checkSelfPermission(getCurrentActivity().getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            hasAccess = (permission == PackageManager.PERMISSION_GRANTED);
        }

        return hasAccess;
    }

}