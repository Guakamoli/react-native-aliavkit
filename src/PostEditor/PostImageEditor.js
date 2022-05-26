import React from 'react';

import {
    StyleSheet,
    View,
    Text,
    TouchableOpacity,
    Dimensions,
    Image,
    Alert
} from 'react-native';

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';


import ImageCarousel from './ImageCarousel'
import PostMusic from './PostMusic'

import AVService from '../AVService';


const { width, height } = Dimensions.get('window');

export default class PostImageEditor extends React.Component {

    constructor(props) {
        super(props);
        this.refMarqueeText = React.createRef();
        this.state = {
            currentMusic: null,
            openMusicView: false,
            isPlay: true,
        }
    }

    setPlay = (isPlay) => {
        this.setState({ isPlay: isPlay });
    }

    setCurrentMusic = async (musicInfo) => {
        this.setState({ currentMusic: musicInfo });
    }

    componentDidMount() {
    }

    componentWillUnmount() {
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.currentMusic !== this.state.currentMusic) {
            return true;
        }
        if (nextState.openMusicView !== this.state.openMusicView) {
            return true;
        }
        if (nextState.isPlay !== this.state.isPlay) {
            return true;
        }
        return false;
    }

    /**
     * 返回页面
     */
    _goBack = () => {
        Alert.alert(
            I18n.t('post_image_editor_back_msg'),
            "",
            [
                {
                    text: `${I18n.t('Cancel')}`,
                    style: "default",
                },
                {
                    text: `${I18n.t('confirm')}`,
                    onPress: () => this.props.setType('post'),
                    style: "default",
                },
            ],
            {
                cancelable: true,
            }
        );
    }

    getFileName = (path) => {
        var pos1 = path.lastIndexOf('/');
        var pos2 = path.lastIndexOf('\\');
        var pos = Math.max(pos1, pos2)
        if (pos < 0)
            return path;
        else
            return path.substring(pos + 1);
    }

    /**
     * 上传图片
     */
    _onPostUploadFiles = async () => {

        let uploadData = this.props.uploadData.slice()

        for (var i = 0; i < uploadData.length; i++) {
            var item = uploadData[i];

            let path = item.path;
            let type = item.type;

            if (item.type !== 'image/jpg' && item.type !== 'image/png' && item.type !== 'image/jpeg') {
                //保存到沙盒
                path = await AVService.saveToSandBox(path);
                type = 'image/jpg'
            }

            if (!path.startsWith("file://")) {
                path = "file://" + path
            }

            item.path = path;
            item.localPath = path;
            item.type = type;
            item.name = this.getFileName(path);
        }

        // console.info("uploadData", uploadData);

        const musicInfo = this.state.currentMusic
        if (!!musicInfo) {
            const url = musicInfo.url;
            const filelaseIndex = url.lastIndexOf('.')
            let type = url.substr(filelaseIndex + 1)
            type = 'audio/' + type
            const audioInfo = {
                title: musicInfo.name,
                type: type,
                description: '',
                title_link: url,
                title_link_download: true,
                audio_url: url,
                audio_type: type,
                audio_size: 0
            }
            uploadData.push(audioInfo);
        }
        this._onCleanMusic();
        if (!!this.props.getUploadFile) {
            // console.info("_onPostUploadFiles", uploadData);
            this.props.getUploadFile(uploadData);
        }
    }

    /**
     * 去选择音乐
     */
    _onSelectMusic = () => {
        this.setState({
            openMusicView: true
        })
    }

    /**
     * 清除音乐
     */
    _onCleanMusic = () => {
        this.setState({
            currentMusic: null
        })
    }


    HeadView = () => {
        return (
            <View style={styles.continueHeadView} >
                <TouchableOpacity onPress={this._goBack} style={styles.closeContinue} >
                    <Image style={{ width: 30, height: 38, marginStart: 8 }} source={require('../../images/ic_post_editor_back.png')} resizeMode='contain' />
                </TouchableOpacity>


                <View style={styles.musicContinue}>
                    <TouchableOpacity onPress={this._onSelectMusic}>
                        <View style={styles.musicTextContinue}>
                            <FastImage style={styles.musicImg} source={require('../../images/ic_post_upload_music.png')} resizeMode='contain' />
                            <Text style={styles.musicText} ref={this.refMarqueeText} ellipsizeMode={'tail'} numberOfLines={1} >
                                {this.state.currentMusic?.name || I18n.t('selectMusic')}
                            </Text>
                        </View>
                    </TouchableOpacity>

                    {!!this.state.currentMusic?.name && <View style={{ height: '100%', width: StyleSheet.hairlineWidth, backgroundColor: '#999999' }} />}
                    {!!this.state.currentMusic?.name &&
                        <TouchableOpacity onPress={this._onCleanMusic} style={styles.closeMusicContinue}>
                            <FastImage style={styles.musicCloseImg} source={require('../../images/postClose.png')} resizeMode='contain' />
                        </TouchableOpacity>
                    }

                </View>


                <TouchableOpacity onPress={this._onPostUploadFiles} style={styles.nextContinue}>
                    <Text style={[styles.continueText]}>
                        {I18n.t('continue')}
                    </Text>
                </TouchableOpacity>
            </View>
        )
    }


    render() {

        const isPlayMusic = this.state.currentMusic?.name && this.state.isPlay;
        const imageW = isPlayMusic ? 180 : 148;
        const imageH = isPlayMusic ? 12 : 4;

        return (
            <View style={styles.continueView}>
                <ImageCarousel
                    {...this.props}
                    data={this.props.uploadData}
                    setPlay={this.setPlay}
                    openMusicView={this.state.openMusicView}
                />


                <Image
                    key={isPlayMusic ? 'gifImage' : 'pngImage'}
                    style={{
                        position: 'absolute',
                        bottom: isPlayMusic ? 10 : 14,
                        width: imageW,
                        height: imageH,
                        left: (width - imageW) / 2
                    }}
                    source={isPlayMusic ? require('../../images/ic_post_music_play.gif') : require('../../images/ic_post_music_stop.png')}
                />

                <PostMusic
                    {...this.props}
                    setCurrentMusic={this.setCurrentMusic}
                    currentMusic={this.state.currentMusic}
                    openMusicView={this.state.openMusicView}
                    isPlay={this.state.isPlay}
                    onCloseView={() => {
                        this.setState({ openMusicView: false });
                    }}
                />


                {!this.state.openMusicView && this.HeadView()}
            </View>
        )
    }

}




