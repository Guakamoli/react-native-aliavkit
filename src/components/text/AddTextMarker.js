import {
    Platform, Dimensions, Image,
} from 'react-native';

import {
    Grayscale,
    SrcOverComposition,
    TextImage,
} from 'react-native-image-filter-kit';

import CameraRoll from '@react-native-community/cameraroll';

export default class AddTextMarker {


    constructor(width, height) {

        this.width = width || Dimensions.get('screen').width;
        this.height = height || Dimensions.get('screen').width;

        //文本样式
        this.textStyles = [
            // {
            //     text: "呵呵呵呵呵呵\n哈哈哈",
            //     textAlign: "right",
            //     color: "#FFFF00",
            //     backgroundColor: "#0000ff",
            //     fontSize: 20,
            //     fontName: "sd",
            //     x: -50,
            //     y: 50,
            //     scale: 2,
            //     rotate: 0,
            // },
            // {
            //     text: "垂直垂直垂直\n竖直竖直",
            //     textAlign: "right",
            //     color: "#FF00FF",
            //     backgroundColor: "#0000ff",
            //     fontSize: 40,
            //     fontName: "sd",
            //     x: 0,
            //     y: 0,
            //     scale: 1,
            //     rotate: 1.57,
            // },
            // {
            //     text: "反斜反斜反斜反斜\n反斜反斜",
            //     textAlign: "right",
            //     color: "#6600FF",
            //     backgroundColor: "#0000ff",
            //     fontSize: 30,
            //     fontName: "sd",
            //     x: 100,
            //     y: 100,
            //     scale: 1.0,
            //     rotate: 0.8,
            // },
            // {
            //     text: "斜的斜的斜的斜的\n斜的斜的斜的",
            //     textAlign: "right",
            //     color: "#FF6600",
            //     backgroundColor: "#0000ff",
            //     fontSize: 30,
            //     fontName: "sd",
            //     x: 100,
            //     y: -100,
            //     scale: 1.0,
            //     rotate: -0.8,
            // }
        ]
    }


    getTextComponent = (dst, textStyle) => {
        const srcImage = (
            <TextImage
                text={textStyle.text}
                fontName={textStyle.fontName}
                fontSize={textStyle.fontSize * textStyle.scale}
                color={textStyle.color} />
        )
        const tx = textStyle.x / width + 0.5;
        const ty = textStyle.y / width + 0.5;
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
            extractImageEnabled={true}
        // onExtractImage={({ nativeEvent }) => {
        //     console.log("save phont", nativeEvent.uri);
        //     CameraRoll.save(nativeEvent.uri, { type: 'photo' })
        // }}
        />
    }

    ImageComponent() {
        return
    }

    TextComponent() {
        console.log(this.width, this.height, this.imageUri);
        var dstView = <Image
            style={{
                width: this.width,
                height: this.height,
            }}
            source={{ uri: this.imageUri }}
        />
        this.textStyles.forEach((item, index) => {
            dstView = this.getTextComponent(dstView, item);
        });
        return dstView;
    }

    composePhoto(imageUri, textStyles) {
        this.imageUri = imageUri || "";
        this.textStyles = textStyles || [];


        this.TextComponent();

        <Grayscale
            amount={0}
            onExtractImage={({ nativeEvent }) => {
                console.log("save phont", nativeEvent.uri);
                CameraRoll.save(nativeEvent.uri, { type: 'photo' })
                // setPhotoFile(nativeEvent.uri);
            }}
            extractImageEnabled={true}
            image={this.TextComponent()}
        ></Grayscale>
        this.textStyles = textStyles || [];
    }

}