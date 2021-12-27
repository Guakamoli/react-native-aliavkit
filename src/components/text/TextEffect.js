import React from 'react';

import {
    StyleSheet, View, Text, Button, TouchableOpacity, Pressable, Image, KeyboardAvoidingView, Keyboard, ScrollView, Platform,
} from 'react-native'


import GestureText from './GestureText';

import AVService from '../../AVService';

import AddTextMarker from './AddTextMarker';
import CameraRoll from '@react-native-community/cameraroll';
import {
    Grayscale,
    SrcOverComposition,
    TextImage,
} from 'react-native-image-filter-kit';


import * as Progress from 'react-native-progress';

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

    // const textColorArray = ["#F9FAFB", "#F8AA99", "#FFC580", "#FFEA8A", "#BBE5B3", "#B6ECEB", "#B4E0FA", "#B3BCF5", "#E3D0FF", "#F4775C", "#FFA133", "#EDC200", "#50B83C", "#47C1BF", "#007ACE", "#5C6AC4", "#9C6ADE", "#637381", "#F15533", "#FF8A00", "#9C6F19", "#108043", "#00848E", "#084E8A", "#202E78", "#50248F", "#212B36", "#E55130", "#F28300", "#573B00", "#173630", "#003136", "#00152A", "#000639", "#230051"];
    const textColorArray = ["#FEFFFE", "#000000", "#EA4040", "#FF933D", "#FFC840", "#FFFCDA", "#78C25E", "#78C8A6", "#3596F0", "#2444B3", "#5756D4", "#F7D7E9", "#A4895B", "#32523C", "#2F698D", "#92979E", "#333333"];


    const [captionInfo, setCaptionInfo] = React.useState(null);

    //文字对齐方式 0、1、2  = left center  right
    const [textAlignPosition, setTextAlignPosition] = React.useState(2);
    const [textAlign, setTextAlign] = React.useState(textAlignArrya[textAlignPosition]);
    //0 默认的字体选择，1：文字颜色选择；2：文字背景选择
    const [textEffectPostion, setTextEffectPostion] = React.useState(0);

    //是否启用编辑，true：弹出输入框、禁用文字手势、文字移动到初始默认位置
    //点击完成设置成 false：收起输入框、启用文字手势、文字移动到设置的位置
    const [editable, setEditable] = React.useState(true);

    const [textFontPostion, setTextFontPostion] = React.useState(0);
    const [textColor, setTextColor] = React.useState("white");
    const [textBackgroundColor, setTextBackgroundColor] = React.useState("transparent");

    const [textFontList, setTextFontList] = React.useState([]);
    const [textFontName, setTextFontName] = React.useState("");

    const [textFontProgress, setTextFontProgress] = React.useState([]);

    const [keyboard, setKeyboard] = React.useState(false);

    const [lastTextEdit, setLastTextEdit] = React.useState(false);

    const [continueEdit, setContinueEdit] = React.useState(false);



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
        onGetFontList()
    }, [rer]);


    //记录上一次的事件 false：滤镜  true：文字
    React.useEffect(() => {
        if (!lastTextEdit && !!props.isTextEdit) {
            setEditable(true)
        }
        setLastTextEdit(props.isTextEdit)
    }, [props.isTextEdit]);

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

    //获取字体列表
    const onGetFontList = async () => {
        const fontList = await AVService.fetchFontList();
        const systemFont = { name: "系统", fontName: "PingFangSC-Medium", isDbContain: true };

        fontList.unshift(systemFont);
        setTextFontList(fontList);

        setTextFontProgress(Array.from({ length: fontList.length }));

        console.log("textFontProgress", textFontProgress);

        fontList.forEach(async (item, index) => {
            if (index > 0 && item.isDbContain) {
                const fontInfo = await onDownlaodFont(item, index);
                fontList[index] = fontInfo;
                setTextFontList(fontListCopy);
            }
        });
    }

    //下载字体
    const onDownlaodFont = async (fontItem, index) => {
        let listener = null;
        if (index > 0 && !fontItem.isDbContain) {
            listener = await AVService.downloadFontProgress((progress) => {
                console.log("downloadFontProgress", progress);
                const textFontProgressCopy = JSON.parse(JSON.stringify(textFontProgress))
                textFontProgressCopy[index] = progress;
                setTextFontProgress(textFontProgressCopy);
            });
        }
        const fontInfo = await AVService.downloadFont(fontItem);
        listener?.remove();
        const textFontProgressCopy = JSON.parse(JSON.stringify(textFontProgress))
        textFontProgressCopy[index] = null;
        setTextFontProgress(textFontProgressCopy);
        return fontInfo
    }

    /**
     * 字体选择
     */
    async function onTextFontEffcet(item, index) {
        setTextFontPostion(index);
        if (Platform.OS === 'ios') {
            if (!!item.fontName) {
                setTextFontName(item.fontName);
            } else {
                const fontInfo = await onDownlaodFont(item, index);
                setTextFontName(!!fontInfo.fontName ? fontInfo.fontName : null);
                const textFontListCopy = JSON.parse(JSON.stringify(textFontList))
                textFontListCopy[index] = fontInfo;
                setTextFontList(textFontListCopy);
            }
            return
        }
        if (!!item.isDbContain) {
            // console.log("fontName",item.fontName);
            setTextFontName(!!item.fontName ? item.fontName : null);
        } else {
            const fontInfo = await onDownlaodFont(item, index);
            setTextFontName(!!fontInfo.fontName ? fontInfo.fontName : null);
            console.log("downloadFont", fontInfo, index);
            const textFontListCopy = JSON.parse(JSON.stringify(textFontList))
            textFontListCopy[index] = fontInfo;
            setTextFontList(textFontListCopy);
        }
    }

    /**
     * 设置文字颜色背景
     * textEffectPostion： 1：文字颜色选择；2：文字背景选择
     */
    function onTextColorEffcet(itemColor) {
        if (textEffectPostion === 1) {
            setTextColor(itemColor);
            if (textBackgroundColor == itemColor) {
                setTextBackgroundColor("transparent");
            }
        } else if (textEffectPostion === 2) {
            setTextBackgroundColor(itemColor);
            if (textColor == itemColor) {
                setTextColor("white");
            }
        }
    }

    /**
     * 清除文字、文字背景 颜色
     */
    const onCleanColor = () => {
        if (textEffectPostion === 1) {
            setTextColor("white");
        } else if (textEffectPostion === 2) {
            setTextBackgroundColor("transparent");
        }
    }

    /**
     * 继续  () => props.continueEdit()
     */
    const onContinueEdit = () => {
        setContinueEdit(true);
    }

    const dstImageView = () => {
        return (
            <Image
                style={{
                    width: props.width,
                    height: props.height,
                }}
                source={{ uri: props.photoFile }}
            />
        )
    }

    const getTextComponent = (dst, textStyle) => {
        const srcImage = (
            <TextImage
                text={textStyle.text}
                fontName={textStyle.fontName}
                fontSize={textStyle.fontSize * textStyle.scale}
                color={textStyle.color}
                textAlign={textStyle.textAlign}
                backgroundColor={textStyle.backgroundColor}
            />
        )
        const tx = 0.5 + textStyle.x / props.width;
        const ty = 0.5 - textStyle.y / props.height;

        console.log(props.width, props.height);

        return <SrcOverComposition
            resizeCanvasTo={'dstImage'}
            dstImage={dst}
            srcTransform={{
                //srcImage 中心点在屏幕内的位置 0.5 0.5 为屏幕中间
                translate: { x: tx, y: ty },
                scale: 'COVER',
                rotate: textStyle.rotate
            }}
            srcImage={srcImage}
        />
    }

    const TextComponent = () => {
        var dstView = dstImageView();
        dstView = getTextComponent(dstView, captionInfo);
        return dstView;
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
                    <View style={styles.completeText}></View>}
        </View>
    );

    const renderHead2 = () => (
        <View style={styles.headContainer}>
            <TouchableOpacity style={{ height: 30, width: 40, paddingHorizontal: 12, justifyContent: 'center', }} onPress={() => props.goback()}>
                <Image source={require('../../../images/backArrow.png')} resizeMode='contain' />
            </TouchableOpacity>
            <TouchableOpacity onPress={onContinueEdit}>
                <Text style={styles.completeText}>继续</Text>
            </TouchableOpacity>
        </View>
    );

    const renderGestureText = () => (
        <GestureText
            isTextEdit={props.isTextEdit}
            lastTextEdit={lastTextEdit}
            width={props.width}
            height={props.height}
            textAlign={textAlign}
            textColor={textColor}
            textBackgroundColor={textBackgroundColor}
            textFontName={textFontName}
            editable={editable}
            onTextMove={(info) => {
                console.log("onTextMove", info);
                setCaptionInfo(info)
            }}
            onCompleteText={onCompleteText}
            onEditEnable={(isEnable) => {
                //双击进入编辑
                setEditable(isEnable)
            }}
        >
        </GestureText>
    );

    const bottomEffectList = () => {

        return (
            <View style={{ position: 'absolute', bottom: 20 }}>
                <KeyboardAvoidingView behavior={Platform.OS == "ios" ? "position" : "position"} keyboardVerticalOffset={100} style={{ marginEnd: textEffectPostion !== 0 ? 48 : 0 }}>
                    <ScrollView horizontal={true} contentContainerStyle={styles.bottomEffectListContainer} showsHorizontalScrollIndicator={false} keyboardShouldPersistTaps={'handled'}>
                        {textEffectPostion == 0 ?
                            textFontList.map((item, index) => {
                                return (
                                    <TouchableOpacity onPress={() => onTextFontEffcet(item, index)} key={index}>
                                        <View style={[styles.fontEffcetItemContainer, {
                                            // borderWidth: textFontPostion === index ? 1 : 1, 
                                            borderColor: textFontPostion === index ? 'white' : 'rgba(255,255,255,0.3)'
                                        }]}>
                                            <Text style={[styles.textFontName, { fontWeight: textFontPostion === index ? '500' : '400', fontFamily: item.fontName }]}>{item.name}</Text>
                                            {!item.isDbContain &&
                                                <View style={[styles.textFontDownload]}>
                                                    {textFontProgress[index] ?
                                                        <Progress.Circle style={[styles.textFontDownload]} size={12} progress={textFontProgress[index]} color={"rgba(255, 255, 255, 1)"}/>
                                                        :
                                                        <Image style={[styles.textFontDownload]} source={require('../../../images/ic_text_font_download.png')}></Image>
                                                    }
                                                </View>
                                            }
                                        </View>
                                    </TouchableOpacity>
                                );
                            })
                            :
                            textColorArray.map((itemColor, index) => {
                                return (
                                    <TouchableOpacity onPress={() => onTextColorEffcet(itemColor)} key={index}>
                                        <View style={[styles.colorEffcetItemContainer, {
                                            backgroundColor: itemColor || 'white',
                                            borderWidth: ((textEffectPostion === 1 && textColor === itemColor) || (textEffectPostion === 2 && textBackgroundColor === itemColor)) ? 3 : 1
                                        }]} />
                                    </TouchableOpacity>
                                );
                            })}
                    </ScrollView>
                </KeyboardAvoidingView >
                {textEffectPostion !== 0 &&
                    <TouchableOpacity onPress={onCleanColor}>
                        <View style={styles.cleanTextColorContainer}>
                            <Image style={styles.cleanTextColor} source={require('../../../images/ic_text_color_cancel.png')} />
                        </View>
                    </TouchableOpacity>}
            </View>)
    }
    return (
        <View ref={rer} style={styles.container}>
            {(props.isTextEdit && editable) ? renderHead() : renderHead2()}
            {continueEdit ?
                <View style={{ width: props.width, height: props.height }}>
                    <Grayscale
                        image={TextComponent()}
                        amount={0}
                        onExtractImage={({ nativeEvent }) => {
                            console.log("save phont", nativeEvent.uri);
                            CameraRoll.save(nativeEvent.uri, { type: 'photo' })
                            props.onContinueEdit(nativeEvent.uri)
                        }}
                        extractImageEnabled={true}
                    />
                </View> :
                <View style={{ width: props.width, height: props.height, backgroundColor: props.isTextEdit && editable ? 'rgba(0, 0, 0, 0.58)' : 'transparent' }}>
                    {(props.isTextEdit || captionInfo != null) && renderGestureText()}
                    {(props.isTextEdit && editable) && bottomEffectList()}
                </View>
            }
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
        width: '100%',
        backgroundColor: '#000',
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        zIndex: 1000,
    },
    completeText: {
        height: 44,
        width: 56,
        justifyContent: 'center',
        textAlign: 'center',
        alignItems: 'center',
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
        width: 26,
        height: 26,
    },

    bottomEffectListContainer: {
        alignItems: 'center',
        paddingRight: 12,
        paddingLeft: 5,
    },
    fontEffcetItemContainer: {
        position: 'relative',
        height: 26,
        borderRadius: 4,
        marginStart: 1,
        marginStart: 9,
        borderWidth: 1,
        justifyContent: 'center',
        borderColor: 'white',
    },
    textFontName: {
        fontSize: 14,
        color: '#FFFFFF',
        paddingStart: 15,
        paddingEnd: 15,
    },
    textFontDownload: {
        position: 'absolute',
        bottom: 0,
        right: 0,
        width: 12,
        height: 12,
    },
    colorEffcetItemContainer: {
        width: 26,
        height: 26,
        marginStart: 11,
        borderRadius: 13,
        borderWidth: 1,
        borderColor: 'white',
    },
    // cleanTextColorContainer: {
    //     width: 48,
    //     height: 58,
    //     bottom: -20,
    //     position: 'absolute',
    //     right: 0,
    //     justifyContent:'center',
    // },
    cleanTextColor: {
        width: 28,
        height: 28,
        position: 'absolute',
        bottom: -1,
        right: 13,
    },

});


export default TextEffect;