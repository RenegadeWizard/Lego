package com.example.lego

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun plusButtonClick(v: View){
        val intent = Intent(this, ProjectAdding::class.java)
        startActivity(intent)
//        addButton()

    }

    fun addButton(){
        val bt = Button(this)
        bt.layoutParams = buttonsLayout.layoutParams
        bt.text = "Lole"
//        bt.layoutParams = buttonsLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        button_dynamic.text = "Dynamic Button"
    }
}
