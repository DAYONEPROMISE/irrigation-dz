package dz.irrigationdz

import android.app.Application
import dz.irrigationdz.data.ParcelRepository

class IrrigationApp : Application() {
    val repository: ParcelRepository by lazy { ParcelRepository.get(this) }
}
