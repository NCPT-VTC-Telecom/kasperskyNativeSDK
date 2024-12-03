// import { Button } from '@mui/material';
import React from 'react';
import {
  DeviceEventEmitter,
  Image,
  PermissionsAndroid,
  TouchableOpacity,
} from 'react-native';
import {StyleSheet, Text, View, Alert} from 'react-native';

import Loading from './components/Loading';

import {Switch, Button, Divider, ActivityIndicator} from 'react-native-paper';

import Ionicons from 'react-native-vector-icons/Ionicons';
import FontAwesome from 'react-native-vector-icons/FontAwesome';
import {images} from '../../assets';
import {useAppNavigation} from '../../navigation/AppNavigation';
import {iniitializeSdk, wifiScanning} from 'react-native-wifi-scanner';
import FadeInView from '../../components/AnimatedView';
import colors from '../../themes/colors/colors';

import Geolocation, {GeoPosition} from 'react-native-geolocation-service';

interface GeolcationError {
  code?: number;
  message?: string;
}

const WifiScanner: React.FC<any> = () => {
  const navigation = useAppNavigation();
  const styles = createStyles();
  const [loading, setLoading] = React.useState<boolean>(false);
  const [loadingSdk, setLoadingSdk] = React.useState<boolean>(false);

  const [locationError, setLocationError] = React.useState<GeolcationError>({
    code: 0,
    message: '',
  });
  const [currentLocation, setCurrentLocation] = React.useState<GeoPosition>();

  const [result, setResult] = React.useState<any>('');

  const [error, setError] = React.useState<string>();

  Geolocation.getCurrentPosition(
    position => {
      console.log('Psotion', position);
    },
    error => {
      setLocationError(error);
    },
    {enableHighAccuracy: true, timeout: 20000, maximumAge: 1000},
  );

  const initializingSdk = async () => {
    try {
      setLoadingSdk(true);
      const result = await iniitializeSdk();
      setResult(result);
      setLoadingSdk(false);
    } catch (error) {
      console.error(error);
    }
  };

  const requestPermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: 'Location Permission',
          message: 'This app needs access to your location',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        },
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log('You can use the location');
        return true;
      } else {
        console.log('Location permission denied');
        return false;
      }
    } catch (err) {
      console.warn(err);
    }
  };

  const onScan = async () => {
    try {
      if (locationError.message === 'Location permission not granted.') {
        Alert.alert(
          'Thông báo',
          'Vui lòng cấp quyền truy cập vị trí để sử dụng tính năng này',
          [
            {
              text: 'Hủy',
              onPress: () => {
                setError(
                  'Bạn cần phải cho phép quyền truy cập địa điểm để Kaspersky có thể thực hiện',
                );
              },
              style: 'destructive',
            },
            {
              text: 'Cài đặt',
              onPress: requestPermission,
            },
          ],
        );
        return;
      }
      const result = await wifiScanning();

      // setLoading(false);
    } catch (error) {
      console.log(error);
    }
  };

  if (loading) return <Loading />;

  return (
    <FadeInView style={{padding: 8, flex: 1}}>
      <TouchableOpacity onPress={() => navigation.goBack()}>
        <Ionicons name="arrow-back-circle" size={40} color="#29CCB1" />
      </TouchableOpacity>
      <Image
        source={images.wifi}
        resizeMode="contain"
        style={{height: 200, width: '100%'}}
      />
      <View
        style={{
          marginHorizontal: 8,
          justifyContent: 'center',
          alignItems: 'center',
        }}>
        <Text style={styles.title}>Quét Wifi</Text>
        <Text style={styles.description}>
          Sử dụng Wifi an toàn cùng với Kaspersky
        </Text>
        <Text style={styles.descriptionText}>
          Đảm bảo tất cả những thiết bị kết nối vào mạng Wifi của bạn đều an
          toàn. Hãy nhấn vào nút bên dưới để bắt đầu quét mạng lưới Wifi kết nối
          của bạn. Và từ đó bạn sẽ được thông báo về những thiết bị nào đang kết
          nối vào mạng Wifi của bạn.
        </Text>
        <View style={{flexDirection: 'row', gap: 8}}>
          <Button onPress={initializingSdk} style={styles.button}>
            <Text style={{color: 'white'}}>Cập nhật CSDL</Text>
          </Button>
          <Button
            onPress={onScan}
            style={[styles.button, {backgroundColor: colors.dark.primary}]}>
            <Text style={{color: 'white'}}>Nhấn để quét</Text>
          </Button>
        </View>
      </View>
      <View>
        {loadingSdk == true && !result ? (
          <View
            style={{
              flexDirection: 'row',
              justifyContent: 'center',
              alignItems: 'center',
              gap: 8,
            }}>
            <ActivityIndicator size={20} />
            <Text>
              Đang cập nhật cơ sở dữ liệu, vui lòng đợi trong giây lát
            </Text>
          </View>
        ) : (
          <View
            style={{
              flexDirection: 'row',
              justifyContent: 'center',
              alignItems: 'center',
              gap: 8,
            }}>
            <FontAwesome name="check-circle" size={20} color="green" />
            <Text>Cập nhật dữ liệu thành công</Text>
          </View>
        )}
      </View>
      <View style={{flex: 1, alignItems: 'center', justifyContent: 'flex-end'}}>
        {error && <Text style={{textAlign: 'center'}}>{error}</Text>}
      </View>
    </FadeInView>
  );
};

export default WifiScanner;

const createStyles = () => {
  return StyleSheet.create({
    title: {color: '#1D1D1B', fontWeight: '700', fontSize: 30},
    description: {
      color: '#1D1D1B',
      lineHeight: 20,
      fontSize: 15,
      marginVertical: 8,
    },
    descriptionText: {
      color: '#1D1D1B',
      lineHeight: 20,
      fontSize: 12,
      marginVertical: 8,
    },
    subtitle: {
      fontSize: 25,
      fontWeight: '800',
      marginBottom: 8,
      color: '#1D1D1B',
    },
    view: {flexDirection: 'row', alignItems: 'center'},
    button: {
      backgroundColor: colors.dark.primary1,
      borderRadius: 8,
      marginVertical: 8,
      flex: 1,
    },
  });
};
