package wifimodules;

import com.kavsdk.antivirus.easyscanner.EasyResult;
import com.kavsdk.securityscanner.SecurityResult;

public interface SdkEventstListener
{
    void onInitializationFailed(String reason);
    void onSdkInitialized();

    default void onEasyScanFinishedEvent(EasyResult result) {

    }
    default void onSecurityScanFinishedEvent(SecurityResult result) {

    }

    default void onEvent(String tag, String msg) {

    }
}

