package com.example.lego

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.widget.ImageView
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper
import java.lang.Exception
import java.net.URL

class DataBase(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteAssetHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BrickList.db"
    }

    private inner class URLConnect : AsyncTask<String, Int, String>(){

        override fun doInBackground(vararg params: String?): String {
            try {
                val u = params[0]
                val url = URL(u)
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = connection.getInputStream()
                var ret = ByteArray(lengthOfFile)
                val data = ByteArray(1024)
                var total: Long = 0
                var progress = 0
                var count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progressTemp = total.toInt() * 100 / lengthOfFile
                    if (progressTemp % 10 == 0 && progress != progressTemp) {
                        progress = progressTemp
                    }
                    ret += data.copyOfRange(0, count)
                    count = isStream.read(data)
                }
                isStream.close()

            }catch (e: Exception){
                return "Not a success"
            }
            return "success"
        }
    }

    fun getJulianDay() : Int{
        val db = this.writableDatabase
        val query = "select julianday('now')"
        var time = 0
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()){
            time = cursor.getFloat(0).toInt()
        }
        cursor.close()
        return time
    }

    fun addInventory(inventory: Inventory){
        val db = this.writableDatabase
        val id = inventory.id
        val name = inventory.invName
        val active = inventory.active
        val lastAccessed = inventory.lastAccessed
        var values = ContentValues()
        values.put("ID", id)
        values.put("NAME", name)
        values.put("ACTIVE", active)
        values.put("LASTACCESSED", lastAccessed)
        db.insert("INVENTORIES", null, values)

        fun execQueryOnTable(query: String) : String?{
            val cursor = db.rawQuery(query, null)
            if(cursor.moveToFirst()){
                val ret = cursor.getString(0)
                cursor.close()
                return ret
            }
            return null
        }

        for(i in 0 until inventory.inventoryItems.size){
            val item = inventory.inventoryItems[i]
            val type = item.type
            val itemId = item.id
            val qtySet = item.quantity
            val qtyAct = item.quantityActual
            val color = item.colorId
            val extra = item.extra

            val itemIdFromTable: String? = execQueryOnTable("select id from Parts where Code='$itemId'")
            val colorIdFromTable: String? = execQueryOnTable("select id from Colors where Code=$color")
            val typeIdFromTable: String? = execQueryOnTable("select id from ItemTypes where Code='$type'")

            values = ContentValues()
            values.put("InventoryID", id)
            values.put("TypeID", typeIdFromTable)
            values.put("ItemID", itemIdFromTable)
            values.put("QuantityInSet", qtySet)
            values.put("QuantityInStore", qtyAct)
            values.put("ColorID", colorIdFromTable)
            values.put("Extra", extra)
            db.insert("InventoriesParts", null, values)
            val value = getItemCodeByColor(itemIdFromTable, colorIdFromTable)
            if(getInfoAboutPhoto(itemIdFromTable, colorIdFromTable) && value != null)
                URLConnect().execute("https://www.lego.com/service/bricks/5/2/$value")
        }
        db.close()
    }

    private fun getItemCodeByColor(itemId: String?, colorId: String?) : Int?{
        val db = this.writableDatabase
        val query = "select Code from Codes where ItemID=$itemId and ColorID=$colorId"
        val cursor = db.rawQuery(query, null)
        var code: Int? = null
        if (cursor.moveToFirst())
            code = cursor.getInt(0)
        cursor.close()
        return code
    }

    private fun getInfoAboutPhoto(itemId: String?, colorId: String?) : Boolean{
        val db = this.writableDatabase
        val query = "select Image from Codes where ItemID=$itemId and ColorID=$colorId"
        val cursor = db.rawQuery(query, null)
        if(cursor.moveToFirst()){
            val blob = cursor.getBlob(0)
            cursor.close()
            return blob == null
        }
        return true
    }

    fun getInventoryById(id: Int) : Inventory{
        val db = this.writableDatabase
        var query = "select Name, Active, LastAccessed from Inventories where id = $id"
        var cursor = db.rawQuery(query, null)
        var name: String? = null
        var active: Int? = null
        var lastAccessed: Int? = null
        if(cursor.moveToFirst()){
            name = cursor.getString(0)
            active = Integer.parseInt(cursor.getString(1))
            lastAccessed = Integer.parseInt(cursor.getString(2))
        }
        cursor.close()

        val inv = Inventory(id, name, active, lastAccessed)

        query = "select I.TypeID, P.id, P.Name, I.QuantityInSet, I.QuantityInStore, C.id, C.Name, I.Extra from InventoriesParts I join Parts P on I.ItemID=P.id join Colors C on I.ColorID = C.id where InventoryID=$id"
        cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()){
            do {
                val typeId = cursor.getString(0)
                val itemId = cursor.getString(1)
                val itemName = cursor.getString(2)
                val quantityInSet = cursor.getInt(3)
                val quantityInStore = cursor.getInt(4)
                val colorId = cursor.getInt(5)
                val colorName = cursor.getString(6)
                val extra = cursor.getInt(7)
                val item = Inventory.Item(typeId, itemId, quantityInSet, quantityInStore, colorId, extra)
                item.itemName = itemName
                item.itemColor = colorName
                inv.inventoryItems.add(item)

            }while(cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return inv
    }

    fun getAllInventories() : MutableList<Inventory>{
        val db = this.writableDatabase
        val inventories: MutableList<Inventory> = mutableListOf()
        val query = "select id from Inventories"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()){
            do {
                val id = cursor.getInt(0)
                inventories.add(getInventoryById(id))
            }while(cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return inventories
    }

    fun getTypeById(id: Int) : String?{
        val db = this.writableDatabase
        val query = "select Code from ItemTypes where id=$id"
        val cursor = db.rawQuery(query, null)

        if(cursor.moveToFirst()){
            val type = cursor.getString(0)
            cursor.close()
            return type
        }
        return null
    }

}
