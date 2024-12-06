import { View, Text, Image, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';
import { ThemeProvider } from '@rneui/themed';
import React from 'react';
import { images } from '../assets';

import MaterialIcons from 'react-native-vector-icons/MaterialIcons';

import { StackScreenProps } from '@react-navigation/stack';
import LinearGradient from 'react-native-linear-gradient';
import colors from '../themes/colors/colors';
import { Button, Modal } from 'react-native-paper';

// import { IconButton } from 'react-native-paper';

export enum Type {
  information = 'information',
  monitor = 'monitor',
  antivirus = 'antivirus',
  root = 'root',
  filter = 'filter',
  wifi = 'wifi',
}

interface DataInterface {
  title: string;
  description: string;
  type: Type;
  onPress: () => void;
}

const Home: React.FC<StackScreenProps<any>> = ({ navigation }) => {
  const styles = createStyles();

  const DATA: DataInterface[] = [
    {
      title: 'Theo dõi',
      description: 'Theo dõi trạng thái thiết bị',
      type: Type.monitor,
      onPress: () => navigation.navigate('AppMonitor'),
    },
    {
      title: 'Quét virus',
      description: 'Tìm mã độc trong thiết bị',
      type: Type.antivirus,
      onPress: () => navigation.navigate('Antivirus'),
    },
    {
      title: 'Kiểm tra Root',
      description: 'Kiểm tra máy có bị can thiệp',
      type: Type.root,
      onPress: () => navigation.navigate('Root'),
    },
    {
      title: 'Lọc web',
      description: 'Lọc web độc hại khỏi thiết bị của bạn',
      type: Type.filter,
      onPress: () => navigation.navigate('Filter'),
    },
    {
      title: 'Quét Wifi',
      description: 'Sử dụng Wifi an toàn cùng với Kaspersky',
      type: Type.wifi,
      onPress: () => navigation.navigate('WifiScanner'),
    },
    {
      title: 'Thông tin',
      description: 'Thông tin về ứng dụng',
      type: Type.information,
      onPress: () => navigation.navigate('About'),
    },
  ];

  const renderIcon = (name: Type) => {
    switch (name) {
      case Type.information:
        return <MaterialIcons name="info" size={30} color="white" />;
      case Type.monitor:
        return <MaterialIcons name="monitor-heart" size={30} color="white" />;
      case Type.antivirus:
        return <MaterialIcons name="bug-report" size={30} color="white" />;
      case Type.root:
        return <MaterialIcons name="security" size={30} color="white" />;
      case Type.filter:
        return <MaterialIcons name="back-hand" size={30} color="white" />;
      case Type.wifi:
        return <MaterialIcons name="wifi" size={30} color="white" />;
    }
  };

  const testModal = () => {
    return (
      <Modal visible={true}>
        <View>
          <Text>Hello</Text>
        </View>
      </Modal>
    );
  };

  const renderItem = (title?: string, description?: string, type?: Type, onPress?: () => void) => {
    return (
      <TouchableOpacity onPress={onPress} style={styles.selection}>
        <View style={{ backgroundColor: colors.dark.primary, padding: 8, borderRadius: 32 }}>{renderIcon(type as Type)}</View>
        <View style={styles.info}>
          <Text style={styles.title}>{title}</Text>
          <Text style={{ fontSize: 12, color: 'black' }}>{description}</Text>
        </View>
        <MaterialIcons name="chevron-right" size={20} />
      </TouchableOpacity>
    );
  };

  return (
    <ScrollView style={{ backgroundColor: 'white' }} showsVerticalScrollIndicator={false}>
      <Image source={images.header} style={styles.title1} />
      <View>
        <Image source={images.shield} style={{ alignSelf: 'center', height: 110, width: 110, marginTop: 16 }} />
        <Text style={styles.textDevice}>Hãy bắt đầu</Text>
        <Text style={styles.headerTitle}>BẢO MẬT THIẾT BỊ CỦA BẠN</Text>
      </View>
      <View style={styles.wrapMenu}>
        <Text style={styles.quickAction}>Hành động</Text>
        <View style={{ alignItems: 'center', justifyContent: 'center', width: '100%', flex: 1 }}>
          {!!DATA && (
            <View style={{ flex: 1, width: '100%', paddingHorizontal: 8 }} id={'w'}>
              {DATA.map(item => renderItem(item.title, item.description, item.type, item.onPress))}
            </View>
          )}
        </View>
      </View>
      <Text style={styles.footer}>Kaspersky | VTC Telecom, 2024</Text>
    </ScrollView>
  );
};

export default Home;

const createStyles = () => {
  return StyleSheet.create({
    menu: { flexWrap: 'wrap', borderWidth: 1, width: 60, height: 60, justifyContent: 'center', gap: 8, padding: 4, marginVertical: 4, borderRadius: 8, alignItems: 'center' },
    title1: { color: '#1D1D1B', width: '80%', height: 80, alignItems: 'center', alignSelf: 'center' },
    wrapMenu: { width: '100%', flex: 1, paddingTop: 8 },
    textDevice: { fontSize: 20, alignSelf: 'center', color: 'black', fontWeight: '400', padding: 8, fontFamily: 'Montserrat Bold' },
    selection: {
      backgroundColor: 'white',
      marginTop: 8,
      marginHorizontal: 4,
      padding: 16,
      shadowColor: '#000000',
      elevation: 4,
      flexDirection: 'row',
      alignItems: 'center',
      shadowOffset: { width: 100, height: 100 },
      shadowOpacity: 1,
      borderRadius: 8,
    },
    title: { fontSize: 18, marginTop: 4, fontWeight: '800', color: 'black', fontFamily: 'Montserrat Bold' },
    quickAction: { paddingHorizontal: 16, paddingTop: 8, fontSize: 18, color: '#1D1D1B', fontWeight: '700' },
    headerTitle: { fontSize: 28, alignSelf: 'center', color: '#29CCB1', textAlign: 'center', fontWeight: '800', paddingBottom: 8, width: 250 },
    footer: { padding: 8, alignItems: 'flex-end', alignSelf: 'center', justifyContent: 'flex-end' },
    info: { flex: 1, marginLeft: 8 },
  });
};
