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


const CropHeadPortrait = (props) => {

    const { navigation } = props;

    const uri = props.route.params?.imageUri;

    const [imageUri, setImageUri] = useState(uri);

    const [imageAngle, setImageAngle] = useState(0);

    const [startCrop, setStartCrop] = useState(false);

    const [reset, setReset] = useState(false);

    const cropViewRef = useRef(null);

    useEffect(() => {
        return () => {
        };
    }, []);

    const onCropError = (data) => {
        console.info("onCropError", data);
        setStartCrop(!startCrop)
    }

    const onCropped = (data) => {
        console.info("onCropped", data);
        setStartCrop(!startCrop)
        // navigation.goBack()
        navigation.navigate("CropImagePreview", { imageUri: data.uri });
    }

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
                    <TouchableOpacity onPress={navigation.goBack}>
                        <Text style={styles.textClean}>取消</Text>
                    </TouchableOpacity>

                    <TouchableOpacity onPress={() => {
                        setImageAngle((imageAngle + 90) % 360);
                        cropViewRef?.current?.rotateImage(false);
                    }}>
                        <Image style={styles.imageRotating} source={require('../../images/ic_rotating_image.png')} />
                    </TouchableOpacity>
                    {/* <TouchableOpacity onPress={() => {
                        onReset();
                    }}>
                        <Text style={styles.textConfirm}>重置</Text>
                    </TouchableOpacity> */}
                    <TouchableOpacity onPress={() => {
                        setStartCrop(true);
                        cropViewRef?.current?.saveImage(true, 90);
                    }}>
                        <Text style={styles.textConfirm}>确认</Text>
                    </TouchableOpacity>
                </View>


                {Platform.OS === 'android' ?
                    <CropImageView
                        style={{ width: width, height: height - 200, backgroundColor: 'black' }}
                        angle={imageAngle}
                        imageUri={imageUri}
                        startCrop={startCrop}
                        reset={reset}
                        onCropped={onCropped}
                        onCropError={onCropError}
                    />
                    :
                    <CropImageView
                        sourceUrl={imageUri}
                        style={{ width: width, height: height - 200, backgroundColor: 'black' }}
                        ref={cropViewRef}
                        onImageCrop={(res) => {
                            //{"height": 720, "target": 929, 
                            //"uri": "file:///var/mobile/Containers/Data/Application/C73778B0-B20F-4F62-9156-D7117DD002D4/Library/Caches/C053DD5A-88CE-4A26-A97E-086901000F57.jpg", "width": 720}
                            console.info("哈哈哈", res)
                            onCropped(res)
                        }}
                        aspectRatio={{ width: 1, height: 1 }}
                    />
                }
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

export default CropHeadPortrait