package com.rncamerakit.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class FileSQLiteOpenHelper private constructor(context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val sql = "CREATE TABLE $TABLE_NAME" +
                "(" +
                "SONG_ID VARCHAR2(100) PRIMARY KEY," +
                "NAME VARCHAR2(1000)," +
                "ARTIST VARCHAR2(200)," +
                "IS_DB_CONTAIN INTEGER," + //0（false）和 1（true）
                "DURATION INTEGER," +
                "LOCAL_PATH VARCHAR2(1000)," +
                "COVER VARCHAR2(1000)," +
                "URL VARCHAR2(1000)" +
                ")"
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        const val TABLE_NAME = "MUSIC_FILE_INFO"
        const val DB_NAME = "paiya_media_files.db"
        const val VERSION = 1

        @Volatile
        private var openHelp: FileSQLiteOpenHelper? = null
        fun getInstance(context: Context?): FileSQLiteOpenHelper? {
            if (null == openHelp) {
                synchronized(FileSQLiteOpenHelper::class.java) {
                    if (null == openHelp) {
                        openHelp = FileSQLiteOpenHelper(context)
                    }
                }
            }
            return openHelp
        }
    }

}