package com.rncamerakit.db

import android.content.Context
import java.sql.SQLException

interface IMusicFileInfoDao {

    @Throws(SQLException::class)
    fun insert(info: MusicFileBean)

    @Throws(SQLException::class)
    fun insertList(list: MutableList<MusicFileBean>?)

    @Throws(SQLException::class)
    fun createOrUpdate(info: MusicFileBean)

    @Throws(SQLException::class)
    fun replace(info: MusicFileBean)

    @Throws(SQLException::class)
    fun updateLocalPath(songID: String?, localPath: String?,duration:Int?)

    @Throws(android.database.SQLException::class)
    fun queryAll(): MutableList<MusicFileBean>?

    @Throws(android.database.SQLException::class)
    fun query(songID: String?, context: Context?): MusicFileBean?

    @Throws(android.database.SQLException::class)
    fun queryList(queryMsg: String?, page: Int?, total: Int?, context: Context?): MutableList<MusicFileBean>?

    @Throws(android.database.SQLException::class)
    fun delete(songID: String?)

    @Throws(android.database.SQLException::class)
    fun deleteAll()

}