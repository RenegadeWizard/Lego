package com.example.lego

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringBufferInputStream
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class Inventory(var id: Int, var invName: String, var active: Int, var lastAccessed: Int) {
    class Item(var type: String?, var id: String?, var quantity: Int?, var color: Int?, var extra: String?, var alternate: String?, var matchId: Int?, var counterPart: String?){}

    var inventoryItems: MutableList<Item>? = null

    fun parseFromXML(xml: String): Inventory {
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml)))
        xmlDoc.documentElement.normalize()

        val items: NodeList = xmlDoc.getElementsByTagName("item")

        for(i in 0 until items.length){
            val itemNode: Node = items.item(i)
            if(itemNode.nodeType == Node.ELEMENT_NODE){
                val elem = itemNode as Element
                val children = elem.childNodes

                var currType: String? = null
                var currId: String? = null
                var currQuantity: Int? = null
                var currColor: Int? = null
                var currExtra: String? = null
                var currAlternate: String? = null
                var currMatchId: Int? = null
                var currCounterPart: String? = null

                for (j in 0 until children.length){
                    val node = children.item(j)
                    when (node.nodeName){
                        "itemtype" -> currType = node.textContent
                        "itemid" -> currId = node.textContent
                        "qty" -> currQuantity = node.textContent.toInt()
                        "color" -> currColor = node.textContent.toInt()
                        "extra" -> currExtra = node.textContent
                        "alternate" -> currAlternate = node.textContent
                        "matchid" -> currMatchId = node.textContent.toInt()
                        "counterpart" -> currCounterPart = node.textContent
                    }
                }
                inventoryItems?.add(Item(currType, currId, currQuantity, currColor, currExtra, currAlternate, currMatchId, currCounterPart))
            }
        }
        return this
    }
}