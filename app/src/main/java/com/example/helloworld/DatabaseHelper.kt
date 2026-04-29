package com.example.helloworld

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "AbsensiDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val queryBuatTabel = "CREATE TABLE tb_riwayat (id INTEGER PRIMARY KEY AUTOINCREMENT, nama  TEXT, lat  TEXT , lon TEXT)"
            db?.execSQL(queryBuatTabel)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS tb_riwayat")
        onCreate(db)
    }

    fun simpanRiwayat (nama: String, lat: String, lon: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("nama", nama)
        values.put("lat", lat)
        values.put("lon", lon)
        return db.insert("tb_riwayat", null, values)
    }
}