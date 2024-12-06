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

  const [state, setState] = React.useState<any>({ setScanUdsAllow: false, setSkipRiskwareAdWare: false, maxAppSize: 102, udsScan: false, skipRiskwareAdWare: false, result: '' });

  const [skipRiskwareAdWare, setSkipRiskwareAdWare] = React.useState<boolean>(false);
  const [scanUds, setScanUds] = React.useState<boolean>(true);

  React.useEffect(() => {
    DeviceEventEmitter.addListener('Result', event => {
      setState({ ...state, result: event });
    });
  }, []);

  const onFunction = async () => {
    try {
      await kasperskyAppMonitor();
    } catch (error) {
      console.error(error);
    }
  };

  const onEnableSkipRiskwareAdware = () => setSkipRiskwareAdWare(!skipRiskwareAdWare);

  return (
    <View style={{ padding: 8 }}>
      <TouchableOpacity onPress={() => navigation.goBack()}>
        <Ionicons name="arrow-back-circle" size={40} color="#29CCB1" />
      </TouchableOpacity>
      <Image source={images.heart_rate} resizeMode="contain" style={{ height: 200, width: '100%' }} />
      <View style={{ marginHorizontal: 8 }}>
        <Text style={styles.title}>Theo dõi ứng dụng</Text>
        <Text style={styles.description}>
          Để đảm bảo an toàn giữa các ứng dụng, bạn có thể sử dụng chức năng này để kiểm soát ứng dụng trên thiết bị của mình. Cho phép Kaspersky quét ứng dụng không an toàn, và
          theo dõi các ứng dụng có trong thiết bị của mình.
        </Text>
        <Divider style={{ borderWidth: 0.25, marginVertical: 8 }} />
        <Text style={styles.subtitle}>Tùy chỉnh</Text>
        <View
          style={{
            flexDirection: 'row',
            alignItems: 'center',
          }}>
          <View style={{ flex: 1 }}>
            <Text style={{ fontSize: 15, fontWeight: 'bold', color: '#1D1D1B' }}>Bỏ qua ứng dụng mã độc</Text>
            <Text style={{ fontSize: 12, color: 'red' }}>Lưu ý: "Việc bỏ qua ứng dụng mã độc sẽ không đảm bảo an toàn cho thiết bị của bạn trong quá trình sử dụng"</Text>
          </View>
          <Switch value={skipRiskwareAdWare} onChange={onEnableSkipRiskwareAdware} color={colors.dark.primary} />
        </View>
      </View>
      <Button onPress={onFunction} style={styles.button}>
        <Text style={{ color: 'white' }}>Nhấn để theo dõi</Text>
      </Button>
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
    button: { backgroundColor: colors.dark.primary, borderRadius: 8, marginVertical: 8 },
  });
};
