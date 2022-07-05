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

    useEffect(() => () => {

    }, []);

    const onCropError = (data) => {
        console.info("onCropError", data);
        setStartCrop(!startCrop)
    }

    const onCropped = (data) => {
        console.info("onCropped", data);
        setStartCrop(!startCrop)
        navigation.goBack()
        navigation.navigate("CropImagePreview", { imageUri: data.uri });
    }

    return (
        <View style={styles.cropContainer}>
            <StatusBar backgroundColor={"#000"} barStyle={'light-content'} animated />
            <View style={styles.continueHeadView}>
                <TouchableOpacity onPress={navigation.goBack}>
                    <Text style={styles.textClean}>取消</Text>
                </TouchableOpacity>

                <TouchableOpacity onPress={() => {
                    setImageAngle((imageAngle + 90) % 360);
                }}>
                    <Image style={styles.imageRotating} source={require('../../images/ic_rotating_image.png')} />
                </TouchableOpacity>
                <TouchableOpacity onPress={() => {
                    setStartCrop(true);
                }}>
                    <Text style={styles.textConfirm}>确认</Text>
                </TouchableOpacity>
            </View>

            <CropImageView
                style={{ width: width, height: height - 50, backgroundColor: 'black' }}
                angle={imageAngle}
                imageUri={imageUri}
                startCrop={startCrop}
                onCropped={onCropped}
                onCropError={onCropError}
            />
        </View>
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