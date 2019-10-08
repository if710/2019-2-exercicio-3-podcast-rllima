package br.ufpe.cin.android.podcast
import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import android.app.NotificationManager
import android.content.Context
import org.jetbrains.anko.doAsync
import android.content.IntentFilter
import android.content.BroadcastReceiver
import kotlinx.android.synthetic.main.itemlista.view.*


class MusicPlayerService : Service() {

    private var mPlayer: MediaPlayer? = null
    private val mBinder = MusicBinder()
    private var currentEpisode: String? = null

    private var isPaused : Boolean = true
    private var currentHolder : ItemFeedAdapter.ViewHolder? = null

    override fun onCreate() {
        super.onCreate()

        mPlayer = MediaPlayer()
        mPlayer?.isLooping = true
        createChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val playFilter = IntentFilter(PLAY_ACTION)
        val pauseFilter = IntentFilter(PAUSE_ACTION)
        registerReceiver(playReceiver, playFilter)
        registerReceiver(pauseReceiver, pauseFilter)

        return START_STICKY
    }

    override fun onDestroy() {
        mPlayer?.release()
        super.onDestroy()
    }

    fun controlMusic(title: String, holder: ItemFeedAdapter.ViewHolder) {
        if(currentEpisode == null || title != currentEpisode) {
            currentEpisode = title
        }
        if(currentHolder != holder) {
            currentHolder = holder
        }
        isPaused = if(isPaused) {
            saveAndPlay(title,holder.path)
            false
        } else {
            saveAndPlay(title,holder.path)
            true
        }
    }


    private fun preparePlayer(path:String){
        val fis = FileInputStream(path)
        mPlayer?.reset()
        mPlayer?.setDataSource(fis.fd)
        mPlayer?.prepare()
        fis.close()
    }

    fun play(title: String){
        mPlayer?.start()
        currentHolder!!.itemView.playAndPause.setImageResource(R.drawable.pause_icon)
        setNotification(title, PLAY_ACTION)
    }
    fun stop(title: String){
        mPlayer?.pause()
        updatePosition(title, mPlayer!!.currentPosition)
        currentHolder!!.itemView.playAndPause.setImageResource(R.drawable.play_icon)
        setNotification(title, PAUSE_ACTION)


    }

    fun playPodcast(title: String, holder: ItemFeedAdapter.ViewHolder) {
        currentHolder = holder
        val path = holder.path

        if (currentEpisode != title) {
            preparePlayer(path)
            saveAndPlay(title, path)
            currentEpisode = title
        } else {
            if (!mPlayer!!.isPlaying) {
               play(holder.title.text.toString())
            } else {
                stop(holder.title.text.toString())

            }
        }
    }

    private fun saveAndPlay(title: String, podcastPath: String){
        if (currentEpisode != null) {
            updatePosition(title, mPlayer!!.currentPosition)
            playFomPosition(title, podcastPath)
        } else {
            play(podcastPath)
        }

    }private fun updatePosition(title: String, position: Int) {
        var db = ItemPathDB.getDb(this)
        doAsync {
            val itemPath = db.itemPathDao().search(title)
            itemPath.position = position
            db.itemPathDao().insertItemPath(ItemPath(title,itemPath.path,position))
        }
    }

    private fun playFomPosition(title: String, podcastPath: String) {
        var db = ItemPathDB.getDb(this)
        doAsync {
            var episode = db.itemPathDao().search(title)
            mPlayer?.seekTo(episode.position)
            play(title)
        }
    }


    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val mChannel = NotificationChannel("1", "Canal de Notificacoes", NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.description = "PodcastPlayer"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    fun setNotification(t: String, action: String) {
        val imageAction = if(action == PAUSE_ACTION) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val actionName = if(action == PAUSE_ACTION) {
            "Pause"
        } else {
            "Play"
        }

        val actionIntent = Intent(action)
        actionIntent.putExtra("item_title", t)
        val actionPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, actionIntent, 0)

        val notificationIntent = Intent(applicationContext, MusicPlayerService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        createChannel()

        val notification = NotificationCompat.Builder(
            applicationContext,"1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(NotificationCompat.Action(imageAction, actionName, actionPendingIntent))
            .setOngoing(true).setContentTitle("Você está escutando")
            .setContentText(t)
            .setContentIntent(pendingIntent).build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        startForeground(NOTIFICATION_ID, notification)
    }
    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            controlMusic(currentEpisode!!, currentHolder!!)
        }
    }

    private val pauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            controlMusic(currentEpisode!!, currentHolder!!)
        }
    }



    inner class MusicBinder : Binder() {
        internal val playerService: MusicPlayerService
            get() = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
    companion object {
        private val NOTIFICATION_ID = 2
        const val PLAY_ACTION = "br.ufpe.cin.android.podcast.services.PLAY_ACTION"
        const val PAUSE_ACTION = "br.ufpe.cin.android.podcast.services.PAUSE_ACTION"
    }
}