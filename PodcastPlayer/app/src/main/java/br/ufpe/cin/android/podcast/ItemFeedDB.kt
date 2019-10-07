package br.ufpe.cin.android.podcast

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


    @Database(entities = arrayOf(ItemFeed::class), version = 2,exportSchema = false)
    abstract class ItemFeedDB : RoomDatabase() {
        abstract fun itemFeedDao() : ItemFeedDAO
        companion object {
            private var INSTANCE : ItemFeedDB? = null

            fun getDb(ctx: Context) : ItemFeedDB {
                if(INSTANCE == null) {
                    synchronized(ItemFeedDB::class) {
                        INSTANCE = Room.databaseBuilder(
                            ctx.applicationContext,
                            ItemFeedDB::class.java,
                            "itemFeed.db")
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                }

                return INSTANCE!!
            }
        }
    }
