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
        <View style={{ flex: 1 }}>

            <Video
                source={{ uri: videoUri }}
                resizeMode='cover'
                repeat={true}
                muted={false}
                paused={false}
                style={{ width: width, height: '100%' }}
            />

        </View>
    )
}

export default PlayerVideo