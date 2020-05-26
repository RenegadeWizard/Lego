package com.example.lego

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class Inventory(var id: Int?, var invName: String?, var active: Int?, var lastAccessed: Int?){
    class Item(var type: String?, var id: String?, var quantity: Int?, var quantityActual: Int?, var colorId: Int?, var extra: Int?) : Comparable<Item>{
        var itemName : String? = null
        var itemColor : String? = null
        var photo : ByteArray? = null
        var idFromDB : Int? = null
        override fun compareTo(other: Item): Int {
            return colorId!! - other.colorId!!
        }
    }

    var inventoryItems: MutableList<Item> = mutableListOf()

    fun parseFromXML(xml: String): Inventory {
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml)))
        xmlDoc.documentElement.normalize()

        val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

        for(i in 0 until items.length){
            val itemNode: Node = items.item(i)
            if(itemNode.nodeType == Node.ELEMENT_NODE){
                val elem = itemNode as Element
                val children = elem.childNodes

                var currType: String? = null
                var currId: String? = null
                var currQuantity: Int? = null
                var currColor: Int? = null
                var currExtra: Int? = null
                var currAlternate: String? = null
                var currMatchId: Int? = null
                var currCounterPart: String? = null

                for (j in 0 until children.length){
                    val node = children.item(j)
                    when (node.nodeName){
                        "ITEMTYPE" -> currType = node.textContent
                        "ITEMID" -> currId = node.textContent
                        "QTY" -> currQuantity = node.textContent.toInt()
                        "COLOR" -> currColor = node.textContent.toInt()
                        "EXTRA" -> currExtra = if(node.textContent == "N") 0 else 1
                        "ALTERNATE" -> currAlternate = node.textContent
                        "MATCHID" -> currMatchId = node.textContent.toInt()
                        "COUTERPART" -> currCounterPart = node.textContent
                    }
                }
                if(currAlternate == "N" && currType != "M")
                    inventoryItems.add(Item(currType, currId, currQuantity, 0, currColor, currExtra))
            }
        }
        return this
    }
}