// import { Button } from '@mui/material';
import React from 'react';
import { DeviceEventEmitter, Image, TouchableOpacity } from 'react-native';
import { StyleSheet, Text, View } from 'react-native';
import kasperskyAppMonitor from 'react-native-app-monitor';
import { Switch, Button, Divider } from 'react-native-paper';

import Ionicons from 'react-native-vector-icons/Ionicons';
import { images } from '../../assets';
import { useAppNavigation } from '../../navigation/AppNavigation';
import colors from '../../themes/colors/colors';

const AppMonitor: React.FC<any> = () => {
  const navigation = useAppNavigation();
  const styles = createStyles();

  const [state, setState] = React.useState<any>({ result: '' });
  const [loading, setLoading] = React.useState<boolean>(false);

  React.useEffect(() => {
    DeviceEventEmitter.addListener('Result', event => {
      setState({ ...state, result: event });
    });
  }, []);

  const onFunction = async () => {
    try {
      setLoading(true);
      await kasperskyAppMonitor();
      setLoading(false);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <View style={{ padding: 8 }}>
      <TouchableOpacity onPress={() => navigation.goBack()}>
        <Ionicons name="arrow-back-circle" size={40} color="#29CCB1" />
      </TouchableOpacity>
      <Image source={images.heart_rate} resizeMode="contain" style={{ height: 200, width: '100%' }} />
      <View style={{ marginHorizontal: 8 }}>
        <Text style={styles.title}>Theo dõi thiết bị</Text>
        <Text style={styles.description}>Theo dõi thiết bị, cho phép Kaspersky kiểm tra thiết bị có những ứng dụng bị ảnh hưởng mã độc</Text>
      </View>
      <View style={{ flexDirection: 'row', gap: 8, marginHorizontal: 8 }}>
        <Button onPress={onFunction} style={[styles.button, { backgroundColor: colors.dark.primary1 }]}>
          <Text style={{ color: 'white' }}>Cập nhật CSDL</Text>
        </Button>
        <Button onPress={onFunction} style={styles.button}>
          <Text style={{ color: 'white' }}>Nhấn để theo dõi</Text>
        </Button>
      </View>
      {loading ? (
        <View>
          <Text>Đang kiểm tra vui lòng đợi</Text>
        </View>
      ) : (
        <View>
          <Text>Đã kiểm tra!</Text>
        </View>
      )}
    </View>
  );
};

export default AppMonitor;

const createStyles = () => {
  return StyleSheet.create({
    title: { color: '#1D1D1B', fontWeight: '700', fontSize: 30, textAlign: 'center' },
    description: { color: '#1D1D1B', lineHeight: 20, fontSize: 13, marginVertical: 8, textAlign: 'center' },
    subtitle: { fontSize: 25, fontWeight: '800', marginBottom: 8, color: '#1D1D1B' },
    view: { flexDirection: 'row', alignItems: 'center' },
    button: { backgroundColor: colors.dark.primary, borderRadius: 8, marginVertical: 8, flex: 1 },
  });
};
