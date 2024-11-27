import {
  DeviceEventEmitter,
  EmitterSubscription,
  NativeModules,
  Platform,
  PermissionsAndroid,
} from 'react-native';

const {KasperskyWebFilter} = NativeModules;

/** Setting to update the database */
export const updateDatabase = (): Promise<any> => {
  return new Promise((resolve, reject) => {
    let result: EmitterSubscription;
    result = DeviceEventEmitter.addListener('Status', data => {
      resolve(data);
      result.remove();
    });

    try {
      KasperskyWebFilter.updateDatabase();
    } catch (error) {
      reject(error);
    }
  });
};

export const activateFilter = (): Promise<any> => {
  // Check whether the device activate the webFilter
  return new Promise((resolve, reject) => {
    let result: EmitterSubscription;
    result = DeviceEventEmitter.addListener('CheckRoot', data => {
      resolve(data);
      result.remove();
    });

    try {
      KasperskyWebFilter.onSdkInitialized();
    } catch (error) {
      reject(error);
    }
  });
};

export default {activateFilter, updateDatabase};
