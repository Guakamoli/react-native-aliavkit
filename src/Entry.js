/* eslint-disable */

import React, { useEffect, useState } from 'react';
// TODO
import { StyleSheet, Text, TouchableOpacity, View, Dimensions, Animated, StatusBar, Platform } from 'react-native';
import CameraScreen from './CameraScreen';
import PostUpload from './PostScreen';
import { useThrottleFn } from 'ahooks';
import { useSelector, useDispatch } from 'react-redux';
import { setType } from './actions/container';
import I18n from './i18n';
const { width, height } = Dimensions.get('window');
const StoryView = React.memo((props) => {
  const dispatch = useDispatch();
  const { type, initStory } = props;
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
    bottomToolsVisibility,
    bottomToolsVisibilityType,
    showBottomTools,
    hideBottomTools,
  } = props;
  const { server, user, item, navigation, sendfile = () => { }, goBack = () => { }, haptics } = props;
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
  console.info('initStoryinitStoryinitStory', initStory)
  return (
    <CameraScreen
      initStory={initStory}
      bottomToolsVisibility={bottomToolsVisibility}
      bottomToolsVisibilityType={bottomToolsVisibilityType}
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
    />
  )
})
const PostView = React.memo((props) => {
  const dispatch = useDispatch();
  const { multipleBtnPng, startMultipleBtnPng, postCameraPng, changeSizePng, type, initStory } = props;
  const {
    closePng,
    noVolumePng,
    volumePng,
  } = props;
  const { goBack = () => { } } = props;
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
})
const Tabs = React.memo((props) => {
  const { toolsInsetBottom, transX, changeType, type } = props
  const types = [
    {
      type: 'post',
      name: `${I18n.t('work')}`,
    },
    {
      type: 'story',
      name: `${I18n.t('Snapshot')}`,
    },
  ];
  return (
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
  )
})
const Entry = (props) => {
  const [bottomToolsVisibility, setBottomToolsVisibility] = useState(true);
  const [bottomToolsVisibilityType, setBottomToolsVisibilityType] = useState(1);
  //story 是否初始化过，切换过，后续仅暂停拍摄器，不销毁
  const [initStory, setInitStory] = useState(false);
  const showBottomTools = (visibilityType = 1) => {
    setBottomToolsVisibility(true);
    setBottomToolsVisibilityType(visibilityType);
  }
  const hideBottomTools = () => {
    setBottomToolsVisibility(false);
    setBottomToolsVisibilityType(0);
  }
  const dispatch = useDispatch();
  const type = useSelector((state) => {
    return state.shootContainer.type;
  });
  const changeFlagLock = React.useRef(false);
  const transX = React.useRef(new Animated.Value(type === 'post' ? 30 : -30)).current;
  useEffect(() => {
    if (!props.isDrawerOpen) {
      console.info('贵安必了')
      setInitStory(false)
    } else {
      if (type === 'post') {
        props?.setAudioMode?.();
      }
      changeType({ type }, false)
    }
  }, [props.isDrawerOpen, type]);
  const { run: changeType } = useThrottleFn(
    (i, setFlag = true) => {
      changeFlagLock.current = true;
      Animated.timing(transX, {
        duration: 150,
        toValue: i.type === 'post' ? 30 : -30,
        useNativeDriver: true,
      }).start(({finished})=>{
        if (finished) {
          if (i.type === 'story' && !initStory) {
            setInitStory(true)
          }
        }
     
      });
      if (setFlag) {
        dispatch(setType(i.type));
      }
      setTimeout(() => {
        changeFlagLock.current = false;
      }, 0);
      console.info(i.type, initStory, 'initStory')
    
    },
    { wait: 0 },
  );
  //
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
  const isShowStory = type === 'story' || type === 'storyedit';
  return (
    <View style={{ width: "100%", height: "100%", backgroundColor: '#000', position: 'relative' }}>
      {(props?.isDrawerOpen || props.isExample) && <StatusBar backgroundColor={"#000"} barStyle={'light-content'} animated />}
      <>
        {
          Platform.OS === 'ios' ?
            <View style={{ display: (type === 'post' || type === 'edit') ? 'flex' : 'none', width: '100%', height: '100%' }}>
              <PostView {...props} initStory={initStory} type={type} />
            </View>
            :
            <View style={{ width: '100%', height: '100%' }}>
              <PostView {...props} initStory={initStory} type={type} />
            </View>
        }
        {Platform.OS === 'ios' ?
          ((initStory && (props.isDrawerOpen || props.isExample)) ?
            <View key='StoryViewOpenWrapper' style={{ display: (type === 'story' || type === 'storyedit') ? 'flex' : 'none', height: '100%' }}>
              <StoryView {...props}
                type={type}
                key='StoryViewOpen'
                bottomToolsVisibility={bottomToolsVisibility}
                bottomToolsVisibilityType={bottomToolsVisibilityType}
                initStory={initStory}
                showBottomTools={showBottomTools}
                hideBottomTools={hideBottomTools}
              />
            </View>
            :
            ((type === 'story' || type === 'storyedit') &&
              <View key='StoryViewCloseWrapper' style={{ height: '100%', }}>
                <StoryView {...props}
                  key='StoryViewClose'
                  type={type}
                  bottomToolsVisibility={bottomToolsVisibility}
                  bottomToolsVisibilityType={bottomToolsVisibilityType}
                  initStory={initStory}
                  showBottomTools={showBottomTools}
                  hideBottomTools={hideBottomTools}
                />
              </View>
            )
          )
          :
          (
            (initStory && (props.isDrawerOpen || props.isExample)) ?
              <View  key='StoryViewOpenWrapper'  style={[styles.storyViewStyles, {
                width: isShowStory ? "100%" : 0,
                height: isShowStory ? "100%" : 0,
                position: isShowStory ? 'absolute' : 'relative',
              }]}>
                <StoryView {...props}
                                key='StoryViewOpen'
                                type={type}
                  bottomToolsVisibility={bottomToolsVisibility}
                  bottomToolsVisibilityType={bottomToolsVisibilityType}
                  initStory={initStory}
                  showBottomTools={showBottomTools}
                  hideBottomTools={hideBottomTools}
                />
              </View>
              :
              ((type === 'story' || type === 'storyedit') &&
                <View key='StoryViewCloseWrapper' style={[styles.storyViewStyles, {
                  width: isShowStory ? "100%" : 0,
                  height: isShowStory ? "100%" : 0,
                  position: isShowStory ? 'absolute' : 'relative',
                }]}>
                  <StoryView {...props}
                    key='StoryViewClose'
                    type={type}
                    bottomToolsVisibility={bottomToolsVisibility}
                    bottomToolsVisibilityType={bottomToolsVisibilityType}
                    initStory={initStory}
                    showBottomTools={showBottomTools}
                    hideBottomTools={hideBottomTools}
                  />
                </View>
              )
          )
        }
        {((bottomToolsVisibilityType === 1) && (type === 'story' || type === 'post')) &&
          <Tabs key='Tabs' type={type} changeType={changeType} toolsInsetBottom={toolsInsetBottom} transX={transX}/>
        }
      </>
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
  storyViewStyles: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  }
});
export default Entry;