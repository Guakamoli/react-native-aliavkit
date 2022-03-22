import React from 'react';

import { StyleSheet, View, Text, Button, } from 'react-native'


const TAG = "StoryPhoto"
const StoryPhoto = (props) => {


    const rer = React.useRef(null);

    React.useEffect(() => {
        console.log(TAG, "初始化:", props.route.params)
        return () => {
            console.log(TAG, "移除页面")
        };
    }, []);

    useFocusEffect(React.useCallback(() => {
        console.log(TAG, "focused 获取焦点")
        return () => {
            console.log(TAG, "unfocused 失去焦点")
        };
    }, []));

    React.useEffect(() => {
        if (!rer.current) {
            return;
        }
        console.log(TAG, "加载完成")
    }, [rer]);

    return (
        <View ref={rer} style={{ width: '100%', height: '100%' }}>

        </View>
    );
}

const styles = StyleSheet.create({
    container: {
    },
    btnContainer: {
        marginHorizontal: 40,
        marginVertical: 25,
    },
});

export default StoryPhoto