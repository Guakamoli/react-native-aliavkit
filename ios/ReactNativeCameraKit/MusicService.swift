//
//  File.swift
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/12/12.
//

import Foundation
import AVFoundation

@objc(MusicService)
class MusicService: NSObject {
    lazy var player: AVPlayer = AVPlayer()
    var musics: [RNMusicInfo] = []
    var downloadingMusics:[RNMusicInfo] = []
    
    
    
    
}
