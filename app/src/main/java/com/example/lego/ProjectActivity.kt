package com.example.lego

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_project2.*
import java.io.File
import java.lang.Exception
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class ProjectActivity : AppCompatActivity() {

    var inventory: Inventory? = null
    var sortedList: MutableList<Inventory.Item>? = null
    var db: DataBase? = null
    var colorSorted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project2)
        db = DataBase(this, null, null, 1)
        unravelParams()
        titleText.text = inventory!!.invName
        createItems()
        db!!.updateAccess(inventory!!.id!!)
    }

    private fun createItems(){
        var mutableList = inventory!!.inventoryItems
        if(colorSorted)
            mutableList = sortedList!!
        for(item in mutableList)
            addItem(item)
    }

    fun sortOrNotByColor(v: View){
        colorSorted = !colorSorted
        linLayout.removeAllViews()
        createItems()
    }

    private fun unravelParams(){
        val i = this.intent
        val extras = i.extras
        if (extras!!.containsKey("inventoryNo")) {
            val id: Int = i.getIntExtra("inventoryNo", 0)
            inventory = db!!.getInventoryById(id)
        }
        sortedList = inventory!!.inventoryItems
        sortedList!!.sort()
    }

    private fun getImageFromBytes(pic: ImageView, item: Inventory.Item){
        val bmp = BitmapFactory.decodeByteArray(item.photo, 0, item.photo!!.size)
        pic.setImageBitmap(
            Bitmap.createScaledBitmap(
                bmp, pic.width,
                pic.height, false
            )
        )
    }

    private fun addItem(item: Inventory.Item){
        val top = LinearLayout(this)
        val bot = LinearLayout(this)
        val verticalLayout = LinearLayout(this)
        verticalLayout.orientation = LinearLayout.VERTICAL

        val pic = ImageView(this)

        if (item.photo == null) {
            pic.setImageResource(R.drawable.no_picture)
        } else {
            try {
                val bmp = BitmapFactory.decodeByteArray(item.photo, 0, item.photo!!.size)
                pic.setImageBitmap(
                    Bitmap.createScaledBitmap(
                        bmp, 500,
                        500, false
                    )
                )
            }catch (e: Exception){
                pic.setImageResource(R.drawable.no_picture)
            }
        }

        val minusButton = Button(this)
        minusButton.text = "-"

        val plusButton = Button(this)
        plusButton.text = "+"



        val verticalDescription = LinearLayout(this)
        verticalDescription.orientation = LinearLayout.VERTICAL

        val itemName = TextView(this)
        itemName.text = item.itemName
        val itemColor = TextView(this)
        itemColor.text = item.itemColor
        val itemQTY = TextView(this)
        val qty1 = item.quantityActual
        val qty2 = item.quantity
        itemQTY.text = "$qty1 z $qty2"

        verticalDescription.addView(itemName)
        verticalDescription.addView(itemColor)
        verticalDescription.addView(itemQTY)

        top.addView(pic)
        top.addView(verticalDescription)
        bot.addView(minusButton)
        bot.addView(plusButton)
        verticalLayout.addView(top)
        verticalLayout.addView(bot)
        linLayout.addView(verticalLayout)

        fun changeQTY(value : Int){
            if (item.quantityActual!!.plus(value) >= 0 && item.quantityActual!!.plus(value) <= item.quantity!! && inventory!!.active == 1)
                item.quantityActual = item.quantityActual?.plus(value)
            val qty = item.quantityActual
            db?.updateItem(item.idFromDB!!, qty!!)
            itemQTY.text = "$qty z $qty2"
        }

        minusButton.setOnClickListener{ changeQTY(-1) }
        plusButton.setOnClickListener{ changeQTY(1) }

    }

    fun exportXML(v: View){
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.newDocument()
        val rootElement = doc.createElement("INVENTORY")

        for(i in inventory!!.inventoryItems){
            val item = doc.createElement("ITEM")

            val itemType = doc.createElement("ITEMTYPE")
            val itemID = doc.createElement("ITEMID")
            val itemColor = doc.createElement("COLOR")
            val itemQTY = doc.createElement("QTYFILLED")

            val type = db!!.getTypeById(i.type!!.toInt())

            itemType.appendChild(doc.createTextNode(type))
            itemID.appendChild(doc.createTextNode(i.id))
            itemColor.appendChild(doc.createTextNode(i.colorId.toString()))
            itemQTY.appendChild(doc.createTextNode((i.quantity!! - i.quantityActual!!).toString()))

            item.appendChild(itemType)
            item.appendChild(itemID)
            item.appendChild(itemColor)
            item.appendChild(itemQTY)

            rootElement.appendChild(item)
        }

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        val path = this.filesDir
        val outDir = File(path, "Output")
        outDir.mkdir()
        val name = inventory!!.invName
        val file = File(outDir, "$name.xml")

        transformer.transform(DOMSource(rootElement), StreamResult(file))
    }

}
