package com.example.lego

import android.os.Bundle
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
        val text1 = TextView(this)
        val text2 = TextView(this)
        val text3 = TextView(this)
        val text4 = TextView(this)
        text1.text = item.id
        text2.text = "2"
        text3.text = "3"
        text4.text = "4"
        top.addView(text1)
        top.addView(text2)
        bot.addView(text3)
        bot.addView(text4)
        verticalLayout.addView(top)
        verticalLayout.addView(bot)
        linLayout.addView(verticalLayout)
    }
}
