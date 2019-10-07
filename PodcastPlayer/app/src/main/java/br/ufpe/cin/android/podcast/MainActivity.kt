package br.ufpe.cin.android.podcast

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL
import android.os.IBinder


class MainActivity : AppCompatActivity() {

    internal var musicPlayerService: MusicPlayerService? = null
    internal var isBound = false
    private lateinit var itemFeedAdapter: ItemFeedAdapter

    private val sConn = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            musicPlayerService = null
            isBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, b: IBinder?) {
            val binder = b as MusicPlayerService.MusicBinder
            musicPlayerService = binder.playerService
            isBound = true
            itemFeedAdapter.playerService = musicPlayerService
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        itemFeedAdapter = ItemFeedAdapter(applicationContext)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = itemFeedAdapter

        val musicServiceIntent = Intent(this, MusicPlayerService::class.java)
        startService(musicServiceIntent)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0
            )
        }


        doAsync {
            val db = ItemFeedDB.getDb(applicationContext)
            var itemFeedList = listOf<ItemFeed>()

            try{
                var xml = URL("https://s3-us-west-1.amazonaws.com/podcasts.thepolyglotdeveloper.com/podcast.xml").readText()
                itemFeedList = Parser.parse(xml)

                db.itemFeedDao().addAll(itemFeedList)

            }catch (e:Throwable){
                itemFeedList = db.itemFeedDao().all()
                Log.e("ERRO",e.message.toString())
            }
            uiThread {
                itemFeedAdapter.itemFeeds = itemFeedList
                list.adapter = itemFeedAdapter
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if (!isBound) {
            val bindIntent = Intent(this, MusicPlayerService::class.java)
            isBound = bindService(bindIntent, sConn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        if(isBound){
            unbindService(sConn)
            isBound = false
        }

        super.onStop()
    }

}
