// import { Button } from '@mui/material';
import React from 'react';
import {DeviceEventEmitter, Image, TouchableOpacity} from 'react-native';
import {StyleSheet, Text, View} from 'react-native';

import Loading from './components/Loading';

import {Switch, Button, Divider} from 'react-native-paper';

import Ionicons from 'react-native-vector-icons/Ionicons';
import {images} from '../../assets';
import {useAppNavigation} from '../../navigation/AppNavigation';
import {iniitializeSdk, wifiScanning} from 'react-native-wifi-scanner';

const WifiScanner: React.FC<any> = () => {
  const navigation = useAppNavigation();
  const styles = createStyles();
  const [loading, setLoading] = React.useState<boolean>(false);

  const [result, setResult] = React.useState<any>('');

  React.useEffect(() => {
    initializingSdk();
  }, []);

  const initializingSdk = async () => {
    try {
      setLoading(true);
      const result = await iniitializeSdk();
      setResult(result);
      setLoading(false);
    } catch (error) {
      console.error(error);
    }
  };

  const onScan = async () => {
    try {
      setLoading(true);
      const result = await wifiScanning();
      setResult(result);
      setLoading(false);
    } catch (error) {
      console.log(error);
    }
  };

  if (loading) return <Loading />;
  return (
    <View style={{padding: 8}}>
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
        <Text style={styles.description}>
          Đảm bảo tất cả những thiết bị kết nối vào mạng Wifi của bạn đều an
          toàn. Hãy nhấn vào nút bên dưới để bắt đầu quét mạng lưới Wifi kết nối
          của bạn. Và từ đó bạn sẽ được thông báo về những thiết bị nào đang kết
          nối vào mạng Wifi của bạn.
        </Text>
        <Button onPress={onScan} style={styles.button}>
          <Text style={{color: 'white'}}>Nhấn để quét</Text>
        </Button>
      </View>
    </View>
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
    subtitle: {
      fontSize: 25,
      fontWeight: '800',
      marginBottom: 8,
      color: '#1D1D1B',
    },
    view: {flexDirection: 'row', alignItems: 'center'},
    button: {backgroundColor: '#29CCB1', borderRadius: 32, marginVertical: 8},
  });
};
