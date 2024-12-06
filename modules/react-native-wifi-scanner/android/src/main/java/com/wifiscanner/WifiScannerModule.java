package com.wifiscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import wifimodules.SdkEventstListener;
import wifimodules.SdkInitListener;
import wifimodules.AvCompletedListener;
import wifimodules.DataStorage;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.modules.core.DeviceEventManagerModule;
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
import com.kavsdk.webfilter.WebFilterControl;
import com.kavsdk.wifi.WifiCheckResult;
import com.kavsdk.wifi.WifiReputation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class WifiScannerModule extends ReactContextBaseJavaModule implements SdkInitListener {
  private static final String TAG = "WIFI_SCAN_EXAMPLE";
  private volatile WifiScannerModule.InitStatus mSdkInitStatus = InitStatus.NotInited;
  private Antivirus mAntivirusComponent;
  private Thread mScanThread;
  private Context mContext;

  private Antivirus mAntivirus;
  private Handler mHandler;
  private SdkEventstListener mExtListener;
  WebFilterControl mWebFilter;
  private boolean isSdkReady;

  private AvCompletedListener mAvCompletedListener;


  WifiScannerModule(ReactApplicationContext context) {
    super(context);
  }

  @NonNull
  @Override
  public String getName() {
    return "KasperskyWifiScanner";
  }

  @ReactMethod
  public void displayName() {
    Log.d("Note: ", "Kaspersky Wifi Scanner is ready to run");

  }


  @ReactMethod
  public void onCreate() {
    Log.i(TAG, "Sample application started");
    new Thread(new Runnable() {
      @Override
      public void run() {
        final Context context = getReactApplicationContext().getApplicationContext();
        try {
          initializeSdk(context, WifiScannerModule.this);
        } catch (SdkLicenseViolationException | IOException e) {
          throw new RuntimeException(e);
        }
      }
    }).start();
  }

  @ReactMethod
  public void updateDatabase () {
    Updater updater = Updater.getInstance();
    Context reactContext = getReactApplicationContext();
    try {
      sendEvent((ReactContext) reactContext, "updateDatabase", "Đang cập nhật Database");
      updater.updateAntivirusBases((i, i1) -> false);
      Log.i("TAG", "WIFI_SCANNER_STARTED");
    } catch (SdkLicenseViolationException e) {
      throw new RuntimeException(e);
    }
  }



  @ReactMethod
  public void initializeSdk(Context context, SdkInitListener listener) throws SdkLicenseViolationException, IOException {


    final File basesPath = context.getDir("bases", Context.MODE_PRIVATE);
    ServiceStateStorage generalStorage = new DataStorage(context, DataStorage.GENERAL_SETTINGS_STORAGE);

    try {
      KavSdk.initSafe(context, basesPath, generalStorage, getNativeLibsPath());
      updateDatabase();
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

    File scanTmpDir = context.getDir("scan_tmp", Context.MODE_PRIVATE);
    File monitorTmpDir = context.getDir("monitor_tmp", Context.MODE_PRIVATE);

    try {
      mAntivirusComponent.initAntivirus(context,
              scanTmpDir.getAbsolutePath(),
              monitorTmpDir.getAbsolutePath());

    } catch (SdkLicenseViolationException | IOException e) {
      mSdkInitStatus = InitStatus.InitFailed;
      listener.onInitializationFailed(e.getMessage());
      return;
    }

      mSdkInitStatus = InitStatus.InitedSuccesfully;
      Log.d(TAG, "Result" + mSdkInitStatus);
      sendEvent(getReactApplicationContext(), "Status", String.valueOf(mSdkInitStatus));
  }


  public void onInitializationFailed(final String reason) {

  }


  @ReactMethod
  @SuppressLint("MissingPermission")
    public boolean onSdkInitialized () {
      boolean isNetworkSafe = false;
      Context context = getCurrentActivity().getApplicationContext();
      ReactContext reactContext = getReactApplicationContext();
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      WifiInfo wi = wifiManager.getConnectionInfo();

        try {

          final WifiReputation wifiReputation = new WifiReputation(context);
          WifiCheckResult wifiCheckResult = wifiReputation.checkCurrentNetwork();
//        Log.i(TAG, "Wifi Safe Status: " + wifiReputation.isCurrentNetworkSafe() + " " + wifiCheckResult.getBssid());
          Log.i(TAG, "KSN Wifi check result: " + wifiCheckResult.getVerdict().name());
          isNetworkSafe = wifiReputation.isCurrentNetworkSafe();
          Log.d(TAG, "Is network safe:" + isNetworkSafe);
          if (mExtListener != null) {
            mExtListener.onEvent(TAG, "Wifi Safe Status: " + wifiReputation.isCurrentNetworkSafe());
            mExtListener.onEvent(TAG, "BSSID: " + wifiCheckResult.getBssid());
            mExtListener.onEvent(TAG, "KSN Wifi check result: " + wifiCheckResult.getVerdict().name());
          }
        } catch (SdkLicenseViolationException | IOException e) {
          e.printStackTrace();
        }


    return isNetworkSafe;
  }

  private Object getScanResultSecurity(ScanResult scanResult) {
      return null;
  }


  public void onInitSuccess () {

    }

    // AddListener and removeListeners are required for event subscription in RN
    @ReactMethod
    public void addListener (String eventName){
      // React Native will manage listeners through JavaScript
    }

    @ReactMethod
    public void removeListeners (Integer count){
      // React Native will manage removal of listeners through JavaScript
    }

    public void onInitFailure (String message){
    }

    /**
     * Constructor of getNativeLibsPath()
     */
    public String getNativeLibsPath () {
      try {
        PackageInfo packageInfo = getCurrentActivity().getPackageManager().getPackageInfo(getCurrentActivity().getPackageName(), 0);
        return packageInfo.applicationInfo.nativeLibraryDir;
      } catch (PackageManager.NameNotFoundException error) {
        throw new RuntimeException(error);
      }
    }

    // Method to send log messages to JS via event emitter
    private void sendEvent (ReactContext reactContext, String eventName, @Nullable String message){
      List<String> messageList = Arrays.asList(message);
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit(eventName, message);
    }


    public enum InitStatus {
      NotInited,
      InitInProgress,
      InitedSuccesfully,
      InsufficientPermissions,
      NeedNewLicenseCode,
      InitAntivirusFailed,
      InitFailed, Inited;
    }

      public static boolean isError(InitStatus initStatus) {
        return initStatus != InitStatus.NotInited &&
                initStatus != InitStatus.InitInProgress &&
                initStatus != InitStatus.InitedSuccesfully;
      }
    }

    
