
import React, { useState, useEffect } from 'react';
import {
    StyleSheet,
    View,
} from 'react-native';
import PropTypes from 'prop-types';
import Animated, { useSharedValue, useAnimatedStyle, Easing, withTiming, withDelay } from 'react-native-reanimated';


//useSharedValue不能存放在数组中
const AnimatedProgress = p => {
    console.log(p);
    const offset = useSharedValue(p.progress);
    const animatedStyles = useAnimatedStyle(() => {
        console.log(offset.value);
        return {
            width: (offset.value * 100) + '%',
            // 百分比计算会有误差
            // transform: [
            //     { translateX: (offset.value*100)+'%' },
            //     {scaleX: offset.value,}
            // ],
        };
    });
    offset.value = withDelay(p.animatedData.delay, withTiming(p.animatedData.toValue, p.animatedData))
    return (
        <Animated.View style={[p.next && styles.animated,
            animatedStyles
        ]}
        />
    )
}

//只对外进度条组件本身
const PhotoProgress = props => {
    const {
        itemCount = 3,          //总共有多少个进度块
        itemDuration = 2,       //单个进度的时间
        currentDuration = 0,    //当前进度时间
        playAnimaton = true,    //是否展示动画
        gapTime = 0,            //每个进度条之间的间隔时间
    } = props;
    const [progressData, setProgressData] = useState([]);

    useEffect(() => {
        //拼接一个数组用于渲染,元素起始时间大于当前时间时,显示为灰色进度条
        const progressData = Array.from({ length: itemCount }, (v, index) => {
            let start = index * itemDuration;
            let end = start + itemDuration;
            let next = end > currentDuration;
            let progress = Math.min(Math.max(0, (currentDuration - start) / itemDuration), 1);
            let key = 'progress_' + index;
            let animatedData = {};
            if (next && playAnimaton) {
                let delay = 0;
                if (start > currentDuration) {
                    delay = start - currentDuration;
                    delay += (index - Math.floor(currentDuration / itemDuration)) * gapTime;
                    delay *= 1000;
                }
                animatedData = {
                    toValue: 1,
                    duration: itemDuration * (1 - progress) * 1000,
                    easing: Easing.linear,
                    delay,
                };
            }
            return {
                key,
                next,
                start,
                end,
                progress,
                animatedData,
            }
        });
        setProgressData(progressData);
    }, [itemCount, itemDuration, currentDuration, playAnimaton, gapTime]);

    return (
        <View style={styles.rootView} >
            {progressData.map(p => {
                return <View style={[styles.progress, p.next && styles.blank]} key={p.key} >
                    <AnimatedProgress {...p} />
                </View>
            })}
        </View >
    )
}


PhotoProgress.propTypes = ({
    itemCount: PropTypes.number,
    itemDuration: PropTypes.number,
    currentDuration: PropTypes.number,
    playAnimaton: PropTypes.bool,
    gapTime: PropTypes.number,
});


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



export default PhotoProgress;