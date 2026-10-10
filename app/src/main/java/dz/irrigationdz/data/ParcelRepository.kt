package dz.irrigationdz.data

import android.content.Context
import androidx.room.Room

class ParcelRepository private constructor(private val dao: ParcelDao) {

    fun observeAll() = dao.observeAll()
    suspend fun byId(id: Long) = dao.byId(id)
    suspend fun save(parcel: ParcelEntity): Long =
        if (parcel.id == 0L) dao.insert(parcel) else {
            dao.update(parcel); parcel.id
        }
    suspend fun remove(parcel: ParcelEntity) = dao.delete(parcel)

    /** Effective ETo for a parcel: custom value wins, else the zone preset. */
    fun etoFor(parcel: ParcelEntity): Double =
        parcel.customEto ?: Agronomy.etoByZoneId(parcel.etoZoneId).etoMmPerDay

    fun doseFor(parcel: ParcelEntity): Agronomy.DoseResult {
        val crop = Agronomy.cropById(parcel.cropId)
        val stage = Agronomy.GrowthStage.entries.getOrElse(parcel.stageOrdinal) {
            Agronomy.GrowthStage.MID
        }
        return Agronomy.computeDose(
            crop = crop,
            soil = Agronomy.soilById(parcel.soilId),
            method = Agronomy.methodById(parcel.methodId),
            etoMmPerDay = etoFor(parcel),
            stage = stage,
            areaHa = parcel.areaHa,
            flowLs = parcel.flowLs
        )
    }

    companion object {
        @Volatile private var instance: ParcelRepository? = null

        fun get(context: Context): ParcelRepository =
            instance ?: synchronized(this) {
                instance ?: run {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "irrigationdz.db"
                    ).build()
                    ParcelRepository(db.parcelDao()).also { instance = it }
                }
            }
    }
}
