package br.ufpe.cin.android.podcast


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(ItemPath::class), version = 2,exportSchema = false)
abstract class ItemPathDB : RoomDatabase() {
    abstract fun itemPathDao() : ItemPathDao
    companion object {
        private var INSTANCE : ItemPathDB? = null

        fun getDb(ctx: Context) : ItemPathDB {
            if(INSTANCE == null) {
                synchronized(ItemPathDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        ctx.applicationContext,
                        ItemPathDB::class.java,
                        "items_path.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return INSTANCE!!
        }
    }
}