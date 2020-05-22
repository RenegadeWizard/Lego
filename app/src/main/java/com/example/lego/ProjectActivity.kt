package com.example.lego

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_project2.*


class ProjectActivity : AppCompatActivity() {

    var inventory: Inventory? = null
    var db: DataBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project2)
        db = DataBase(this, null, null, 1)
        val i = this.intent
        val extras = i.extras
        if (extras!!.containsKey("inventoryNo")) {
            val id: Int = i.getIntExtra("inventoryNo", 0)
            inventory = db!!.getInventoryById(id)
        }
        titleText.text = inventory!!.invName
        for(item in inventory!!.inventoryItems)
            addItem(item)
    }

    private fun addItem(item: Inventory.Item){
        val top = LinearLayout(this)
        val bot = LinearLayout(this)
        val verticalLayout = LinearLayout(this)
        verticalLayout.orientation = LinearLayout.VERTICAL

        val pic = ImageView(this)
        if (item.photo == null)
            pic.setImageResource(R.drawable.no_picture)
        else
            TODO("Set pic from byte arr")

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
            item.quantityActual = item.quantityActual?.plus(value)
            val qty = item.quantityActual
            itemQTY.text = "$qty z $qty2"
        }

        minusButton.setOnClickListener{ changeQTY(-1) }
        plusButton.setOnClickListener{ changeQTY(1) }

    }
}
