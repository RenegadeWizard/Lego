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
                val ret = ArrayList<Byte>()
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
                    ret.addAll(data.copyOfRange(0, count).toList())
                    count = isStream.read(data)
                }
                isStream.close()
                setPicture(ret, params[1])

            }catch (e: Exception){
                return "Not a success"
            }
            return "success"
        }
    }

    private fun setPicture(pic: ArrayList<Byte>, idStr: String?){
        if(idStr == null){

        }else{
            val id = idStr.toInt()
            val db = this.writableDatabase

            val values = ContentValues().apply {
                put("Image", pic.toByteArray())
            }

            val selection = "id = ?"
            val selectionArgs = arrayOf(id.toString())
            val count = db.update(
                "Codes",
                values,
                selection,
                selectionArgs)
        }

    }

    fun getJulianDay() : Int{
        val db = this.writableDatabase
        val query = "SELECT CAST ((\n" +
                "    julianday('now')\n" +
                ") * 24 * 60 * 60 AS INTEGER)"
        var time = 0
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()){
            time = cursor.getInt(0)
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
            val codeId = getCodeIdByColor(itemIdFromTable, colorIdFromTable)
//            if(getInfoAboutPhoto(itemIdFromTable, colorIdFromTable) && value != null)
//                URLConnect().execute("https://www.lego.com/service/bricks/5/2/$value", "$codeId")
//            URLConnect().execute("http://img.bricklink.com/P/${item.colorId}/${item.id}.gif", "$codeId")
            URLConnect().execute("https://www.bricklink.com/PL/${item.id}.jpg", "$codeId")
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

    private fun getCodeIdByColor(itemId: String?, colorId: String?) : Int?{
        val db = this.writableDatabase
        val query = "select id from Codes where ItemID=$itemId and ColorID=$colorId"
        val cursor = db.rawQuery(query, null)
        var id: Int? = null
        if (cursor.moveToFirst())
            id = cursor.getInt(0)
        cursor.close()
        return id
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
            lastAccessed = cursor.getInt(2)
        }
        cursor.close()

        val inv = Inventory(id, name, active, lastAccessed)

        query = "select I.TypeID, P.id, P.Name, I.QuantityInSet, I.QuantityInStore, C.id, C.Name, I.Extra, I.id from InventoriesParts I join Parts P on I.ItemID=P.id join Colors C on I.ColorID = C.id where InventoryID=$id order by P.id"
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
                val idFromDB = cursor.getInt(8)
                val item = Inventory.Item(typeId, itemId, quantityInSet, quantityInStore, colorId, extra)
                item.itemName = itemName
                item.itemColor = colorName
                item.idFromDB = idFromDB

                val q = "select Image from Codes where ItemID=$itemId and ColorID=$colorId"
                val cur = db.rawQuery(q, null)
                if(cur.moveToFirst()){
                    item.photo = cur.getBlob(0)
                }
                cur.close()

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
        val query = "select id from Inventories order by Active Desc, LastAccessed Desc"
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

    fun updateAccess(id: Int){
        val db = this.writableDatabase

        val lastAccess: Int = getJulianDay()
        val values = ContentValues().apply {
            put("LASTACCESSED", lastAccess)
        }

        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val count = db.update(
            "INVENTORIES",
            values,
            selection,
            selectionArgs)
    }

    fun updateItem(id: Int, qty: Int){
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("QuantityInStore", qty)
        }

        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val count = db.update(
            "InventoriesParts",
            values,
            selection,
            selectionArgs)
    }

    fun archiveInventory(id: Int){
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("ACTIVE", 0)
        }

        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val count = db.update(
            "INVENTORIES",
            values,
            selection,
            selectionArgs)
    }

}
