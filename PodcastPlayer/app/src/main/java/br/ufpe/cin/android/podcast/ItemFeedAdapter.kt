package br.ufpe.cin.android.podcast
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.LocalServerSocket
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import android.net.Uri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.itemlista.view.*
import org.jetbrains.anko.doAsync

class ItemFeedAdapter (private val ctx : Context) : RecyclerView.Adapter<ItemFeedAdapter.ViewHolder>() {
    var itemFeeds = listOf<ItemFeed>()
    override fun getItemCount(): Int = itemFeeds.size
    var playerService: MusicPlayerService? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.itemlista, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.play.isEnabled = false
        val itemFeed = itemFeeds[position]
        doAsync {
            val db = ItemPathDB.getDb(ctx)
            val item = db.itemPathDao().search(itemFeed.title)
            if(!item.path.equals("")){
                holder.path = item.path
                holder.play.isEnabled = true
                holder.download.isEnabled = false

            }
        }
        holder.title?.text = itemFeed.title
        holder.date?.text = itemFeed.pubDate
        holder.download.setOnClickListener {
            holder.download.isEnabled = false
            val downloadService = Intent(ctx, DownloadService::class.java)
            downloadService.data = Uri.parse(itemFeed.downloadLink)
            downloadService.putExtra("fileTitle",itemFeed.title)
            ctx.startService(downloadService)
            cfgReceiver(holder)

        }
        holder.play.setOnClickListener{
            playerService!!.playPodcast(itemFeed.title,holder)

        }

        holder.title.setOnClickListener{
            val intent = Intent(ctx, EpisodeDetailActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("title",itemFeed.title)
            intent.putExtra("description",itemFeed.description)
            intent.putExtra("link",itemFeed.link)
            ctx.startActivity(intent)
        }


    }

    private fun cfgReceiver(holder: ViewHolder){
        val filter = IntentFilter()
        filter.addAction(DownloadService.ACTION_DONWLOAD)
        val receiver = DownloadReceiver(holder)
        LocalBroadcastManager.getInstance(ctx).registerReceiver(receiver,filter)
    }

    class ViewHolder (item : View) : RecyclerView.ViewHolder(item) {
        val title = item.item_title
        val date = item.item_date
        val download = item.item_action
        val play = item.playAndPause
        var path = ""


    }
}