const styles = StyleSheet.create({
    continueView: {
        position: "absolute",
        left: 0,
        top: 0,
        right: 0,
        bottom: 0,
        backgroundColor: '#000'
    },

    continueHeadView: {
        height: 50,
        left: 0,
        top: 0,
        right: 0,
        position: "absolute",
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    closeContinue: {
        height: '100%',
        width: 50,
        justifyContent: 'center',
    },
    closeIcon: {
        width: 18,
        height: 18,
    },
    nextContinue: {
        height: 44,
        paddingHorizontal: 12,
        justifyContent: 'center',
        alignItems: 'flex-end',
    },

    continueText: {
        fontSize: 15,
        fontWeight: '600',
        color: '#FFF',
        lineHeight: 44,
        textShadowColor: 'rgba(0,0,0,0.5)',
        textShadowOffset: { width: 0, height: 0 },
        textShadowRadius: 4,
    },

    musicContinue: {
        width: "auto",
        height: 32,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        borderRadius: 32,
        flexDirection: 'row',
        alignItems: 'center',
        overflow: 'hidden',
    },
    musicTextContinue: {
        width: "auto",
        height: '100%',
        flexDirection: 'row',
        alignItems: 'center',
    },
    musicImg: {
        width: 12,
        height: 14,
        marginStart: 14,
    },
    musicInv: {
        width: 10,
        height: 14,
    },
    musicText: {
        height: '100%',
        maxWidth: 80,
        marginStart: 12,
        marginEnd: 12,
        lineHeight: 32,
        fontSize: 13,
        color: '#fff',
        textAlign: 'center',
    },
    closeMusicContinue: {
        flexDirection: 'row',
        alignItems: 'center',
        height: '100%',
        width: 38,
        justifyContent: 'center',
    },
    musicCloseImg: {
        width: 12,
        height: 12,
    },
})