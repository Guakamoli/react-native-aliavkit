package com.rncamerakit.db

class MusicFileBaseInfo {
    var songs: MutableList<MusicFileBean>? = null
}

class MusicFileBean {

    var songID = 0

    /**
     * 文件名
     */
    var name: String? = null

    /**
     * 作者
     */
    var artist: String? = null

    /**
     * 是否下载到了本地 0：否  1：是
     */
    var isDbContain = 0

    /**
     * 音乐时长 ms
     */
    var duration = 0

    /**
     * 本地路径
     */
    var localPath: String? = null

    /**
     * 封面
     */
    var cover: String? = null

    /**
     * 文件 url
     */
    var url: String? = null

}

