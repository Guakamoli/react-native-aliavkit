package com.rncamerakit.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import java.sql.SQLException
import java.util.*

class MusicFileInfoDao private constructor() : IMusicFileInfoDao {

    companion object {
        @JvmField
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = MusicFileInfoDao()
    }

    val tableName: String = FileSQLiteOpenHelper.TABLE_NAME

    private var mHelper: FileSQLiteOpenHelper? = null

    private var mDatabase: SQLiteDatabase? = null

    fun init(context: Context?) {
        if (mHelper == null && context != null) {
            mHelper = FileSQLiteOpenHelper.getInstance(context)
        }
    }

    private fun getReadableDataBase() {
        if (mDatabase == null || mDatabase?.isOpen == false || mDatabase?.isReadOnly == false) {
            mDatabase = mHelper?.readableDatabase
        }
    }

    private fun getWritableDatabase() {
        if (mDatabase == null || mDatabase?.isOpen == false || mDatabase?.isReadOnly == true) {
            mDatabase = mHelper?.writableDatabase
        }
    }

    fun closeDB() {
        if (mDatabase != null && mDatabase?.isOpen == true) {
            mDatabase?.close()
        }
    }

    private fun close() {
//        if (mDatabase?.isOpen == true) {
//            mDatabase?.close()
//        }
    }

    @Synchronized
    override fun insert(info: MusicFileBean) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        if (haveInfo(info.songID)) {
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
        close()
    }

    @Synchronized
    override fun insertList(list: MutableList<MusicFileBean>?) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        list?.forEach { info ->
            if (!haveInfo(info.songID)) {
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
        close()
    }

    @Synchronized
    private fun haveInfo(songID: String?): Boolean {
        val sql = "SELECT * FROM $tableName WHERE SONG_ID = ?"
        val cursor = mDatabase?.rawQuery(sql, arrayOf(songID)) ?: throw SQLException("Cursor is null")
        val haveData = cursor.count > 0
        if (!cursor.isClosed) {
            cursor.close()
        }
        return haveData
    }

    @Synchronized
    override fun createOrUpdate(info: MusicFileBean) {
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
        cursor?.close()
        close()
    }

    @Synchronized
    override fun replace(info: MusicFileBean) {
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
        close()
    }

    @Synchronized
    override fun updateLocalPath(songID: String?, localPath: String?, duration: Int?) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw SQLException("SQLiteDatabase is null")
        }
        val values = ContentValues()
        values.put("IS_DB_CONTAIN", 1)
        values.put("LOCAL_PATH", localPath)
        values.put("DURATION", duration)
        mDatabase?.update(tableName, values, "SONG_ID = ?", arrayOf(songID))
        close()
    }

    @Synchronized
    override fun queryAll(): MutableList<MusicFileBean>? {
        getReadableDataBase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        val sql = "SELECT * FROM $tableName"
        val cursor = mDatabase?.rawQuery(sql, null) ?: throw SQLException("Cursor is null")
        if (!cursor.isClosed && cursor.count <= 0) {
            cursor.close()
            return null
        }
        val infoList: MutableList<MusicFileBean> = ArrayList()
        while (cursor.moveToNext()) {
            val info = MusicFileBean()
            info.songID = cursor.getString(cursor.getColumnIndex("SONG_ID"))
            info.name = cursor.getString(cursor.getColumnIndex("NAME"))
            info.artist = cursor.getString(cursor.getColumnIndex("ARTIST"))
            info.isDbContain = cursor.getInt(cursor.getColumnIndex("IS_DB_CONTAIN"))
            info.duration = cursor.getInt(cursor.getColumnIndex("DURATION"))
            info.localPath = cursor.getString(cursor.getColumnIndex("LOCAL_PATH"))
            info.cover = cursor.getString(cursor.getColumnIndex("COVER"))
            info.url = cursor.getString(cursor.getColumnIndex("URL"))
            infoList.add(info)
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        close()
        return infoList
    }

    @Synchronized
    override fun query(songID: String?, context: Context?): MusicFileBean? {
        if (songID == null) {
            return null
        }
        init(context)
        getReadableDataBase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        val sql = "SELECT * FROM $tableName WHERE SONG_ID = ?"
        val cursor = mDatabase?.rawQuery(sql, arrayOf(songID))
            ?: throw SQLException("Cursor is null")
        if (!cursor.isClosed && cursor.count <= 0) {
            cursor.close()
            return null
        }
        val info = MusicFileBean()
        while (cursor.moveToNext()) {
            info.songID = cursor.getString(cursor.getColumnIndex("SONG_ID"))
            info.name = cursor.getString(cursor.getColumnIndex("NAME"))
            info.artist = cursor.getString(cursor.getColumnIndex("ARTIST"))
            info.isDbContain = cursor.getInt(cursor.getColumnIndex("IS_DB_CONTAIN"))
            info.duration = cursor.getInt(cursor.getColumnIndex("DURATION"))
            info.localPath = cursor.getString(cursor.getColumnIndex("LOCAL_PATH"))
            info.cover = cursor.getString(cursor.getColumnIndex("COVER"))
            info.url = cursor.getString(cursor.getColumnIndex("URL"))
            break
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        close()
        return info
    }

    @Synchronized
    override fun queryList(
        queryMsg: String?,
        page: Int?,
        total: Int?,
        context: Context?
    ): MutableList<MusicFileBean> {
        init(context)
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
        val offset = (page - 1)*total
        val cursor = if (TextUtils.isEmpty(queryMsg)) {
            val sql = "SELECT * FROM $tableName ORDER BY SONG_ID ASC LIMIT $total OFFSET $offset"
            mDatabase?.rawQuery(sql, null) ?: throw SQLException("Cursor is null")
        } else {
            val sql =
                "SELECT * FROM $tableName WHERE NAME LIKE '%$queryMsg%' OR ARTIST LIKE '%$queryMsg%' ORDER BY SONG_ID ASC LIMIT $total OFFSET $offset"
            mDatabase?.rawQuery(sql, null) ?: throw SQLException("Cursor is null")
        }
        val infoList: MutableList<MusicFileBean> = ArrayList()
        while (cursor.moveToNext()) {
            val info = MusicFileBean()
            info.songID = cursor.getString(cursor.getColumnIndex("SONG_ID"))
            info.name = cursor.getString(cursor.getColumnIndex("NAME"))
            info.artist = cursor.getString(cursor.getColumnIndex("ARTIST"))
            info.isDbContain = cursor.getInt(cursor.getColumnIndex("IS_DB_CONTAIN"))
            info.duration = cursor.getInt(cursor.getColumnIndex("DURATION"))
            info.localPath = cursor.getString(cursor.getColumnIndex("LOCAL_PATH"))
            info.cover = cursor.getString(cursor.getColumnIndex("COVER"))
            info.url = cursor.getString(cursor.getColumnIndex("URL"))
            infoList.add(info)
        }
        if (!cursor.isClosed) {
            cursor.close()
        }
        close()
        return infoList
    }

    @Synchronized
    override fun delete(songID: String?) {
        getWritableDatabase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        mDatabase?.delete(tableName, "SONG_ID = ?", arrayOf(songID))
        close()
    }

    @Synchronized
    override fun deleteAll() {
        getWritableDatabase()
        if (mDatabase == null) {
            throw android.database.SQLException("SQLiteDatabase is null")
        }
        mDatabase?.delete(tableName, null, null)
        close()
    }

}