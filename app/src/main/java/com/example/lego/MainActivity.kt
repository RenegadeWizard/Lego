package com.example.lego

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var db: DataBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = DataBase(this, null, null, 1)
        val inventories = db!!.getAllInventories()
        addButtons(inventories)

    }

    fun plusButtonClick(v: View){
        val intent = Intent(this, ProjectAdding::class.java)
        startActivity(intent)
    }

    private fun addButtons(inventory: MutableList<Inventory>){
        for(inv in inventory){
            val bt = Button(this)
            bt.text = inv.invName
            bt.setOnClickListener{ changeToInventory(inv) }
            buttonsLayout.addView(bt)
        }
    }

    private fun changeToInventory(inv: Inventory){
        val intent = Intent(this, ProjectActivity::class.java)
        intent.putExtra("inventoryNo", inv.id)
        startActivity(intent)
    }

    fun changeToSettings(v: View){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

}
