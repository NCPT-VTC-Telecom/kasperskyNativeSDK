import {Animated, View, Text, useAnimatedValue} from 'react-native';
import React from 'react';

import {ActivityIndicator} from 'react-native-paper';
import type {ViewStyle} from 'react-native';

import type {PropsWithChildren} from 'react';

type FadeInViewProps = PropsWithChildren<{style: ViewStyle}>;

const Loading = () => {
  const fadeAnim = useAnimatedValue(0); // Initial value for opacity: 0

  React.useEffect(() => {
    Animated.timing(fadeAnim, {
      toValue: 1,
      duration: 200,
      useNativeDriver: true,
    }).start();
  }, [fadeAnim]);

  return (
    <Animated.View
      style={{
        opacity: fadeAnim,
        alignItems: 'center',
        justifyContent: 'center',
        flex: 1,
        gap: 16,
      }}>
      <ActivityIndicator size={50} />
      <Text style={{fontWeight: 500, fontSize: 15, color: '#000000'}}>
        Đang khởi tạo, vui lòng đợi
      </Text>
    </Animated.View>
  );
};

export default Loading;
