import ImageMarker from "react-native-image-marker"



export default class TextMarker {


    constructor(src, text) {
        this.setState({
            loading: true
        })

        //阴影
        this.shadowStyle = {
            dx: 0,
            dy: 0,
            radius: 0,
            color: 'transparent',
        }

        //背景
        this.textBackgroundStyle = {
            type: 'stretchX',
            paddingX: 10,
            paddingY: 10,
            color: 'transparent'
        }

        //文本样式
        this.textStyle = {
            src: src,
            text: text,
            // if you set position you don't need to set X and Y
            X: 30,
            Y: 30,
            color: '#FF0000', // '#ff0000aa' '#f0aa'
            fontName: 'Arial-BoldItalicMT',
            fontSize: 44,
            scale: 1, 
            quality: 100,
            //topLeft,topRight,topCenter, center, bottomLeft, bottomCenter, bottomRight
            position:'center',
            // filename: '',
            saveFormat: 'png', 
            shadowStyle: this.shadowStyle,
            textBackgroundStyle: this.textBackgroundStyle,
         
        }



    }

}

// add text watermark to a photo


Marker.markText({
    src: img.uri,
    text: 'text marker',
    X: 30,
    Y: 30,
    color: '#FF0000', // '#ff0000aa' '#f0aa'
    fontName: 'Arial-BoldItalicMT',
    fontSize: 44,
    shadowStyle: {
        dx: 10.5,
        dy: 20.8,
        radius: 20.9,
        color: '#ff00ff' // '#ff00ffad'
    },
    textBackgroundStyle: {
        type: 'stretchX',
        paddingX: 10,
        paddingY: 10,
        color: '#0f0' // '#0f0a'
    },
    scale: 1,
    quality: 100

})