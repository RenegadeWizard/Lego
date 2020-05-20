package com.example.lego

class Inventory {
    class Item(var type: String, var id: String, var quantity: Int, var color: Int, var extra: String, var alternate: String, var matchId: Int, var counterPart: String){}

    private var items: MutableList<Item>? = null

    fun parseFromXML(xml: String){

    }
}