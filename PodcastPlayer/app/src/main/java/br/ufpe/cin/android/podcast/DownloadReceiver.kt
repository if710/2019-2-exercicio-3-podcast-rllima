package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver

import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.jetbrains.anko.doAsync

class DownloadReceiver(holder: ItemFeedAdapter.ViewHolder) : BroadcastReceiver() {
    private val itemAction = holder.download
    private val playAction = holder.play

    override fun onReceive(context: Context, intent: Intent) {
        itemAction.isEnabled = false
        playAction.isEnabled = true

        val filePath = intent.getStringExtra("filePath")
        Toast.makeText(context,"Download completed: "+ filePath,Toast.LENGTH_LONG).show()
        val db = ItemPathDB.getDb(context)
        val title = intent.getStringExtra("fileTitle")
        doAsync {
            db.itemPathDao().insertItemPath(ItemPath(title, filePath!!,0))

        }
    }

}