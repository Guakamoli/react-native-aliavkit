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
    SafeAreaView
} from 'react-native';

import { HeaderBackButton } from '@react-navigation/elements';

import { CropImageView } from 'react-native-aliavkit';

const { width, height } = Dimensions.get('window');

const CropImagePreview = (props) => {

    const { navigation } = props;

    const imageUri = props.route.params?.imageUri;

    const [reset, setReset] = useState(false);

    useEffect(() => {
        return () => {
        };
    }, []);

    const onReset = () => {
        setReset(true)
        setTimeout(() => {
            setReset(false)
        }, 1000);
    }

    return (
        <SafeAreaView style={{ flex: 1 }}>
            <View style={styles.cropContainer}>
                <StatusBar backgroundColor={"#000"} barStyle={'light-content'} animated />
                <View style={styles.continueHeadView}>
                    <HeaderBackButton
                        label=''
                        tintColor='#FFFFFF'
                        onPress={navigation.goBack}
                        style={{ left: Platform.OS === 'android' ? 0 : 8 }}
                    />
                    <Text style={styles.textClean}>预览</Text>
                    <TouchableOpacity onPress={() => {
                        onReset();
                    }}>
                        <Text style={styles.textConfirm}>重置</Text>
                    </TouchableOpacity>
                </View>

                <CropImageView
                    style={{ width: width, height: height - 50, backgroundColor: 'black' }}
                    imageUri={imageUri}
                    reset={reset}
                />
            </View>
        </SafeAreaView>
    )

}
const styles = StyleSheet.create({
    continueHeadView: {
        width: width,
        height: 47,
        backgroundColor: '#000',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    textClean: {
        fontSize: 17,
        lineHeight: 47,
        color: '#fff',
        paddingHorizontal: 16,

    },
    textConfirm: {
        fontSize: 17,
        lineHeight: 47,
        fontWeight: '500',
        color: '#8EF902',
        paddingHorizontal: 16,
    },
    imageRotating: {
        width: 25,
        height: 22,
        margin: 10
    },
    cropContainer: {
        width: width,
        height: height,
    },
})

export default CropImagePreview