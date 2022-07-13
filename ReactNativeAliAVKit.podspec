require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "ReactNativeAliAVKit"
  s.version      = package["version"]
  s.summary      = "Advanced native camera and gallery controls and device photos API"
  s.license      = "MIT"

  s.authors      = "AliAVKit"
  s.homepage     = "https://github.com/Guakamoli/react-native-aliavkit"
  s.platform     = :ios, "10.0"

  s.source       = { :git => "https://github.com/Guakamoli/react-native-aliavkit", :tag => "v#{s.version}" }
  s.source_files  = "ios/**/*.{h,m,mm}"
  
  s.resource = 'ios/ReactNativeCameraKit/Resources/ShortVideoResource/*'
  
  #不允许多个resource
  #s.resource = 'ios/ReactNativeCameraKit/Resources/AliKitPhotoViewResource/*'
  
  s.dependency 'React-Core'
  
  # 柯南SDK-短视频SDK依赖的数据埋点
  s.dependency 'AlivcConan', '1.0.5'
  s.dependency 'AliyunVideoSDKPro', '3.30.0'
  s.dependency 'VODUpload', '1.6.1'
  # 美颜
  s.dependency 'Queen', '1.9.0-official-pro'
  s.dependency 'AFNetworking'
  s.dependency 'FMDB'
  s.dependency 'JSONModel'
  s.dependency 'SSZipArchive'

  # 裁剪
  s.dependency 'TOCropViewController'
end
