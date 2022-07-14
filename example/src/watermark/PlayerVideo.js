import React, { useEffect, useState, useRef } from 'react';



import {
    StyleSheet,
    View,
    Text,
    Image,
    TextInput,
    FlatList,
    StatusBar,
    Pressable,
    TouchableOpacity,
    Dimensions,
    Platform,
    Keyboard,
    SafeAreaView,
    DeviceEventEmitter
} from 'react-native';

const { width, height } = Dimensions.get('window');

import Video from 'react-native-video';


const PlayerVideo = (props) => {


    const videoUri = props.route.params?.videoUri;

    return (
        <SafeAreaView style={{ flex: 1, backgroundColor: '#000' }}>
            <View style={{ width: width, height: width * 16 / 9, overflow: 'hidden', borderRadius: 20 }}>
                <Video
                    source={{ uri: videoUri }}
                    resizeMode='contain'
                    repeat={true}
                    muted={false}
                    paused={false}
                    style={{ width: width, height: '100%' }}
                />
            </View>
        </SafeAreaView>
    )
}

export default PlayerVideo