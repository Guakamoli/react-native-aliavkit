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
    private var fontInfos: [FontInfo] = []
    
    @objc(fetchFontList:rejecter:)
    func fetchFontList(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        fontInfos.removeAll()
        fetchFontListResource { infos in
            self.fontInfos.append(contentsOf: infos)
            let db = AliyunDBHelper()
            db.openResourceDBSuccess(nil, failure: nil)
            
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
            let fontInfos = dicts
                .map{
                    let model = try? AliyunEffectResourceModel(dictionary:$0)
                    model?.effectType = .font
                    return model
                }
                .compactMap({$0})
                .map{
                    [
                        "banner":$0.banner ?? "",
                        "icon":$0.icon ?? "",
                        "id":$0.eid,
                        "level":$0.level,
                        "name":$0.name ?? "",
                        "sort":$0.sort ?? "",
                        "type":$0.type ?? "",
                        "url":$0.url ?? "",
                        "isDbContain":db.queryOneResourse(with: $0)
                    ]
                }
            db.closeDB()
            resolve(fontInfos)
        }
    }
    
    @objc(setFont:resolver:rejecter:)
    func setFont(fontID: NSNumber,
                 resolve: @escaping RCTPromiseResolveBlock,
                 reject: @escaping RCTPromiseRejectBlock) {
        
        let idValue = fontID.intValue
        self.dbManager = AliyunDBHelper()
        self.dbManager?.openResourceDBSuccess(nil, failure: nil)
        
        if idValue == 0 || idValue == -2 {
            reject("","无对应字体",nil)
            return
        }
        
        guard let info = self.fontInfos.filter({$0.id == idValue}).first else {return}
        let data = [
            "banner":info.banner,
            "icon":info.icon,
            "id":info.id,
            "level":info.level,
            "name":info.name,
            "sort":info.sort,
            "type":info.type,
            "url":info.url
        ] as [String : Any]
        
        guard let fontModel = try? AliyunEffectResourceModel(dictionary:data) else {
            reject("","",nil)
            return;
        }
        fontModel.effectType = .font
        self.fontModel = fontModel
        
        let isFontSaved = self.dbManager?.queryOneResourse(with: fontModel) ?? false
        guard !isFontSaved else {
            guard let savedData = self.fetchFontResource(idValue, data: data) else {
                reject("","注册字体失败",nil)
                return;
            }
            self.sendEvent(withName: "onFontDownloadProgress", body: ["progress": 1.0])
            resolve(savedData)
            return;
        }
        self.downloadFont(with: fontModel)
        guard let downloadedData = self.fetchFontResource(idValue, data: data) else {
            reject("","注册字体失败",nil)
            return;
        }
        print(self.fontModel!.fontName!)
        resolve(downloadedData)
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
    private func downloadFont(with fontInfo: AliyunEffectResourceModel) {
        let fileName = "\(fontInfo.eid)-\(fontInfo.name ??  "")"
        let destination = fontResourceURL.appendingPathComponent("\(fileName)-tmp").path
        
        let zipPathURL = fontResourceURL.appendingPathComponent(fileName)
        
        AppServer.download(withRemotePath: fontInfo.url,
                           toDestinationPath: destination) { progressObj in
            if self.hasListeners {
                let fraction = progressObj?.fractionCompleted ?? 0.0
//                print("------ downloadFont:\(fraction)")
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
    
    private func fetchFontResource(_ effctId: Int, data:[String:Any]) -> [String: Any]? {
        var newData = data
        guard let fontInfo = self.dbManager?.queryEffectInfo(withEffectType: 1, effctId: effctId) as? AliyunEffectFontInfo else {
            return nil
        }
        guard let fullFontInfo = self.registeFont(fontInfo) else {
            return nil
        }
        newData["isDbContain"] = true
        newData["path"] = fullFontInfo.fontPath
        newData["fontName"] = fullFontInfo.fontName
        return newData
    }
    
    private func fetchFontListResource(_ complete: @escaping ([FontInfo])->Void) {
        guard let url = Server.font.resourceURL() else { return }
        let jsonFilePathURL = fontResourceURL.appendingPathComponent(url.lastPathComponent);
        let isJsonFileExist = FileManager.default.fileExists(atPath: jsonFilePathURL.path)
        if isJsonFileExist {
            guard let infos = self.parseJSONData(with: jsonFilePathURL) else { return }
            complete(infos);
            return
        }
        
        let req = URLRequest(url: url)
        let task = URLSession.shared.downloadTask(with: req) { locationURL, response, err in
            guard let _ = response else { return }
            try? FileManager.default.moveItem(at: locationURL!, to: jsonFilePathURL)
            guard let infos = self.parseJSONData(with: jsonFilePathURL) else { return }
            complete(infos)
        }
        
        task.resume()
    }
    
    private func parseJSONData(with url: URL) -> [FontInfo]? {
        guard let data = try? Data.init(contentsOf: url) else {
            return nil
        }
        guard let infos = try? JSONDecoder().decode([FontInfo].self, from: data) else {
            return nil
        }
        return infos
    }
    
    private var fontResourceURL: URL {
        let root = URL(fileURLWithPath:AliyunPathManager.aliyunRootPath()).appendingPathComponent("QPRes")
        let fontURL = root.appendingPathComponent("fontRes")
        if !FileManager.default.fileExists(atPath: fontURL.path) {
            try? FileManager.default.createDirectory(at: fontURL, withIntermediateDirectories: true, attributes: nil)
        }
        return fontURL
    }
    
    private func registeFont(_ fontInfo: AliyunEffectFontInfo) -> AliyunEffectFontInfo? {
        guard !fontInfo.fontName.isEmpty else {
            return nil;
        }
        if UIFont(name: fontInfo.fontName, size: 10) != nil {
            return fontInfo
        }
        guard var fontPath = fontInfo.resourcePath,
              !fontPath.isEmpty else { return nil }
        
        let homeURL = URL(fileURLWithPath: NSHomeDirectory())
        fontPath = homeURL.appendingPathComponent(fontPath).appendingPathComponent("font.ttf").path
        
        if !FileManager.default.fileExists(atPath: fontPath) {
            fontPath = AliyunEffectFontManager.shared().findFontPath(withName: fontInfo.fontName)
        }
        if !FileManager.default.fileExists(atPath: fontPath) {
            return nil
        }
        guard let registerFontName = AliyunEffectFontManager.shared().registerFont(withFontPath: fontPath),
              !registerFontName.isEmpty else { return nil }
        
        guard let _ = UIFont(name: registerFontName, size: 10) else {
            return nil
        }
        
        fontInfo.fontName = registerFontName
        fontInfo.fontPath = fontPath;
        return fontInfo
    }
    
    
}
