package com.rncamerakit.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import androidx.core.content.contentValuesOf
import java.sql.SQLException
import java.util.*

class MusicFileInfoDao private constructor() : IMusicFileInfoDao {

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = MusicFileInfoDao()
    }

    val tableName: String = FileSQLiteOpenHelper.TABLE_NAME

    private var mHelper: FileSQLiteOpenHelper? = null

    private var mDatabase: SQLiteDatabase? = null

    fun init(context: Context?) {
        mHelper = FileSQLiteOpenHelper.getInstance(context)
    }

    private fun getReadableDataBase() {
        mDatabase = mHelper?.readableDatabase
    }

    private fun getWritableDatabase() {
        mDatabase = mHelper?.writableDatabase
    }

    private fun close(cursor: Cursor?) {
        if (cursor?.isClosed == false) {
            cursor.close()
        }
        if (mDatabase?.isOpen == true) {
            mDatabase?.close()
        }
    }

    override fun insert(info: MusicFileInfo) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        if(haveInfo(info.songID)){
            return
        }
        val values = ContentValues()
        values.put("SONG_ID", info.songID)
        values.put("NAME", info.name)
        values.put("ARTIST", info.artist)
        values.put("IS_DB_CONTAIN", info.isDbContain)
        values.put("DURATION", info.duration)
        values.put("LOCAL_PATH", info.localPath)
        values.put("COVER", info.cover)
        values.put("URL", info.url)
        mDatabase?.insert(tableName, null, values)
        close(null)
    }

    override fun insertList(list: MutableList<MusicFileInfo>?) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        list?.forEach { info ->
            if(!haveInfo(info.songID)){
                val values = ContentValues()
                values.put("SONG_ID", info.songID)
                values.put("NAME", info.name)
                values.put("ARTIST", info.artist)
                values.put("IS_DB_CONTAIN", info.isDbContain)
                values.put("DURATION", info.duration)
                values.put("LOCAL_PATH", info.localPath)
                values.put("COVER", info.cover)
                values.put("URL", info.url)
                mDatabase?.insert(tableName, null, values)
            }
        }
        close(null)
    }

    private fun haveInfo(songID: Int?): Boolean {
        val sql = "SELECT * FROM $tableName WHERE SONG_ID = ?"
        val cursor = mDatabase?.rawQuery(sql, arrayOf(songID.toString()))?: throw SQLException("Cursor is null")
        val haveData  = cursor.count > 0
        cursor.close()
        return haveData
    }

    override fun createOrUpdate(info: MusicFileInfo) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        val sql = "SELECT * FROM $tableName WHERE SONG_ID = ?"
        val cursor = mDatabase?.rawQuery(sql, arrayOf(info.songID.toString()))

        val values = ContentValues()
        values.put("SONG_ID", info.songID)
        values.put("NAME", info.name)
        values.put("ARTIST", info.artist)
        values.put("IS_DB_CONTAIN", info.isDbContain)
        values.put("DURATION", info.duration)
        values.put("LOCAL_PATH", info.localPath)
        values.put("COVER", info.cover)
        values.put("URL", info.url)
        if (cursor != null && cursor.count > 0) {
            mDatabase?.update(tableName, values, null, null)
        } else {
            mDatabase?.insert(tableName, null, values)
        }
        close(cursor)
    }

    override fun replace(info: MusicFileInfo) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        val values = ContentValues()
        values.put("SONG_ID", info.songID)
        values.put("NAME", info.name)
        values.put("ARTIST", info.artist)
        values.put("IS_DB_CONTAIN", info.isDbContain)
        values.put("DURATION", info.duration)
        values.put("LOCAL_PATH", info.localPath)
        values.put("COVER", info.cover)
        values.put("URL", info.url)
        mDatabase?.replace(tableName, null, values)
        close(null)
    }

    override fun updateLocalPath(songID: Int?, localPath: String?) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        val values = ContentValues()
        values.put("IS_DB_CONTAIN", 1)
        values.put("LOCAL_PATH", localPath)
        mDatabase?.update(tableName, values, "SONG_ID = ?", arrayOf(songID.toString()))
        close(null)
    }

    override fun queryAll(): MutableList<MusicFileInfo>? {
        getReadableDataBase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        val sql = "SELECT * FROM $tableName"
        val cursor = mDatabase?.rawQuery(sql, null)?: throw SQLException("Cursor is null")
        if (cursor.count <= 0) {
            close(cursor)
            return null
        }
        val infoList: MutableList<MusicFileInfo> = ArrayList()
        while (cursor.moveToNext()) {
            val info = MusicFileInfo()
            info.songID = cursor.getInt(cursor.getColumnIndex("SONG_ID"))
            info.name = cursor.getString(cursor.getColumnIndex("NAME"))
            info.artist = cursor.getString(cursor.getColumnIndex("ARTIST"))
            info.isDbContain = cursor.getInt(cursor.getColumnIndex("IS_DB_CONTAIN"))
            info.duration = cursor.getInt(cursor.getColumnIndex("DURATION"))
            info.localPath = cursor.getString(cursor.getColumnIndex("LOCAL_PATH"))
            info.cover = cursor.getString(cursor.getColumnIndex("COVER"))
            info.url = cursor.getString(cursor.getColumnIndex("URL"))
            infoList.add(info)
        }
        close(cursor)
        return infoList
    }

    override fun query(songID: Int?): MusicFileInfo? {
        getReadableDataBase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        val sql = "SELECT * FROM $tableName WHERE SONG_ID = ?"
        val cursor = mDatabase?.rawQuery(sql, arrayOf(songID.toString()))
            ?: throw SQLException("Cursor is null")
        if (cursor.count <= 0) {
            close(cursor)
            return null
        }
        val info = MusicFileInfo()
        while (cursor.moveToNext()) {
            info.songID = cursor.getInt(cursor.getColumnIndex("SONG_ID"))
            info.name = cursor.getString(cursor.getColumnIndex("NAME"))
            info.artist = cursor.getString(cursor.getColumnIndex("ARTIST"))
            info.isDbContain = cursor.getInt(cursor.getColumnIndex("IS_DB_CONTAIN"))
            info.duration = cursor.getInt(cursor.getColumnIndex("DURATION"))
            info.localPath = cursor.getString(cursor.getColumnIndex("LOCAL_PATH"))
            info.cover = cursor.getString(cursor.getColumnIndex("COVER"))
            info.url = cursor.getString(cursor.getColumnIndex("URL"))
            break
        }
        close(cursor)
        return info
    }

    override fun queryList(
        queryMsg: String?,
        page: Int?,
        total: Int?
    ): MutableList<MusicFileInfo>? {
        getReadableDataBase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        var page = page
        if (page == null) {
            page = 1
        }
        var total = total
        if (total == null) {
            total = 10
        }
        val offset = (page - 1) * total
        val cursor = if (TextUtils.isEmpty(queryMsg)) {
            val sql = "SELECT * FROM $tableName ORDER BY SONG_ID ASC LIMIT $total OFFSET $offset"
            mDatabase?.rawQuery(sql, null) ?: throw SQLException("Cursor is null")
        } else {
            val sql =
                "SELECT * FROM $tableName WHERE NAME LIKE '%$queryMsg%' OR ARTIST LIKE '%$queryMsg%' ORDER BY SONG_ID ASC LIMIT $total OFFSET $offset"
            mDatabase?.rawQuery(sql, null) ?: throw SQLException("Cursor is null")
        }
        val infoList: MutableList<MusicFileInfo> = ArrayList()
        while (cursor.moveToNext()) {
            val info = MusicFileInfo()
            info.songID = cursor.getInt(cursor.getColumnIndex("SONG_ID"))
            info.name = cursor.getString(cursor.getColumnIndex("NAME"))
            info.artist = cursor.getString(cursor.getColumnIndex("ARTIST"))
            info.isDbContain = cursor.getInt(cursor.getColumnIndex("IS_DB_CONTAIN"))
            info.duration = cursor.getInt(cursor.getColumnIndex("DURATION"))
            info.localPath = cursor.getString(cursor.getColumnIndex("LOCAL_PATH"))
            info.cover = cursor.getString(cursor.getColumnIndex("COVER"))
            info.url = cursor.getString(cursor.getColumnIndex("URL"))
            infoList.add(info)
        }
        close(cursor)
        return infoList
    }

    override fun delete(songID: Int?) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        mDatabase?.delete(tableName, "SONG_ID = ?", arrayOf(songID.toString()))
        close(null)
    }

    override fun deleteAll() {
        getWritableDatabase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        mDatabase?.delete(tableName, null, null)
        close(null)
    }

}