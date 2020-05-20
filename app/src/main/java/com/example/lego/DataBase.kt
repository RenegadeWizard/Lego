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

    fun addInventory(inventory: Inventory){
        val db = this.writableDatabase
        val id = inventory.id
        val name = inventory.invName
        val active = inventory.active
        val lastAccessed = inventory.lastAccessed
        var query = "INSERT INTO Inventories VALUES ($id,$name,$active,$lastAccessed)"
        db.execSQL(query)
        for(i in 0 until inventory.inventoryItems!!.size){
            val item = inventory.inventoryItems!![i]
            val type = item.type
            val itemId = item.id
            val qtySet = item.quantity
            val color = item.color
            val extra = item.extra
            query = "INSERT INTO InventoriesParts (InventoryID, TypeID, ItemID, QuantityInSet, ColorID) VALUES ($id, $type, $itemId, $qtySet, 0, $color, $extra)"
            db.execSQL(query)
        }
    }
}
