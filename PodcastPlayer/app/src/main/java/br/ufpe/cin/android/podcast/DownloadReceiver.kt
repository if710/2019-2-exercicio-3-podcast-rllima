package br.ufpe.cin.android.podcast

import android.content.BroadcastReceiver

import android.content.Context
import android.content.Intent
import android.widget.Toast
import org.jetbrains.anko.doAsync

class DownloadReceiver(holder:ItemFeedAdapter.ViewHolder) : BroadcastReceiver() {
    private val itemAction = holder.download
    private val itemTitle = holder.title

    override fun onReceive(context: Context, intent: Intent) {
        itemAction.isEnabled = true

        val filePath = intent.getStringExtra("filePath")
        Toast.makeText(context,"Download completed: "+ filePath,Toast.LENGTH_LONG).show()
        val db = ItemPathDB.getDb(context)

        doAsync {
            db.itemPathDao().insertItemPath(ItemPath(itemTitle.toString(), filePath!!))
        }
    }

}