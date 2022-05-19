import React from 'react';

import {
    StyleSheet,
    View,
    Text,
    TouchableOpacity,
    Dimensions,
} from 'react-native';

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';


import ImageCarousel from './ImageCarousel'
import PostMusic from './PostMusic'


const { width, height } = Dimensions.get('window');




export default class PostImageEditor extends React.Component {

    constructor(props) {
        super(props);
        this.refMarqueeText = React.createRef();
        this.state = {
            musicInfo: { name: '哈哈哈哈哈' }
        }
    }

    componentDidMount() {
    }

    componentWillUnmount() {
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (nextState.musicInfo !== this.state.musicInfo) {
            return true;
        }
        return false;
    }

    /**
     * 返回页面
     */
    _goBack = () => {
        this.props.setType('post');
    }

    /**
     * 上传图片
     */
    _onPostUploadFiles = () => {
        console.info("_onPostUploadFiles");
    }

    /**
     * 去选择音乐
     */
    _onSelectMusic = () => {
        this.setState({
            musicInfo: { name: '庐州月光光' }
        })
    }

    /**
     * 清除音乐
     */
    _onCleanMusic = () => {
        this.setState({
            musicInfo: { name: '' }
        })
    }


    HeadView = () => {
        return (<View style={styles.continueHeadView} >
            <TouchableOpacity onPress={this._goBack} style={styles.closeContinue} >
                <FastImage style={styles.closeIcon} source={require('../../images/backArrow.png')} resizeMode='contain' />
            </TouchableOpacity>


            <View style={styles.musicContinue}>
                <TouchableOpacity onPress={this._onSelectMusic} disabled={!!this.state.musicInfo?.name}>
                    <View style={styles.musicTextContinue}>
                        <FastImage style={styles.musicImg} source={require('../../images/ic_post_upload_music.png')} resizeMode='contain' />
                        <Text style={styles.musicText} ref={this.refMarqueeText} >
                            {this.state.musicInfo?.name || '选择音乐'}
                        </Text>
                    </View>
                </TouchableOpacity>

                {!!this.state.musicInfo?.name && <View style={{ height: '100%', width: 1, backgroundColor: '#fff' }} />}
                {!!this.state.musicInfo?.name &&
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
        </View>)
    }


    render() {
        return (
            <View style={styles.continueView}>
                <ImageCarousel {...this.props}></ImageCarousel>
       
                <PostMusic  {...this.props}></PostMusic>

                {this.HeadView()}
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
        backgroundColor:'#000'
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
        paddingHorizontal: 14,
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
        color: '#836BFF',
        lineHeight: 44,
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
        maxWidth: 66,
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