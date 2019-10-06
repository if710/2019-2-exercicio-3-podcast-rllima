package br.ufpe.cin.android.podcast

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.ufpe.cin.android.podcast.ItemPath

@Dao
interface ItemPathDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItemPath(vararg itemPath: ItemPath)

    @Query("SELECT * FROM items_path")
    fun findAllItemsPath() : Array<ItemPath>
}