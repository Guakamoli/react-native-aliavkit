/* eslint-disable */
import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, TouchableOpacity, View, Dimensions, Animated } from 'react-native';
import CameraScreenTest from './CameraScreenTest';
import PostUpload from './PostScreenTest';
import { useThrottleFn } from 'ahooks';
import {  useSelector, useDispatch } from 'react-redux';
import {
  setType,

} from './actions/container';
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
  const { server, user, item, navigation, sendfile = () => {}, goBack = () => {}, haptics, } = props;
  const dispatch = useDispatch()
  const type = useSelector((state) => {
    return state.shootContainer.type
  })
  const changeFlagLock =React.useRef(false)
  const lockFlag = React.useRef(false)
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
  React.useEffect(()=>{
    if (changeFlagLock.current) return
    transX.setValue(type === 'post' ? 30 : -30)
  }, [
    type
  ])
  const {run: changeType} = useThrottleFn((i)=>{
    changeFlagLock.current = true
    Animated.timing(transX, {
      toValue: i.type === 'post' ? 30 : -30,
      useNativeDriver: true,
    }).start();
    dispatch(setType(i.type))
    setTimeout(() => {
      changeFlagLock.current = false
    }, 0);
  },{wait: 1000})
  return (
    <>
      <View style={{ display: ['post', 'edit'].indexOf(type) > -1 ? 'flex' : 'none' }}>
        <PostUpload
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
          setType={(type)=>{
            dispatch(setType(type))

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
      </View>
      <View style={[['story', 'storyedit'].indexOf(type) > -1 ? {} : { display: 'none' }, { height: '100%' }]}>
        <CameraScreenTest
          actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
          // 退出操作
          {...props}
          goback={goBack}
          type={type}
          setType={(type)=>{
            dispatch(setType(type))

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
      </View>
      <Animated.View
        style={[
          styles.tools,
          {bottom: props.insets.bottom},
          { display: types.findIndex((i) => i.type === type) > -1 ? 'flex' : 'none' },
          {
            transform: [{ translateX: transX }],
          },
        ]}
      >
        {types.map((i) => {
          return (
            <TouchableOpacity
              key={i.type}
              onPress={()=>{
                changeType(i)
              }}
            >
              <Text style={[styles.toolText, type !== i.type ? styles.curretnText : {}]}> {i.name}</Text>
            </TouchableOpacity>
          );
        })}
      </Animated.View>
    </>
    // </View>
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
    bottom: 40,
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
