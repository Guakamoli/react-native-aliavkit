import React from 'react';

import { requireNativeComponent, NativeModules } from 'react-native';

const { RNAliKitPhotoViewModule } = NativeModules;
const NativePhotoView = requireNativeComponent('RNAliKitPhotoViewManager');


export default class AVkitPhotoView extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            pageSize: !!props?.pageSize ? props.pageSize : 40,
            numColumns: !!props?.numColumns ? props.numColumns : 4,
            multiSelect: !!props?.multiSelect,
            maxSelectCount: !!props?.maxSelectCount ? props.maxSelectCount : 10,
            defaultSelectedPosition: !!props?.defaultSelectedPosition ? props.defaultSelectedPosition : 0,

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
        if (nextProps.multiSelect !== this.props.multiSelect) {
            this.setMultiSelectAsync(nextProps.multiSelect);
        }
        return true;
    }

    componentWillUnmount() {

    }


    _onSelectedPhotoCallback = event => {
        // console.info("_onSelectedPhotos",event.nativeEvent);
        if (this.props.onSelectedPhotoCallback) {
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
            // const selectedIndex = event.nativeEvent.selectedIndex;
            // const selectedData = event.nativeEvent.data;
            this.props.onSelectedPhotoCallback(event.nativeEvent);
        }
    };

    /**
     * 多选图片超出最大选择
     * @param {} event 
     */
     _onMaxSelectCountCallback = event => {
        console.info("_onMaxSelectCountCallback 最多选择十张图片");
        if (this.props.onMaxSelectCountCallback) {
            this.props.onMaxSelectCountCallback();
        }
    }

    _onErrorCallback = event => {
        console.info("_onErrorCallback", event.nativeEvent);
        if (this.props.onErrorCallback) {
            // {
            //     code: errorCode
            //     message: errorMessage 
            // }
            this.props.onErrorCallback(event.nativeEvent);
        }
    }


    render() {
        // pageSize: 分页加载数量 默认40张
        // numColumns: 列数:默认4
        // multiSelect: 是否多选  默认：false
        // maxSelectCount: 多选最大数量，目前仅限图片

        // defaultSelectedPosition: 默认选中位置，默认0

        // itemWidth: 相册一张图片的宽度（为0则不设置，默认是：屏幕宽度 / 列数）。post用不到，后续 story 相册，UI宽高不一样会用到
        // itemHeight: 同itemWidth

        // onSelectedPhotoCallback: 每次选择图片/视频的回调
        // onMaximumSelectionCallback: 多选超过最大限制回调
        // onErrorCallback: 错误日志回调
        return (
            <NativePhotoView
                {...this.props}
                pageSize={this.state.pageSize}
                numColumns={this.state.numColumns}
                multiSelect={this.state.multiSelect}
                maxSelectCount={this.state.maxSelectCount}
                defaultSelectedPosition={this.state.defaultSelectedPosition}
                itemWidth={this.state.itemWidth}
                itemHeight={this.state.itemHeight}

                onSelectedPhotos={this._onSelectedPhotos}
                onSelectedPhotoCallback={this._onSelectedPhotoCallback}
                onMaxSelectCountCallback={this._onMaxSelectCountCallback}

                onErrorCallback={this._onErrorCallback}
            />
        );
    }

}