/* eslint-disable */
import React, { useEffect, useState } from 'react';
// TODO
import { StyleSheet, Text, TouchableOpacity, View, Dimensions, Animated, StatusBar, Platform } from 'react-native';
import CameraScreen from './CameraScreen';
import PostUpload from './PostScreen';
import { useThrottleFn } from 'ahooks';
import { useSelector, useDispatch } from 'react-redux';
import { setType } from './actions/container';
const { width, height } = Dimensions.get('window');

const Entry = (props) => {
  const { multipleBtnPng, startMultipleBtnPng, postCameraPng, changeSizePng } = props;
  const {
    cameraFlipPng,
    captureButtonPng,
    closePng,
    musicPng,
    beautifyPng,
    beautyAdjustPng,
    AaPng,
    filterPng,
    musicRevampPng,
    giveUpPng,
    noVolumePng,
    tailorPng,
    volumePng,
    musicDynamicGif,
    musicIconPng,
    musicIcongray,
    musicSearchPng,
    selectBeautifyPng,
    noResultPng,
    videomusicIconPng,
  } = props;

  const [bottomToolsVisibility, setBottomToolsVisibility] = useState(true);

  //story 是否初始化过，切换过，后续仅暂停拍摄器，不销毁
  const [initStory, setInitStory] = useState(false);


  const showBottomTools = () => {
    setBottomToolsVisibility(true);
  }

  const hideBottomTools = () => {
    setBottomToolsVisibility(false);
  }

  const { server, user, item, navigation, sendfile = () => { }, goBack = () => { }, haptics } = props;
  const dispatch = useDispatch();
  const type = useSelector((state) => {
    return state.shootContainer.type;
  });
  const changeFlagLock = React.useRef(false);
  const lockFlag = React.useRef(false);
  const transX = React.useRef(new Animated.Value(type === 'post' ? 30 : -30)).current;
  const types = [
    {
      type: 'post',
      name: '作品',
    },
    {
      type: 'story',
      name: '快拍',
    },
  ];


  const { run: changeType } = useThrottleFn(
    (i) => {
      changeFlagLock.current = true;
      Animated.timing(transX, {
        duration: 200,
        toValue: i.type === 'post' ? 30 : -30,
        useNativeDriver: true,
      }).start();
      dispatch(setType(i.type));
      setTimeout(() => {
        changeFlagLock.current = false;
      }, 0);
      if (type === 'story' && !initStory) {
        setInitStory(true)
      }
      // console.info("type:", type, "initStory:", initStory);
    },
    { wait: 0 },
  );
  // console.info("types111", type);

  const PostView = () => {
    return (
      <PostUpload
        initStory={initStory}
        // onRef={this.onRef}
        {...props}
        goback={goBack}
        goStory={() => {
          props.navigation.replace('FeedsStory');
        }}
        goPostEditor={(data) => {
          props.navigation.navigate('FeedsPostEditor', { ...data });
        }}
        type={type}
        setType={(type) => {
          dispatch(setType(type));
        }}
        multipleBtnImage={multipleBtnPng}
        startMultipleBtnImage={startMultipleBtnPng}
        postCameraImage={postCameraPng}
        changeSizeImage={changeSizePng}
        closePng={closePng}
        cameraModule={true}
        noVolumeImage={noVolumePng}
        volumeImage={volumePng}
      />
    )
  }


  let toolsInsetBottom = 20;
  const videoHeight = width * 16 / 9;
  const contentHeight = height - props.insets.top - props.insets.bottom

  const toolsHeight = 36

  let bottomSpaceHeight = 0;

  if (contentHeight > videoHeight) {
    bottomSpaceHeight = contentHeight - videoHeight
    if (bottomSpaceHeight > toolsHeight) {
      toolsInsetBottom = (bottomSpaceHeight - toolsHeight - (props.insets.bottom) / 2) / 2
      if (toolsInsetBottom < 0) toolsInsetBottom = 0
    }
  }
  const StoryView = () => {
    return (
      <CameraScreen
        initStory={initStory}
        bottomToolsVisibility={bottomToolsVisibility}
        showBottomTools={showBottomTools}
        hideBottomTools={hideBottomTools}
        toolsInsetBottom={toolsInsetBottom}
        bottomSpaceHeight={bottomSpaceHeight}
        actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
        // 退出操作
        {...props}
        goback={goBack}
        type={type}
        setType={(type) => {
          dispatch(setType(type));
        }}
        goPost={() => {
          navigation.replace('FeedsPost');
        }}
        // 拿到上传数据
        getUploadFile={(data) => {
          sendfile(data);
        }}
        haptics={haptics}
        cameraFlipImage={cameraFlipPng}
        captureButtonImage={captureButtonPng}
        closeImage={closePng}
        musicImage={musicPng}
        beautifyImage={beautifyPng}
        beautyAdjustImag={beautyAdjustPng}
        AaImage={AaPng}
        filterImage={filterPng}
        musicRevampImage={musicRevampPng}
        giveUpImage={giveUpPng}
        noVolumeImage={noVolumePng}
        tailorImage={tailorPng}
        volumeImage={volumePng}
        cameraModule={true}
        musicDynamicGif={musicDynamicGif}
        musicIconPng={musicIconPng}
        musicIcongray={musicIcongray}
        videomusicIcon={videomusicIconPng}
        musicSearch={musicSearchPng}
        selectBeautify={selectBeautifyPng}
        noResultPng={noResultPng}
        cameraModule={true}
      />
    )
  }


  return (
    <View style={{ width: "100%", height: "100%", backgroundColor: '#000' }}>
      {props?.isDrawerOpen || props.isExample && <StatusBar backgroundColor={"#000"} barStyle={'light-content'} animated />}

      <View style={{ display: (type === 'post' || type === 'edit') ? 'flex' : 'none', height: '100%', }}>
        {PostView()}
      </View>

      {initStory && (props.isDrawerOpen || props.isExample) ?
        <View style={{ display: (type === 'story' || type === 'storyedit') ? 'flex' : 'none', height: '100%' }}>
          {StoryView()}
        </View>
        :
        ((type === 'story' || type === 'storyedit') &&
          <View style={{ height: '100%', }}>
            {StoryView()}
          </View>
        )
      }

      {bottomToolsVisibility && (type === 'story' || type === 'post') &&
        <Animated.View
          style={[
            styles.tools,
            { bottom: toolsInsetBottom },
            {
              transform: [{ translateX: transX }],
            },
          ]}
        >
          {types.map((i) => {
            return (
              <TouchableOpacity
                style={{ width: '50%', height: '100%', justifyContent: 'center', alignItems: 'center' }}
                key={i.type}
                onPress={() => {
                  changeType(i);
                }}
              >
                <Text style={[styles.toolText, type !== i.type ? styles.curretnText : {}]}> {i.name}</Text>
              </TouchableOpacity>
            );
          })}
        </Animated.View>
      }
    </View>
  );
};

const styles = StyleSheet.create({
  tools: {
    backgroundColor: 'rgba(0, 0, 0, 1)',
    borderRadius: 22,
    zIndex: 100,
    width: 120,
    height: 36,
    position: 'absolute',
    left: (width - 120) / 2,
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  toolText: {
    color: 'white',
    fontSize: 14,
    fontWeight: '500',
    lineHeight: 18,
    // marginRight: 10,
  },
  curretnText: {
    color: 'rgba(126, 126, 126, 1)',
  },
});

export default Entry;
