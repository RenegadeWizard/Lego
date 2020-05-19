package com.example.lego

import android.os.AsyncTask
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class URLConnect : AsyncTask<String, Int, String>(){
    override fun doInBackground(vararg params: String?): String {
        try {
            val url = URL("http://fcds.cs.put.poznan.pl/MyWeb/BL/615.xml")
            val connection = url.openConnection()
            connection.connect()
            val lengthOfFile = connection.contentLength
            val isStream = url.openStream()
            val testDirectory = File("inventory/XML")
            if (!testDirectory.exists()) testDirectory.mkdir()
            val fos = FileOutputStream("$testDirectory/waluty")
            val data = ByteArray(1024)
            var count = 0
            var total: Long = 0
            var progress = 0
            count = isStream.read(data)
            while (count != -1) {
                total += count.toLong()
                val progress_temp = total.toInt() * 100 / lengthOfFile
                if (progress_temp % 10 == 0 && progress != progress_temp) {
                    progress = progress_temp
                }
                fos.write(data, 0, count)
                count = isStream.read(data)
            }
            isStream.close()
            fos.close()
        }catch (e: MalformedURLException){
            return "Malformed URL"
        }catch (e: FileNotFoundException){
            return "File not found"
        }catch (e: IOException){
            return "IO Exception"
        }

        return "success"
    }
}