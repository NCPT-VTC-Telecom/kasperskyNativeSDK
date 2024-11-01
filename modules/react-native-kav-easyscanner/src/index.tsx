import {
  DeviceEventEmitter,
  EmitterSubscription,
  NativeModules,
} from 'react-native';
import {ScanType} from './interface';

/** Set the permission on storage */

export const updateDatabase = (): Promise<any> => {
  const {KasperskyScannerSDK} = NativeModules;

  return new Promise((resolve, reject) => {
    let result: EmitterSubscription;
    result = DeviceEventEmitter.addListener('Status', data => {
      console.log(data);
      resolve(data);
      result.remove();
    });

    try {
      KasperskyScannerSDK.updateDatabase();
    } catch (error) {
      reject(error);
    }
  });
};

export const getPermission = (): Promise<any> => {
  const {KasperskyScannerSDK} = NativeModules;

  return new Promise((resolve, reject) => {
    let result: EmitterSubscription;
    result = DeviceEventEmitter.addListener('Permission', data => {
      resolve(data);
      result.remove();
    });

    try {
      KasperskyScannerSDK.getPermissionClick();
    } catch (error) {
      reject(error);
    }
  });
};

export const kasperskyEasyScanner = (scanType: ScanType): Promise<any> => {
  const {KasperskyScannerSDK} = NativeModules;

  return new Promise((resolve, reject) => {
    let result: EmitterSubscription;
    result = DeviceEventEmitter.addListener('AllResult', data => {
      resolve(data);
      result.remove();
    });

    try {
      KasperskyScannerSDK.onCreate(scanType);
    } catch (error) {
      reject(error);
    }
  });
};

export default {kasperskyEasyScanner, updateDatabase, getPermission};
