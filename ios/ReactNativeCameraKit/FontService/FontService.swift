//
//  FontService.swift
//  ReactNativeAliAVKit
//
//  Created by jimmy on 2021/12/13.
//

import Foundation

enum Server {
    case font
    
    func resourceURL() -> URL? {
        switch self {
        case .font:
            return URL(string: "https://static.paiyaapp.com/font/fonts.json")
        }
    }
}

struct FontInfo: Codable{
    var banner = ""
    var icon = ""
    var id = 0
    var level = 0
    var name = ""
    var sort = 0
    var type = 0
    var url = ""
}

let kAlibaseUrlString =  "https://alivc-demo.aliyuncs.com";

@objc(FontService)
class FontService: RCTEventEmitter {
    private var fontModel: AliyunEffectResourceModel?
    private var dbManager: AliyunDBHelper?
    private var hasListeners = false
    
    @objc(fetchFontList:rejecter:)
    func fetchFontList(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        fetchFontResource { infos in
            let dicts = infos.map{
                [
                    "banner":$0.banner,
                    "icon":$0.icon,
                    "id":$0.id,
                    "level":$0.level,
                    "name":$0.name,
                    "sort":$0.sort,
                    "type":$0.type,
                    "url":$0.url
                ]
            }
            resolve(dicts)
        }
    }
    
    @objc(downloadFont:resolver:rejecter:)
    func downloadFont(fontID: NSNumber,
                      resolve: @escaping RCTPromiseResolveBlock,
                      reject: @escaping RCTPromiseRejectBlock) {
        
        let idValue = fontID.intValue
        self.dbManager = AliyunDBHelper()
        self.dbManager?.openResourceDBSuccess(nil, failure: nil)
        
        if idValue == 0 || idValue == -2 {
            reject("","无对应字体",nil)
            return
        }
        
        requestFont(with: idValue) { data in
            guard let fontModel = try? AliyunEffectResourceModel(dictionary:data) else {
                return;
            }
            fontModel.effectType = .font
            self.fontModel = fontModel
            
            let isSave = self.dbManager?.queryOneResourse(with: fontModel) ?? false
            
            guard !isSave else {
                print("字体已下载")
                resolve(data)
                return;
            }
            self.downloadFont(with: fontModel)
            resolve(data)
        } failure: { err in
            reject("","",nil)
        }
    }
    
    override func startObserving() {
        hasListeners = true
    }
    
    override func stopObserving() {
        hasListeners = false
    }
    
    override class func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc(supportedEvents)
    override func supportedEvents() -> [String]! {
        ["onFontDownloadProgress"]
    }
    
}

extension FontService {
    
    private func requestFont(with fontID: Int,
                             success:@escaping ([AnyHashable : Any]?)->Void,
                             failure:@escaping (Error)->Void) {
        AppServer.getRequestPath("/resource/getFont", parameters: ["fontId":fontID]) { response, responseObject, error in
            if error == nil {
                success(responseObject?["data"] as? [AnyHashable : Any])
            } else {
                failure(error!)
            }
        }
    }
    
    private func downloadFont(with fontInfo: AliyunEffectResourceModel) {
        let fileName = "\(fontInfo.eid)-\(fontInfo.name ??  "")"
        let destination = fontResourceURL.appendingPathComponent("\(fileName)-tmp").path
        
        let zipPathURL = fontResourceURL.appendingPathComponent(fileName)
        
        AppServer.download(withRemotePath: fontInfo.url,
                           toDestinationPath: destination) { progressObj in
            if self.hasListeners {
                let fraction = progressObj?.fractionCompleted ?? 0.0
                print("------ downloadFont:\(fraction)")
                self.sendEvent(withName: "onFontDownloadProgress", body: ["progress": fraction])
            }
        } completionHandler: { filePathURL, error in
            guard error == nil else {
                return
            }
            self.unzipFile(from: filePathURL!, to: zipPathURL)
        }
        
    }
    
    private func unzipFile(from filPathURL: URL, to destinationURL: URL) {
        let unzipSuccess = SSZipArchive.unzipFile(atPath: filPathURL.path,
                                                  toDestination: destinationURL.path)
        
        guard unzipSuccess else {
            print("解压失败")
            return
        }
        
        if FileManager.default.fileExists(atPath: filPathURL.path) {
            try? FileManager.default.removeItem(atPath: filPathURL.path)
        }
        
        guard let fileBase = try? FileManager.default.contentsOfDirectory(atPath: destinationURL.path).first else {
            return
        }
        
        let fileBaseURL = destinationURL.appendingPathComponent(fileBase)
        guard let fontFiles = try? FileManager.default.contentsOfDirectory(atPath: fileBaseURL.path) else {
            return
        }
        fontFiles.forEach{
            let oldURL = fileBaseURL.appendingPathComponent($0)
            try? FileManager.default.moveItem(at: oldURL, to: destinationURL.appendingPathComponent($0))
        }
        
        if FileManager.default.fileExists(atPath: fileBaseURL.path) {
            try? FileManager.default.removeItem(at: fileBaseURL)
        }
        
        //注册字体
        let fontPathURL = destinationURL.appendingPathComponent("font").appendingPathExtension("ttf")
        let fontName = AliyunEffectFontManager.shared().registerFont(withFontPath: fontPathURL.path)
        
        let filePathArray = destinationURL.path.components(separatedBy: "Documents/")
        let relativePath = "Documents/\(filePathArray.last ?? "")"
        
        fontModel?.fontName = fontName ?? ""
        fontModel?.resourcePath = relativePath
        fontModel?.isDBContain = true
        
        self.dbManager?.insertData(with: self.fontModel)
        self.dbManager?.closeDB()
    }
    
    
    private func fetchFontResource(_ complete: @escaping ([FontInfo])->Void) {
        guard let url = Server.font.resourceURL() else { return }
        let req = URLRequest(url: url)
        let task = URLSession.shared.downloadTask(with: req) { locationURL, response, err in
            guard let res = response else { return }
            
            let destURL = self.fontResourceURL.appendingPathComponent(res.suggestedFilename!)
            
            try? FileManager.default.moveItem(at: locationURL!, to: destURL)
            
            guard let data = try? Data.init(contentsOf: destURL) else{
                return
            }
            guard let json = try? JSONDecoder().decode([FontInfo].self, from: data) else {
                return
            }
            complete(json)
        }
        
        task.resume()
    }
    
    private var fontResourceURL: URL {
        let root = URL(fileURLWithPath:AliyunPathManager.aliyunRootPath()).appendingPathComponent("QPRes")
        let fontURL = root.appendingPathComponent("fontRes")
        if !FileManager.default.fileExists(atPath: fontURL.path) {
            try? FileManager.default.createDirectory(at: fontURL, withIntermediateDirectories: true, attributes: nil)
        }
        print("--------: \(fontURL.path)")
        return fontURL
    }
}
