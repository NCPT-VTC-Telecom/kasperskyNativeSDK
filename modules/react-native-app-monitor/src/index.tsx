import { DeviceEventEmitter, EmitterSubscription, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-app-monitor' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const kasperskyAppMonitor = (): Promise<any> => {
  const { AppMonitorSDK } = NativeModules;

  return new Promise((resolve, reject) => {
    let result: EmitterSubscription;
    result = DeviceEventEmitter.addListener('AllResult', data => {
      resolve(data);
      result.remove();
    });

    try {
      AppMonitorSDK.onCreate();
    } catch (error) {
      console.log(error);
      reject(error);
    }
  });
};
/** Set the PRODUCT KEY for first Initalize the API */
// const DEFAULT_KEY = 'JKBSB-WMEQD-KY8MG-1CGGW';
export default kasperskyAppMonitor;
