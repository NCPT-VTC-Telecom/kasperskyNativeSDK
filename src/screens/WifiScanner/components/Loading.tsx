import {View, Text} from 'react-native';
import React from 'react';

import {ActivityIndicator} from 'react-native-paper';

const Loading = () => {
  return (
    <View style={{alignItems: 'center', justifyContent: 'center', flex: 1}}>
      <ActivityIndicator />
      <Text>Đang khởi tạo, vui lòng đợi</Text>
    </View>
  );
};

export default Loading;
