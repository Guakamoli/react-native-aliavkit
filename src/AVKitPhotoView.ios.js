import React from 'react';

import { requireNativeComponent, NativeModules } from 'react-native';

const { RNAlikitPhotoViewManager } = NativeModules;
const NativePhotoView = requireNativeComponent('RNAliKitPhotoView');


export default class AVkitPhotoView extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            pageSize: !!props?.pageSize ? props.pageSize : 40,
            numColumns: !!props?.numColumns ? props.numColumns : 4,
            multiSelect: !!props?.multiSelect,
            itemWidth: !!props?.itemWidth ? props.itemWidth : 0,
            itemHeight: !!props?.itemHeight ? props.itemHeight : 0,
        }
    };

    /**
     * 
     * @param {*} multiSelect 是否多选
     */
    setMultiSelectAsync = (multiSelect) => {
        this.setState({
            multiSelect
        })
    }

    componentDidMount() {

    }

    shouldComponentUpdate(nextProps, nextState) {
        return true;
    }

    componentWillUnmount() {

    }

    _onSelectedPhotos = event => {
        if (this.props.onSelectedPhotos) {
            // selectedIndex： 当前选中的 图片/视频 数组下标，单选模式固定返回0
            // selectedData：  选择的图片、视频的数组，单选模式其中只有一条数据，多选模式中视频也应该只有一条数据
            // data = [{
            //     index:下标：选择的图片/视频数组的顺序,
            //     width:该图片/视频的宽, 视频可能需要根据角度宽高对换
            //     height:该图片/视频的高,
            //     url:文件本地地址
            //     fileSize:文件大小（字节大小）,
            //     filename:文件名称,
            //     type: 文件类型： 格式为 "video/mp4" 或者  "image/jpeg",
            //     playableDuration: 视频时长,图片为0,视频为 ms
            //     rotation: 视频角度，通常手机拍摄的适配，宽高相反，需要根据角度重新设置宽高，（android 有这个问题）
            // }];
            const selectedIndex = event.nativeEvent.selectedIndex;
            const selectedData = event.nativeEvent.data;
            this.props.onSelectedPhotos(selectedIndex, selectedData);
        }
    };

    render() {

        // pageSize: 分页加载数量 默认40张
        // numColumns: 列数:默认4
        // multiSelect: 是否多选  默认：false
        // itemWidth: 相册一张图片的宽度（为0则不设置，默认是：屏幕宽度 / 列数）。post用不到，后续 story 相册，UI宽高不一样会用到
        // itemHeight: 同itemWidth
        return (
            <NativePhotoView
                style={{ minWidth: 100, minHeight: 100 }}
                {...this.props}
                pageSize={this.state.pageSize}
                numColumns={this.state.numColumns}
                multiSelect={this.state.multiSelect}
                itemWidth={this.state.itemWidth}
                itemHeight={this.state.itemHeight}

                onSelectedPhotos={this._onSelectedPhotos}
            />
        );
    }
}