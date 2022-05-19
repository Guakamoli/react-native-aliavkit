import React, { Component } from 'react';

import {
    StyleSheet,
    Text,
    View,
    TouchableOpacity,
} from 'react-native';

import I18n from '../i18n';

import FastImage from '@rocket.chat/react-native-fast-image';

export default class PostHead extends Component {

    constructor(props) {
        super(props);
    }

    shouldComponentUpdate(nextProps, nextState) {
        return false;
    }

    render() {
        return (
            <View style={styles.continueHeadView} >
                <TouchableOpacity onPress={this.props.goback} style={styles.closeContinue} >
                    <FastImage testID={"back-home"} style={styles.closeIcon} source={require('../../images/postClose.png')} resizeMode='contain' />
                </TouchableOpacity>

                <Text style={styles.textCenter}>{`${I18n.t('New_product')}`}</Text>

                <TouchableOpacity onPress={this.props.onPostUploadFiles} style={styles.nextContinue}>
                    <Text testID={'confirm-upload'} style={[styles.continueText]}>
                        {I18n.t('continue')}
                    </Text>
                </TouchableOpacity>
            </View>
        )
    }
}


const styles = StyleSheet.create({
    continueHeadView: {
        height: 50,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor:'#000'
    },
    closeContinue: {
        height: 44,
        width: 50,
        paddingHorizontal: 14,
        justifyContent: 'center',
    },
    closeIcon: {
        width: 16,
        height: 16,
    },

    textCenter: {
        fontSize: 17,
        fontWeight: '500',
        color: '#fff',
        lineHeight: 24,
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
})