package com.appmonitor;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appmonitor.monitor.AvStatusListener;
import com.appmonitor.monitor.DataStorage;
import com.appmonitor.monitor.SdkEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.kavsdk.KavSdk;
import com.kavsdk.antivirus.Antivirus;
import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.SuspiciousThreatType;
import com.kavsdk.antivirus.ThreatInfo;
import com.kavsdk.antivirus.ThreatType;
import com.kavsdk.antivirus.appmonitor.AppInstallationMonitor;
import com.kavsdk.antivirus.appmonitor.AppInstallationMonitorListener;
import com.kavsdk.antivirus.appmonitor.AppInstallationMonitorSuspiciousListener;
import com.kavsdk.license.SdkLicense;
import com.kavsdk.license.SdkLicenseDateTimeException;
import com.kavsdk.license.SdkLicenseException;
import com.kavsdk.license.SdkLicenseNetworkException;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.shared.iface.ServiceStateStorage;
import com.kavsdk.updater.Updater;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class AppMonitorModule extends ReactContextBaseJavaModule implements AvStatusListener, AppInstallationMonitorListener, AppInstallationMonitorSuspiciousListener {


  @NonNull
  @Override
  public String getName() {
    return "AppMonitorSDK";
  }

  /** Setting TAG */
  private static final String TAG = "APP_MONITOR_EXAMPLE";

  private volatile InitStatus mSdkInitStatus = AppMonitorModule.InitStatus.NotInited;
  private volatile String mReport;

  private Antivirus mAntivirusComponent;
  private SdkEventListener mExtListener;
  private AppInstallationMonitor mAppInstallationMonitor;

  private volatile AvStatusListener mAvStatusListener;
  AppMonitorModule(ReactApplicationContext reactApplicationContext) {super(reactApplicationContext);}

  @ReactMethod
  public void onCreate() {

    Log.i(TAG, "Sample application started");
    new Thread(() -> {
        try {
            initializeSdk();
        } catch (SdkLicenseException e) {
            throw new RuntimeException(e);
        }
    }).start();
  }

  private void initializeSdk() throws SdkLicenseException {


    onStatus("SDK initialization started");
    mSdkInitStatus = InitStatus.InitInProgress;

    final File basesPath = Objects.requireNonNull(getCurrentActivity()).getApplicationContext().getDir("bases", Context.MODE_PRIVATE);
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
      onInitializationFailed(InitStatus.InitFailed, "Init failure: " + e.getMessage());
      return;
    } catch (SdkLicenseException e) {
      onInitializationFailed(InitStatus.NeedNewLicenseCode, "New license code is required: " + e.getMessage());
      return;
    }

      SdkLicense license = KavSdk.getLicense();
    if (!license.isValid()) {
      onInitializationFailed(InitStatus.NeedNewLicenseCode, "New license code is required");
      return;
    }

    mAntivirusComponent = AntivirusInstance.getInstance();

    File scanTmpDir = getCurrentActivity().getApplicationContext().getDir("scan_tmp", Context.MODE_PRIVATE);
    File monitorTmpDir = getCurrentActivity().getApplicationContext().getDir("monitor_tmp", Context.MODE_PRIVATE);

    try {
      mAntivirusComponent.initAntivirus(getCurrentActivity().getApplicationContext(),
              scanTmpDir.getAbsolutePath(), monitorTmpDir.getAbsolutePath());

    } catch (SdkLicenseViolationException e) {
      onInitializationFailed(InitStatus.InitFailed, e.getMessage());
      return;
    } catch (IOException ioe) {
      onInitializationFailed(InitStatus.InitFailed, ioe.getMessage());
      return;
    }

    mSdkInitStatus = InitStatus.InitedSuccesfully;
    startMonitor();
  }


  private void onInitializationFailed(InitStatus status, String reason) {
    mReport = reason;
    mSdkInitStatus = status;

    Log.e(TAG, "SDK initialization status=" + mSdkInitStatus + ", reason=" + reason);
    notifyFailed();
  }

  private void notifyFailed() {
    onStatus("SDK initialization failed, status=" + mSdkInitStatus + ", reason=" + mReport);
  }

  @NonNull
  public boolean onVirusDetected(ThreatInfo threatInfo, ThreatType threatType) {
    String msg = "Found Malware: " + threatInfo.getFileFullPath() +
            " : " + threatInfo.getVirusName();
    if (mExtListener != null) {
      mExtListener.onEvent(TAG, msg);
    }
    Log.e(TAG, msg);
    // Remove the infected application
    AntivirusInstance.getInstance().removeThreat(threatInfo);
    // stop further scanning of this package
    return true;
  }

  @NonNull
  public void onSuspiciousDetected(ThreatInfo threatInfo, SuspiciousThreatType threatType) {
    String msg = "Found Suspicious: " + threatInfo.getFileFullPath() +
            " : " + threatInfo.getVirusName();
    Log.e(TAG, msg);
  }

  private void startMonitor() {
    Updater updater = Updater.getInstance();
    try {
      updater.updateAntivirusBases((i, i1) -> false);
    } catch (SdkLicenseViolationException e) {
      throw new RuntimeException(e);
    }
    AppInstallationMonitor monitor = new AppInstallationMonitor(getCurrentActivity().getApplicationContext());
    monitor.setSkipRiskwareAdware(false);
    monitor.enable((threatInfo, threatType) -> {
      String msg = "Found Malware: " + threatInfo.getFileFullPath() +
              " : " + threatInfo.getVirusName();
      if (mExtListener != null) {
        mExtListener.onEvent(TAG, msg);
      }

      Log.e(TAG, msg);
      // Remove the infected application
      AntivirusInstance.getInstance().removeThreat(threatInfo);
      // stop further scanning of this package
      return true;
    }, (threatInfo, threatType) -> {
      String msg = "Found Suspicious: " + threatInfo.getFileFullPath() +
              " : " + threatInfo.getVirusName();
      Log.e(TAG, msg);
    });
    Log.e(TAG, "App Monitor Enabled");
  }


  @Override
  public void onStatus(String report) {
    Log.i(TAG, report);

    mReport = report;

    AvStatusListener listener = mAvStatusListener;
    if (listener != null) {
      listener.onStatus(report);
    }
  }

  @Override
  public void onInfected(String report) {
    Log.w(TAG, report);

    AvStatusListener listener = mAvStatusListener;
    if (listener != null) {
      listener.onInfected(report);
    }
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
    return getCurrentActivity().getApplicationInfo().nativeLibraryDir;
  }

  /** This will send the event through DeviceEventEmitters*/
  private void sendEvent(ReactContext reactContext, String eventName, @Nullable String message) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, message);
  }
}
