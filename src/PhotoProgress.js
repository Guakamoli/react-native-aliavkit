
import React, { Component, useRef, useEffect } from 'react';
import {
    StyleSheet,
    View,
    Animated,
    Easing,
} from 'react-native';
import PropTypes from 'prop-types';

export default class PhotoProgress extends Component {
    static propTypes = ({
        itemCount: PropTypes.number,
        itemDuration: PropTypes.number,
        currentDuration: PropTypes.number,
        playAnimaton: PropTypes.bool,
    });

    render() {
        const {
            itemCount = 3,
            itemDuration = 3,
            currentDuration = 4,
            playAnimaton = true
        } = this.props;
        //拼接一个数组用于渲染,元素起始时间大于当前时间时,显示为灰色进度条
        const progressData = Array.from({ length: itemCount }, (v, index) => {
            let start = index * itemDuration;
            let end = start + itemDuration;
            let next = end > currentDuration;
            let progress = Math.max(0, (currentDuration - start) / itemDuration);
            let width = new Animated.Value(progress);
            if (next && playAnimaton) {
                let animatedData = {
                    toValue: 1,
                    duration: itemDuration * (1 - progress) * 1000,
                    easing: Easing.easeInOut,
                    delay: Math.max(0, start - currentDuration) * 1000,
                    useNativeDriver: false,
                };
                Animated.timing(width, animatedData).start();
            }
            return {
                key: 'progress_' + index,
                next,
                width,
            }
        });
        return (
            <View style={styles.rootView} >
                {progressData.map(p => {
                    return <View style={[styles.progress, p.next && styles.blank]} key={p.key} >
                        <Animated.View style={[p.next && styles.animated, {
                            width: p.width.interpolate({
                                inputRange: [0, 1],
                                outputRange: ['0%', '100%'],
                            })
                        }]} />
                    </View>
                })}
            </View >
        )
    }
}



const styles = StyleSheet.create({
    rootView: {
        paddingLeft: 10,
        paddingRight: 5,
        justifyContent: 'space-between',
        alignItems: 'center',
        flexDirection: 'row',
        flexWrap: 'nowrap',
        height: 3,
    },
    progress: {
        marginRight: 5,
        flex: 1,
        color: '#f00',
        backgroundColor: "#fff",
        borderRadius: 1.5,
        height: 3,
        overflow: 'hidden',
    },
    blank: {
        backgroundColor: 'rgba(255, 255, 255, 0.4)'
    },
    animated: {
        backgroundColor: "#fff",
        flex: 1,
    },
})


