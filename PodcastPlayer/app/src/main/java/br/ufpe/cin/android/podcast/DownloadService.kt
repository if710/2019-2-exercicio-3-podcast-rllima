package br.ufpe.cin.android.podcast

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService("DownloadService") {

    companion object{
        const val ACTION_DONWLOAD = "br.ufpe.cin.android.podcast.services.action.DOWNLOAD_COMPLETE"
    }

    override fun onHandleIntent(i: Intent?) {
        try{
            val root = getExternalFilesDir(DIRECTORY_DOWNLOADS)
            root?.mkdir()
            val output = File(root,i!!.data!!.lastPathSegment)
            if(output.exists()){
                output.delete()
            }
            val url = URL(i.data!!.toString())
            val c = url.openConnection() as HttpURLConnection
            val fos = FileOutputStream(output.path)
            val out = BufferedOutputStream(fos)
            try{
                val inp = c.inputStream
                val buffer = ByteArray(8192)
                var len = inp.read(buffer)
                while(len >= 0){
                    out.write(buffer,0,len)
                    len = inp.read(buffer)

                }
                out.flush()


            }finally {
                fos.fd.sync()
                out.close()
                c.disconnect()
            }

            val actionItent = Intent(ACTION_DONWLOAD)
            actionItent.putExtra("filePath",output.path)
            LocalBroadcastManager.getInstance(this).sendBroadcast(actionItent)

        }catch (e2:IOException){
            Log.e(javaClass.getName(), "Exception in download",e2)
        }

    }

}
