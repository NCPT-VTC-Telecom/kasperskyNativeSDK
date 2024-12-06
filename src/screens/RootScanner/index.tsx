import { ActivityIndicator, Image, StyleSheet, TouchableOpacity, View } from 'react-native';
import React from 'react';
import { Button, Divider, Switch, Text } from 'react-native-paper';

import kasperskyRootCheck from 'react-native-root-module';

import Ionicons from 'react-native-vector-icons/Ionicons';
import { images } from '../../assets';

import { useAppNavigation } from '../../navigation/AppNavigation';

const CheckRoot: React.FC<any> = ({ root }) => {
  const [isLoading, setIsLoading] = React.useState(false);
  const [isRooted, setIsRooted] = React.useState(undefined);
  const [isEnabled, setIsEnabled] = React.useState(false);

  const navigation = useAppNavigation();

  const onPress = async () => {
    try {
      setIsLoading(true);
      const isRooted: any = await kasperskyRootCheck();
      setIsRooted(isRooted);
      setIsLoading(false);
    } catch (error) {
      console.error(error);
    }
  };

  const renderIcon = () => {
    if (isRooted === undefined) {
      return images.device_unknown;
    } else if (isRooted) {
      return images.device_unsecure;
    } else {
      return images.device_secure;
    }
  };

  return (
    <View style={styles.container}>
      <TouchableOpacity onPress={() => navigation.goBack()}>
        <Ionicons name="arrow-back-circle" size={40} color="#29CCB1" />
      </TouchableOpacity>
      <Image source={images.binary_code} resizeMode="contain" style={{ height: 200, width: '100%' }} />

      <Text style={{ color: '#1D1D1B', fontWeight: '700', fontSize: 30 }}>Kiểm tra quyền root</Text>
      <Text style={{ color: '#1D1D1B', lineHeight: 20, fontSize: 15, marginVertical: 8 }}>
        Các thiết bị nếu bị phá quyền root, cho phép các tài nguyên trong máy có quyền truy cập, thiết bị sẽ dễ dàng bị tấn công bởi tất cả các yếu tố bên ngoài, tiết lộ các tệp
        tin của hệ thống và làm cho thiết bị dễ bị nhiễm virus
      </Text>
      <Divider style={{ borderWidth: 0.25 }} />
      <View style={styles.headerTitle}>
        {isLoading ? <ActivityIndicator size={'large'} style={{ width: 100 }} /> : <Image source={renderIcon()} style={{ height: 100, width: 100 }} />}
        <View style={{ justifyContent: 'center', flex: 1 }}>
          {isRooted === undefined ? (
            <>
              <Text style={{ fontSize: 20, color: '#1D1D1B', fontWeight: '700' }}>Test thiết bị</Text>
              <Text style={{ color: '#1D1D1B' }}>
                Để có thể đảm bảo an toàn cho thiết bị của bạn, hãy để chúng tôi quét qua dữ liệu máy một lần để xác định thiết bị của bạn có bị can thiệp hay chưa?
              </Text>
            </>
          ) : isRooted === false ? (
            <View>
              <Text style={{ fontSize: 20, color: '#29CCB1', fontWeight: '700' }}>Chúc mừng</Text>
              <Text style={{ color: '#1D1D1B' }}>Thiết bị của bạn hiện tại không bị can thiệp, bạn có thể sử dụng thiết bị một cách an toàn.</Text>
            </View>
          ) : (
            <View>
              <Text style={{ fontSize: 20, color: '#FF335C', fontWeight: '700' }}>Cảnh báo</Text>
              <Text style={{ color: '#1D1D1B' }}>Thiết bị của bạn hiện tại đang bị can thiệp, hãy kiểm tra lại để đảm bảo an toàn cho thiết bị của bạn.</Text>
            </View>
          )}
        </View>
      </View>
      <Button onPress={onPress} style={{ backgroundColor: '#29CCB1', borderRadius: 8 }}>
        Nhấn để kiểm tra
      </Button>
    </View>
  );
};

export default React.memo(CheckRoot);

const styles = StyleSheet.create({
  container: { padding: 16 },
  headerTitle: { flexDirection: 'row', alignItems: 'center', marginVertical: 8 },
});
