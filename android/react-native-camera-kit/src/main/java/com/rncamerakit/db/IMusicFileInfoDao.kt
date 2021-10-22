package com.rncamerakit.db

import java.sql.SQLException

interface IMusicFileInfoDao {

    @Throws(SQLException::class)
    fun insert(info: MusicFileInfo)

    @Throws(SQLException::class)
    fun insertList(list: MutableList<MusicFileInfo>?)

    @Throws(SQLException::class)
    fun createOrUpdate(info: MusicFileInfo)

    @Throws(SQLException::class)
    fun replace(info: MusicFileInfo)

    @Throws(SQLException::class)
    fun updateLocalPath(songID: Int?, localPath: String?)

    @Throws(android.database.SQLException::class)
    fun queryAll(): MutableList<MusicFileInfo>?

    @Throws(android.database.SQLException::class)
    fun query(songID: Int?): MusicFileInfo?

    @Throws(android.database.SQLException::class)
    fun queryList(queryMsg: String?, page: Int?, total: Int?): MutableList<MusicFileInfo>?

    @Throws(android.database.SQLException::class)
    fun delete(songID: Int?)

    @Throws(android.database.SQLException::class)
    fun deleteAll()

}