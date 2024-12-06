import { Image, ScrollView, StyleSheet, TouchableOpacity, View, ActivityIndicator, Alert, Linking, Platform, PermissionsAndroid } from 'react-native';
import React, { useCallback } from 'react';
import { Divider, Text } from 'react-native-paper';
import { Button } from 'react-native-paper';
import { kasperskyEasyScanner, updateDatabase, getPermission } from 'react-native-kav-easyscanner';

import { ScanType } from 'react-native-kav-easyscanner/src/interface';
import { images } from '../../assets';

import Ionicons from 'react-native-vector-icons/Ionicons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import AntDesign from 'react-native-vector-icons/AntDesign';
import { StackScreenProps } from '@react-navigation/stack';
import colors from '../../themes/colors/colors';

const AntivirusChecker: React.FC<StackScreenProps<any>> = ({ navigation }) => {
  const bottomSheetRef = React.useRef<any>(null);

  const styles = createStyles();
  const [isLoading, setIsLoading] = React.useState(false);
  const [isResult, setIsResult] = React.useState<any>([]);
  const [scanType, setScanType] = React.useState<ScanType>('Basic');
  const [error, setError] = React.useState<boolean>(false);

  const [updateStatus, setUpdateStatus] = React.useState<boolean>(false);

  React.useEffect(() => {
    setIsResult(undefined);
  }, [scanType]);

  const requestExternalStoragePermission = async () => {
    try {
      // const result = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.MANAGE_EXTERNAL_STORAGE);
      // console.log(result);
      // if (result === PermissionsAndroid.RESULTS.GRANTED) {
      //   console.log('Permission granted');
      // } else if (result === PermissionsAndroid.RESULTS.DENIED) {
      //   console.log('Permission denied');
      //   Alert.alert('Phân quyền bị từ chối', 'Hãy cho phép chúng tôi sử dụng quyền truy cập vào thiết bị nhớ / thư mục Tải về để kiểm tra');
      // } else if (result === PermissionsAndroid.RESULTS.BLOCKED || result === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) {
      //   console.log('Bạn đã chặn quyền này');
      //   Alert.alert('Bạn đã chặn quyền này', 'Để có thể tìm ra được mã độc trong máy, cần phải được cấp quyền cho phép truy cập vào thiết bị nhớ / thư mục Tải về để kiểm tra');
      // }
      await getPermission();
    } catch (err) {
      console.warn(err);
    }
  };

  /** This function update the database of the Scanner to scan properly */
  const onUpdateDatabase = async () => {
    try {
      const result = await updateDatabase();
      console.log(result);
      setUpdateStatus(result === 'Thành công' ? true : false);
      setIsLoading(false);
    } catch (error) {
      console.log(error);
      setError(true);
    }
  };

  const onPress = async () => {
    try {
      if (scanType === undefined) {
        setError(true);
        setIsLoading(false);
        return;
      }

      setError(false);
      setIsLoading(true);
      const result = await kasperskyEasyScanner(scanType);

      setIsResult(result);
      renderResult(result);
      setIsLoading(false);

      console.log(result);
    } catch (error) {
      setError(true);
    }
  };

  const renderResult = (result: any) => {
    if (isResult) Alert.alert('Kết quả', result);
  };

  const onSelectScanType = (value: string) => setScanType(value as ScanType);

  return (
    <ScrollView style={styles.container}>
      <View style={styles.navigation}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Ionicons name="arrow-back-circle" size={40} color="#00A88E" />
        </TouchableOpacity>
        <TouchableOpacity onPress={() => navigation.navigate('AboutAntivirus')}>
          <AntDesign name="questioncircle" size={25} />
        </TouchableOpacity>
      </View>

      <Image source={images.virus_scan} resizeMode="contain" style={{ height: 120, width: '100%' }} />
      <Text style={styles.title}>Diệt virus</Text>
      <Button onPress={onPress} style={{ backgroundColor: '#29CCB1' }}>
        <Text style={{ color: '#FFFFFF', fontSize: 15, fontWeight: 'bold' }}>Quét</Text>
      </Button>
      {!isLoading && !isResult ? (
        <Text style={{ textAlign: 'center', marginHorizontal: 32, marginVertical: 8 }}>Hãy chọn một trong những cách quét thiết bị dưới đây và bắt đầu</Text>
      ) : !isResult && isLoading ? (
        <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'center', marginVertical: 8 }}>
          <ActivityIndicator size="large" color="#00A88E" />
          <Text style={{ color: '#1D1D1B', lineHeight: 23, marginHorizontal: 8 }}>Đang quét...</Text>
        </View>
      ) : (
        <View>
          <Text>{isResult}</Text>
        </View>
      )}
      <View style={{ flexDirection: 'row', gap: 8, marginVertical: 8 }}>
        <TouchableOpacity style={styles.updateSelection} onPress={onUpdateDatabase}>
          <Text style={{ fontWeight: '700', fontSize: 18 }}>Cập nhật database</Text>
          <Text style={{ color: colors.dark.primary }}>Cập nhật ngay</Text>
          {updateStatus && (
            <View style={{ position: 'absolute', bottom: 0, right: 0 }}>
              <Ionicons name="checkmark-circle" color={colors.dark.secondary1} size={30} />
            </View>
          )}
        </TouchableOpacity>
        <TouchableOpacity style={styles.updateSelection} onPress={requestExternalStoragePermission}>
          <Text style={{ fontWeight: '700', fontSize: 18 }}>Quyền truy cập</Text>
          <Text style={{ color: colors.dark.primary }}>Cho phép quyền truy cập vào hệ thống</Text>
        </TouchableOpacity>
      </View>
      <View style={{ marginVertical: 8 }}>
        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
          <Text style={{ fontSize: 20, fontWeight: 'bold', flex: 1 }}>Các loại quét</Text>
          <Text style={{ fontSize: 15 }}>{scanType}</Text>
        </View>
        <Divider style={{ borderWidth: 0.25, opacity: 0.5, marginVertical: 8 }} />

        <TouchableOpacity onPress={() => onSelectScanType('Basic')} style={[styles.selection, { backgroundColor: scanType === 'Basic' ? '#EFFFFC' : undefined }]}>
          <Ionicons name="cloud" size={30} color="#00A88E" />
          <View style={styles.optionBox}>
            <Text style={{ flex: 1, fontSize: 20, fontWeight: 'bold' }}>Cơ bản</Text>
            <Text style={styles.optionSubtitle}>Đây là chức năng cơ bản nhất, dùng bộ scan từ hệt thống đám mây KSN của Kaspersky</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => onSelectScanType('Light')} style={[styles.selection, { backgroundColor: scanType === 'Light' ? '#EFFFFC' : undefined }]}>
          <MaterialCommunityIcons name="lightning-bolt-circle" size={30} color="#00A88E" />
          <View style={styles.optionBox}>
            <Text style={styles.optionTitle}>Quét nhanh</Text>
            <Text style={styles.optionSubtitle}>Đây là chức năng cơ bản được thêm một vài tính năng nâng cao dùng bộ đám mây KSN</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => onSelectScanType('LightPlus')} style={[styles.selection, { backgroundColor: scanType === 'LightPlus' ? '#EFFFFC' : undefined }]}>
          <Ionicons name="add-circle" size={30} color="#00A88E" />
          <View style={styles.optionBox}>
            <Text style={styles.optionTitle}>Quét nhanh (nâng cao)</Text>
            <Text style={styles.optionSubtitle}>Đây là chức năng cơ bản được thêm một vài tính năng nâng cao dùng bộ đám mây KSN</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => onSelectScanType('Recommended')}
          style={[
            styles.selection,
            {
              backgroundColor: scanType === 'Recommended' ? '#EFFFFC' : undefined,
            },
          ]}>
          <Ionicons name="thumbs-up-sharp" size={30} color="#00A88E" />
          <View style={styles.optionBox}>
            <Text style={styles.optionTitle}>Khuyên dùng</Text>
            <Text style={styles.optionSubtitle}>Chức năng được khuyên dùng nhất về độ đầy đủ và sự an toàn bảo mật</Text>
          </View>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => onSelectScanType('Full')} style={[styles.selection, { backgroundColor: scanType === 'Full' ? '#EFFFFC' : undefined }]}>
          <Ionicons name="shield-checkmark" size={30} color="#00A88E" />
          <View style={styles.optionBox}>
            <Text style={styles.optionTitle}>Toàn bộ</Text>
            <Text style={styles.optionSubtitle}>Bản đầy đủ các chức năng quét, bảo mật và kiểm tra từ hệ thống KSN</Text>
          </View>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
};

export default AntivirusChecker;

const createStyles = () => {
  return StyleSheet.create({
    navigation: { flexDirection: 'row', flex: 1, justifyContent: 'space-between', alignItems: 'center' },
    container: { marginTop: 24, padding: 16, paddingBottom: 16 },
    title: { marginVertical: 16, color: '#1D1D1B', fontWeight: '700', fontSize: 30, flex: 1, textAlign: 'center' },
    optionTitle: { flex: 1, fontSize: 18, fontWeight: 'bold' },
    optionSubtitle: { flex: 0.8, fontSize: 11, lineHeight: 20 },
    optionBox: { marginHorizontal: 16, flex: 1 },
    selection: { flexDirection: 'row', alignItems: 'center', borderRadius: 8, paddingLeft: 8 },
    sync: { backgroundColor: '#29CCB1', color: '#FFFFFF', position: 'absolute', right: 0, borderRadius: 8 },
    updateSelection: { backgroundColor: '#FFFFFF', flex: 1, padding: 8, borderRadius: 8 },
    contentContainer: { flex: 1, alignItems: 'center' },
  });
};
