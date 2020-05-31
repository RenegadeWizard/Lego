package com.example.lego

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_project_adding.*
import java.lang.Exception
import java.net.URL

class ProjectAdding : AppCompatActivity() {

    private var xml: String? = null
    private var db: DataBase? = null

    private inner class URLConnect : AsyncTask<String, Int, String>(){

        override fun doInBackground(vararg params: String?): String {
            try {

                val url = URL(params[0] +".xml")
                val connection = url.openConnection()
                connection.connect()
                val lengthOfFile = connection.contentLength
                val isStream = connection.getInputStream()
                val data = ByteArray(1024)
                var total: Long = 0
                var progress = 0
                var xmlStr = ""
                var count = isStream.read(data)
                while (count != -1) {
                    total += count.toLong()
                    val progressTemp = total.toInt() * 100 / lengthOfFile
                    if (progressTemp % 10 == 0 && progress != progressTemp) {
                        progress = progressTemp
                    }
                    xmlStr += String(data.copyOfRange(0, count))
                    count = isStream.read(data)
                }
                isStream.close()
//                doesExist()


                xml = xmlStr
            }catch (e: Exception){
//                e.printStackTrace()
//                doesNotExist()
                xml = null
                return "Not a success"
            }
            return "success"
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result == "success")
                doesExist()
            else
                doesNotExist()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_adding)
        db = DataBase(this, null, null, 1)
    }

    fun checkIfExists(v: View){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val path = sharedPreferences.getString("signature", "http://fcds.cs.put.poznan.pl/MyWeb/BL/")
        URLConnect().execute(path + idText.text.toString())
    }



    fun doesExist(){
        existsTextField.text = "Istnieje"
    }

    fun doesNotExist(){
        existsTextField.text = "Nie istnieje"
    }

    fun confirm(v: View){
        if(!xml.isNullOrBlank()){
            val inv = Inventory(idText.text.toString().toInt(), nameText.text.toString(), 1, db?.getJulianDay()).parseFromXML(xml!!)
            db?.addInventory(inv)
            finish()
        }else{
            existsTextField.text = "Sprawdź najpierw!"
        }
    }

}
