package com.example.lego

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

class DataBase(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteAssetHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "bricklist.db"
    }

    fun selectTable(){
        val db = this.writableDatabase
        val query = "SELECT * FROM CATEGORIES"
        val cursor = db.rawQuery(query, null)
        if(cursor.moveToFirst()){
            val id = Integer.parseInt(cursor.getString(0))
            val code = Integer.parseInt(cursor.getString(1))
            val name = cursor.getString(2)
            val namepl = cursor.getString(3)
        }
        cursor.close()
        db.close()
    }
}
