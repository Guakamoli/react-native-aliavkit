/* eslint-disable */
import React, { useState } from 'react';
import { StyleSheet, Text, TouchableOpacity, View, Dimensions, Animated } from 'react-native';
import CameraScreen from "./CameraScreen"
import PostUpload from "./PostScreen"
const { width, height } = Dimensions.get('window');
const Entry = (props) => {
  const {
    multipleBtnPng, startMultipleBtnPng, postCameraPng, changeSizePng
  } = props
  const {
    cameraFlipPng,
    captureButtonPng, closePng, musicPng, beautifyPng, beautyAdjustPng, AaPng, filterPng, musicRevampPng,
    giveUpPng,
    noVolumePng,
    tailorPng, volumePng,
    musicDynamicGif,
    musicIconPng,
    musicIcongray,
    musicSearchPng,
    selectBeautifyPng,
    noResultPng,
    videomusicIconPng,

  } = props
  const { server, user, item, navigation, sendfile = () => { }, goBack = () => { }, haptics } = props;
  const params = props.route?.params || {}
  const initType = params.type || "post"
  const [type, setType] = useState(initType)
  const transX = React.useRef(new Animated.Value(initType === 'post' ? 30 : -30)).current
  const types = [{
    type: "post", name: "作品",
  }, {
    type: "story", name: "快拍"
  }]

  return (

    <>
      <View style={{ display: type === 'post' ? "flex" : "none" }}>
        <PostUpload
          // onRef={this.onRef}
          {...props}
          goback={
            goBack

          }
          goStory={

            () => {
              props.navigation.replace('FeedsStory')
            }
          }
          goPostEditor={(data) => {
            props.navigation.navigate('FeedsPostEditor', { ...data })

          }}
          multipleBtnImage={multipleBtnPng}
          startMultipleBtnImage={startMultipleBtnPng}
          postCameraImage={postCameraPng}
          changeSizeImage={changeSizePng}
          closePng={closePng}
          cameraModule={true}
        />
      </View>
      <View style={[type !== 'post' ? {} : { display: "none" }, { height: "100%", flex: 1 }]}>

        <CameraScreen
          actions={{ rightButtonText: 'Done', leftButtonText: 'Cancel' }}
          // 退出操作
          goback={goBack}

          goPost={() => {
            navigation.replace('FeedsPost')
          }}
          // 拿到上传数据
          getUploadFile={(data) => { sendfile(data) }}
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
          {
            transform: [
              { translateX: transX }
            ]
          }
        ]}
      >
        {types.map((i) => {
          return (<TouchableOpacity key={i.type}
            onPress={() => {
              Animated.timing(transX, {
                toValue: i.type === 'post' ? 30 : -30,
                useNativeDriver: true
              }).start()
              setType(i.type)
            }}
          >
            <Text style={[styles.toolText, type !== i.type ? styles.curretnText : {}]}> {i.name}</Text>
          </TouchableOpacity>)
        })}
      </Animated.View>
    </>
    // </View>
  );
}

const styles = StyleSheet.create({
  tools: {
    backgroundColor: 'rgba(0, 0, 0, 1)',
    borderRadius: 22,
    zIndex: 100,
    width: 120,
    height: 43,
    position: 'absolute',
    left: (width - 120) / 2,
    bottom: 40,
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  toolText: {
    color: 'white',
    fontSize: 13,
    fontWeight: "500",
    lineHeight: 18,
    // marginRight: 10,
  },
  curretnText: {
    color: "rgba(126, 126, 126, 1)"
  },

})


export default Entry
