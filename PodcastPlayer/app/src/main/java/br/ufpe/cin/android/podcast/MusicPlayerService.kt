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


class MusicPlayerService : Service() {

    private var mPlayer: MediaPlayer? = null
    private val mBinder = MusicBinder()
    private val NOTIFICATION_ID = 2
    private var currentEpisode: String? = null

    override fun onCreate() {
        super.onCreate()

        mPlayer = MediaPlayer()
        mPlayer?.isLooping = true

        createChannel()
        startForeground(NOTIFICATION_ID, getNotification(""))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        mPlayer?.release()
        super.onDestroy()
    }
    private fun play(path:String, position: Int){
        val fis = FileInputStream(path)
        mPlayer?.reset()
        mPlayer?.setDataSource(fis.fd)
        mPlayer?.prepare()
        mPlayer?.seekTo(position)
        fis.close()
        mPlayer?.start()
    }

    fun playPodcast(title: String, path: String) {
        if (currentEpisode != title) {
            saveAndPlay(title, path)
            currentEpisode = title
            updateNotification(title)
        } else {
            if (!mPlayer!!.isPlaying) {
                mPlayer?.start()
            } else {
                updatePosition(title, mPlayer!!.currentPosition)
                mPlayer?.pause()
            }
        }
    }

    private fun saveAndPlay(title: String, podcastPath: String){
        if (currentEpisode != null) {
            updatePosition(title, mPlayer!!.currentPosition)
            playFomPosition(title, podcastPath)
        } else {
            play(podcastPath, 0)
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
            play(podcastPath, episode.position)
        }
    }


    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "1",
                "Podcast Player Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(text: String) {
        val notification = getNotification(text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getNotification(text: String) : Notification {
        val notificationIntent = Intent(applicationContext, MusicPlayerService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        return NotificationCompat.Builder(applicationContext, "1")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentTitle("Podcast Player")
            .setContentText(text)
            .setContentIntent(pendingIntent).build()
    }

    inner class MusicBinder : Binder() {
        internal val playerService: MusicPlayerService
            get() = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }
}