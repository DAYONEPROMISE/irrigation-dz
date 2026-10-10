package dz.irrigationdz.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** One irrigated field. Everything needed to recompute its dose offline. */
@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cropId: String,
    val soilId: String,
    val methodId: String,
    val etoZoneId: String,
    val customEto: Double? = null,
    val areaHa: Double,
    val flowLs: Double,
    val stageOrdinal: Int = 2, // GrowthStage.MID by default
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE id = :id")
    suspend fun byId(id: Long): ParcelEntity?

    @Insert
    suspend fun insert(parcel: ParcelEntity): Long

    @Update
    suspend fun update(parcel: ParcelEntity)

    @Delete
    suspend fun delete(parcel: ParcelEntity)
}

@Database(entities = [ParcelEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parcelDao(): ParcelDao
}
