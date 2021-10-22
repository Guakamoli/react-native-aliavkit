//
//  RNMusicInfo.m
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/10/21.
//

#import "RNMusicInfo.h"
#import <AVFoundation/AVFoundation.h>
#import "AliyunPathManager.h"

@implementation RNMusicInfo

- (instancetype)initWithDictionary:(NSDictionary *)dictionary
{
    self = [super init];
    if (self) {
        self.name = dictionary[@"name"];
        self.songID = dictionary[@"songID"];
        self.localPath =  dictionary[@"localPath"];
        self.cover = dictionary[@"cover"];
        self.url = dictionary[@"url"];
        self.artist = dictionary[@"artist"];
        self.format = dictionary[@"format"];
        
        self.isDBContain = NO;
        self.startTime = 0;
        self.duration = 0;
        self.volume = [AVAudioSession sharedInstance].outputVolume;
    }
    return self;
}

- (NSDictionary *)convertModelToInfo
{
    return @{
        @"name": self.name ? : @"",
        @"songID": self.songID ? : @"",
        @"localPath": self.localPath ? : @"",
        @"cover": self.cover ? : @"",
        @"url": self.url ? : @""
    };
}

- (BOOL)isDBContain {
    
    NSString *dirPath = [[AliyunPathManager compositionRootDir] stringByAppendingPathComponent:@"music"];
    NSString *fileName = [NSString stringWithFormat:@"%@-%@.%@",self.songID,self.name,self.format];
    NSString *filePath = [dirPath stringByAppendingPathComponent:fileName];
    
    BOOL isExist = [[NSFileManager defaultManager] fileExistsAtPath:filePath];
    if (isExist && [self.localPath isEqualToString:@""]) {
        self.localPath = filePath;
    }
    return isExist;
}

@end
