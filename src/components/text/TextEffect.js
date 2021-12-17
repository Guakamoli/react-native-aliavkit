import React from 'react';

import {
    StyleSheet, View, Text, Button, TouchableOpacity, Pressable, Image, KeyboardAvoidingView, Keyboard, ScrollView
} from 'react-native'


import GestureText from './GestureText';


const TAG = "TextEffect"



/**
 * 文字设置
 */
const TextEffect = (props) => {

    const imageTextAlign = [
        require('../../../images/ic_text_align_left.png'),
        require('../../../images/ic_text_align_center.png'),
        require('../../../images/ic_text_align_right.png'),
    ];
    const imageTextEffect = [
        [require('../../../images/ic_text_color_unselect.png'), require('../../../images/ic_text_font_unselect.png')],
        [require('../../../images/ic_text_color_select.png'), require('../../../images/ic_text_font_unselect.png')],
        [require('../../../images/ic_text_color_unselect.png'), require('../../../images/ic_text_font_select.png')]
    ];
    const textAlignArrya = ['left', 'center', 'right']

    const textColorArray = ["#F9FAFB", "#F8AA99", "#FFC580", "#FFEA8A", "#BBE5B3", "#B6ECEB", "#B4E0FA", "#B3BCF5", "#E3D0FF", "#F4775C", "#FFA133", "#EDC200", "#50B83C", "#47C1BF", "#007ACE", "#5C6AC4", "#9C6ADE", "#637381", "#F15533", "#FF8A00", "#9C6F19", "#108043", "#00848E", "#084E8A", "#202E78", "#50248F", "#212B36", "#E55130", "#F28300", "#573B00", "#173630", "#003136", "#00152A", "#000639", "#230051"];


    console.log(TAG, "render：")

    //文字对齐方式 0、1、2  = left center  right
    const [textAlignPosition, setTextAlignPosition] = React.useState(2);
    const [textAlign, setTextAlign] = React.useState(textAlignArrya[textAlignPosition]);
    //0 默认的字体选择，1：文字颜色选择；2：文字背景选择
    const [textEffectPostion, setTextEffectPostion] = React.useState(0);

    //是否启用编辑，true：弹出输入框、禁用文字手势、文字移动到初始默认位置
    //点击完成设置成 false：收起输入框、启用文字手势、文字移动到设置的位置
    const [editable, setEditable] = React.useState(true);

    const [keyboard, setKeyboard] = React.useState(false);

    //TODO
    // React.useReducer
    const rer = React.useRef(null);

    React.useEffect(() => {
        console.log(TAG, "初始化:", props.route.params)
        Keyboard.addListener("keyboardDidShow", _keyboardDidShow);
        Keyboard.addListener("keyboardDidHide", _keyboardDidHide);
        return () => {
            console.log(TAG, "移除页面")
            Keyboard.removeListener("keyboardDidShow", _keyboardDidShow);
            Keyboard.removeListener("keyboardDidHide", _keyboardDidHide);
        };
    }, []);

    const _keyboardDidShow = () => {
        setKeyboard(true);
    };

    const _keyboardDidHide = () => {
        setKeyboard(false);
    };

    React.useEffect(() => {
        if (!rer.current) {
            return;
        }
        console.log(TAG, "加载完成")
    }, [rer]);


    const onTextAlign = () => {
        const position = (textAlignPosition + 1) % 3;
        setTextAlignPosition(position);
        setTextAlign(textAlignArrya[position]);
    }

    function onTextEffect(position) {
        setTextEffectPostion(textEffectPostion === position ? 0 : position);
    }

    /**
     * 完成文字添加
     */
    const onCompleteText = () => {
        setEditable(false)
    }

    /**
     * 设置文字颜色背景
     * textEffectPostion： 1：文字颜色选择；2：文字背景选择
     */
    function onTextColorEffcet(itemColor) {

    }

    const renderHead = () => (
        <View style={styles.headContainer}>
            <View style={styles.completeText}></View>
            <View style={styles.topToolsContainer}>
                <Pressable style={styles.imageToolsContainer} onPress={onTextAlign}>
                    <Image style={styles.imageTools} source={imageTextAlign[textAlignPosition]}></Image>
                </Pressable>
                <Pressable style={styles.imageToolsContainer} onPress={() => onTextEffect(1)}>
                    <Image style={styles.imageTools} source={imageTextEffect[textEffectPostion][0]}></Image>
                </Pressable>
                <Pressable style={styles.imageToolsContainer} onPress={() => onTextEffect(2)}>
                    <Image style={styles.imageTools} source={imageTextEffect[textEffectPostion][1]}></Image>
                </Pressable>
            </View>
            {
                editable ?
                    <TouchableOpacity onPress={onCompleteText}>
                        <Text style={styles.completeText}>完成</Text>
                    </TouchableOpacity> :
                    <View style={styles.completeText}></View>
            }
        </View>
    );

    const renderGestureText = () => (
        <GestureText
            textAlign={textAlign}
            editable={editable}
            onTextMove={(info) => {
                console.log(info);
            }}
            onEditEnable={(isEnable) => {
                //双击进入编辑
                setEditable(isEnable)
            }}
        >
        </GestureText>
    );

    const bottomEffectList = () => {
        return (
            <KeyboardAvoidingView behavior={Platform.OS == "ios" ? "position" : "position"} keyboardVerticalOffset={100}>
                <ScrollView horizontal={true} contentContainerStyle={styles.bottomEffectListContainer} showsHorizontalScrollIndicator={false}>
                    {textEffectPostion == 0 ?
                        textColorArray.map((itemColor) => {
                            return (
                                < View style={[styles.fontEffcetItemContainer, { backgroundColor: itemColor || 'white' }]} />
                            );
                        })
                        :
                        textColorArray.map((itemColor) => {
                            return (
                                <TouchableOpacity onPress={() => onTextColorEffcet(itemColor)}>
                                    <View style={[styles.colorEffcetItemContainer, { backgroundColor: itemColor || 'white' }]} />
                                </TouchableOpacity>
                            );
                        })}
                </ScrollView>
            </KeyboardAvoidingView >)
    }

    return (
        <View ref={rer} style={styles.container}>
            {renderHead()}
            <View style={{ width: props.width, height: props.height, backgroundColor: editable ? 'rgba(0, 0, 0, 0.58)' : 'transparent' }}>
                {renderGestureText()}
                {editable && bottomEffectList()}
            </View>

        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        flexDirection: 'column',
        position: 'absolute',
        top: 0,
        bottom: 0,
        left: 0,
        right: 0
    },
    headContainer: {
        height: 44,
        backgroundColor: '#000',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    completeText: {
        height: 44,
        width: 56,
        // backgroundColor:'white',
        textAlign: 'center',
        lineHeight: 44,
        color: '#836BFF',
        fontSize: 15,
    },
    topToolsContainer: {
        height: 44,
        flexDirection: 'row',
        alignItems: 'center',
    },
    imageToolsContainer: {
        width: 44,
        marginStart: 2,
        marginEnd: 2,
        height: 44,
        justifyContent: 'center',
        alignItems: 'center',
    },
    imageTools: {
        width: 24,
        height: 24,
    },

    bottomEffectListContainer: {
        alignItems: 'center',
        paddingRight: 12,
    },
    fontEffcetItemContainer: {
        width: 26,
        height: 26,
        marginStart: 1,
        borderWidth: 1,
        borderColor: 'white',
    },
    colorEffcetItemContainer: {
        width: 26,
        height: 26,
        marginStart: 11,
        borderRadius: 13,
        borderWidth: 1,
        borderColor: 'white',
    }
});


export default TextEffect;