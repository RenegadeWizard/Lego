package com.example.lego

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_project_adding.*

class ProjectAdding : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_adding)
//        val db = DataBase(this, null, null, 1)
        val lol = URLConnect().execute()
        val h = 0
    }
}
