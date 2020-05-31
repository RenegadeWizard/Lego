package com.example.lego

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_project2.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


@Suppress("DEPRECATION")
class ProjectActivity : AppCompatActivity() {

    var inventory: Inventory? = null
    var sortedList: MutableList<Inventory.Item>? = null
    var db: DataBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project2)
        db = DataBase(this, null, null, 1)
        unravelParams()
        titleText.text = inventory!!.invName
        createItems(null)
        addEntriesToColorGroup()
        db!!.updateAccess(inventory!!.id!!)
    }

    private fun createItems(color: String?){
        for(item in inventory!!.inventoryItems){
            if(color != null && color != "(Brak)"){
                if(item.itemColor == color)
                    addItem(item)
            }else{
                addItem(item)
            }

        }

    }

    private fun addEntriesToColorGroup(){
        colorGroup.prompt = "Color"
        val hashSet = hashSetOf<String>()
        for(item in inventory!!.inventoryItems){
            hashSet.add(item.itemColor!!)
        }
        val arrayList = arrayListOf("(Brak)")
        arrayList.addAll(hashSet)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        colorGroup.adapter = arrayAdapter
        colorGroup.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                linLayout.removeAllViews()
                createItems(arrayList[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {  }
        }
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

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val minusButton = Button(this)
        minusButton.text = "-"
        minusButton.width = displayMetrics.widthPixels/2

        val plusButton = Button(this)
        plusButton.text = "+"
        plusButton.width = displayMetrics.widthPixels/2

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
            if(i.quantity!! - i.quantityActual!! <= 0)
                continue
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
        val outDir1 = File(path, "Output")
        val sd = Environment.getExternalStorageDirectory().path
        val outDir2 = File(sd, "xmls")

        try {
            outDir2.mkdir()
            val name = inventory!!.invName
            val file = File(outDir2, "$name.xml")

            transformer.transform(DOMSource(rootElement), StreamResult(file))
        }catch (e : Exception){
            outDir1.mkdir()
            val name = inventory!!.invName
            val file = File(outDir1, "$name.xml")

            transformer.transform(DOMSource(rootElement), StreamResult(file))
        }
    }

}
