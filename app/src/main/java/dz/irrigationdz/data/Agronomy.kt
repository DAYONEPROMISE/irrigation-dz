package dz.irrigationdz.data

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Pure offline agronomy engine for IrrigationDZ.
 *
 * Clean-room implementation of the standard FAO Irrigation & Drainage
 * Paper 24 / 56 relationships:
 *
 *   ETc  = Kc x ETo                      (crop water need, mm/day)
 *   Net  = ETc x interval - rainEff      (net dose per watering, mm)
 *   Gross = Net / efficiency             (gross dose, mm)
 *   Volume = Gross x area_ha x 10        (1 mm over 1 ha = 10 m3)
 *   Hours = Volume / flow_m3h
 *   Interval = RAW / ETc,  RAW = TAW x rootDepth x p
 *
 * All reference tables below are rounded simplifications of published FAO
 * values, documented as field estimates (ordre de grandeur), NOT as
 * calibrated advice. See docs/RESEARCH.md for sources.
 */
object Agronomy {

    // ------------------------------------------------------------------
    // Reference tables
    // ------------------------------------------------------------------

    enum class GrowthStage { INITIAL, DEVELOPMENT, MID, LATE }

    data class CropProfile(
        val id: String,
        val kcIni: Double,
        val kcDev: Double,
        val kcMid: Double,
        val kcEnd: Double,
        /** Effective root depth, metres (FAO indicative range). */
        val rootDepthM: Double,
        /** Readily-available-water depletion fraction p (0..1). */
        val depletionP: Double,
        /** Indicative season length, days. */
        val seasonDays: Int
    )

    /** Single-Kc values rounded from FAO-56 Table 12 (sub-humid to arid). */
    val CROPS: List<CropProfile> = listOf(
        CropProfile("wheat", 0.40, 0.80, 1.15, 0.40, 1.20, 0.55, 150),
        CropProfile("potato", 0.50, 0.80, 1.15, 0.75, 0.50, 0.35, 130),
        CropProfile("tomato", 0.60, 0.85, 1.15, 0.80, 0.70, 0.40, 140),
        CropProfile("onion", 0.70, 0.90, 1.05, 0.75, 0.40, 0.30, 150),
        CropProfile("maize", 0.30, 0.80, 1.20, 0.60, 1.20, 0.55, 125),
        CropProfile("melon", 0.50, 0.80, 1.05, 0.75, 0.80, 0.45, 100),
        CropProfile("pepper", 0.60, 0.85, 1.05, 0.90, 0.60, 0.40, 130),
        CropProfile("palm", 0.90, 0.90, 0.90, 0.90, 1.50, 0.50, 365),
        CropProfile("citrus", 0.70, 0.70, 0.65, 0.65, 1.20, 0.50, 365),
        CropProfile("olive", 0.65, 0.65, 0.65, 0.65, 1.50, 0.60, 365)
    )

    fun cropById(id: String): CropProfile =
        CROPS.firstOrNull { it.id == id } ?: CROPS.first { it.id == "wheat" }

    data class SoilType(val id: String, /** Total available water, mm per metre. */ val tawMmPerM: Double)

    /** TAW rounded from FAO indicative soil-water data (field-capacity minus wilting-point). */
    val SOILS: List<SoilType> = listOf(
        SoilType("sandy", 70.0),
        SoilType("sandy_loam", 120.0),
        SoilType("loam", 170.0),
        SoilType("clay", 200.0)
    )

    fun soilById(id: String): SoilType =
        SOILS.firstOrNull { it.id == id } ?: SOILS.first { it.id == "loam" }

    data class IrrigationMethod(val id: String, /** Application efficiency 0..1. */ val efficiency: Double)

    /** Field-application efficiencies (FAO indicative ranges). */
    val METHODS: List<IrrigationMethod> = listOf(
        IrrigationMethod("drip", 0.90),
        IrrigationMethod("sprinkler", 0.75),
        IrrigationMethod("surface", 0.60)
    )

    fun methodById(id: String): IrrigationMethod =
        METHODS.firstOrNull { it.id == id } ?: METHODS.first { it.id == "drip" }

    data class EtoZone(val id: String, /** Reference evapotranspiration, mm/day. */ val etoMmPerDay: Double)

    /** ETo presets for Algerian agro-climatic zones (hot-season indicative values). */
    val ETO_ZONES: List<EtoZone> = listOf(
        EtoZone("littoral", 4.5),
        EtoZone("hauts_plateaux", 6.0),
        EtoZone("steppe", 6.5),
        EtoZone("sahara", 8.0)
    )

    fun etoByZoneId(id: String): EtoZone =
        ETO_ZONES.firstOrNull { it.id == id } ?: ETO_ZONES.first { it.id == "hauts_plateaux" }

    // ------------------------------------------------------------------
    // Computation
    // ------------------------------------------------------------------

    fun kcForStage(crop: CropProfile, stage: GrowthStage): Double = when (stage) {
        GrowthStage.INITIAL -> crop.kcIni
        GrowthStage.DEVELOPMENT -> crop.kcDev
        GrowthStage.MID -> crop.kcMid
        GrowthStage.LATE -> crop.kcEnd
    }

    /** Daily crop water need ETc in mm/day. ETo clamped to the plausible 0..15 range. */
    fun etCropMmPerDay(etoMmPerDay: Double, kc: Double): Double {
        require(kc in 0.0..2.0) { "kc out of range: $kc" }
        return etoMmPerDay.coerceIn(0.0, 15.0) * kc
    }

    /** Net dose per watering in mm. Never negative: rain above need means no irrigation. */
    fun netDoseMm(etCropMmPerDay: Double, intervalDays: Double, effectiveRainMm: Double = 0.0): Double {
        require(intervalDays >= 0) { "interval must be >= 0" }
        return max(0.0, etCropMmPerDay * intervalDays - max(0.0, effectiveRainMm))
    }

    /** Gross dose in mm after application losses. */
    fun grossDoseMm(netMm: Double, efficiency: Double): Double {
        require(efficiency in 0.05..1.0) { "efficiency out of range: $efficiency" }
        return max(0.0, netMm) / efficiency
    }

    /** Water volume per watering in m3. 1 mm over 1 ha = 10 m3. */
    fun volumeM3(grossMm: Double, areaHa: Double): Double {
        require(areaHa >= 0) { "area must be >= 0" }
        return max(0.0, grossMm) * areaHa * 10.0
    }

    /** Pump/valve run time in hours. flowLs = well or network discharge in litres/second. */
    fun durationHours(volumeM3: Double, flowLs: Double): Double {
        require(flowLs > 0) { "flow must be > 0" }
        return max(0.0, volumeM3) / (flowLs * 3.6)
    }

    /**
     * Suggested interval between waterings in whole days, from soil water
     * storage: RAW = TAW x rootDepth x p. Clamped to 1..30 days.
     */
    fun intervalDays(crop: CropProfile, soil: SoilType, etCropMmPerDay: Double): Int {
        if (etCropMmPerDay <= 0.0) return 30
        val rawMm = soil.tawMmPerM * crop.rootDepthM * crop.depletionP
        return min(30, max(1, (rawMm / etCropMmPerDay).roundToInt()))
    }

    /**
     * Rough seasonal net need in mm (ordre de grandeur): mean of the four
     * stage Kcs x ETo x season length. For planning comparison only.
     */
    fun seasonNetMm(crop: CropProfile, etoMmPerDay: Double): Double {
        val avgKc = (crop.kcIni + crop.kcDev + crop.kcMid + crop.kcEnd) / 4.0
        return avgKc * etoMmPerDay.coerceIn(0.0, 15.0) * crop.seasonDays
    }

    // ------------------------------------------------------------------
    // Full dose result + 14-day plan
    // ------------------------------------------------------------------

    data class DoseResult(
        val kc: Double,
        val etCropMmPerDay: Double,
        val intervalDays: Int,
        val netMm: Double,
        val grossMm: Double,
        val volumeM3: Double,
        val durationHours: Double,
        val seasonNetM3PerHa: Double
    )

    fun computeDose(
        crop: CropProfile,
        soil: SoilType,
        method: IrrigationMethod,
        etoMmPerDay: Double,
        stage: GrowthStage,
        areaHa: Double,
        flowLs: Double,
        effectiveRainMm: Double = 0.0,
        intervalDaysOverride: Int? = null
    ): DoseResult {
        val kc = kcForStage(crop, stage)
        val etc = etCropMmPerDay(etoMmPerDay, kc)
        val interval = intervalDaysOverride?.coerceIn(1, 30) ?: intervalDays(crop, soil, etc)
        val net = netDoseMm(etc, interval.toDouble(), effectiveRainMm)
        val gross = grossDoseMm(net, method.efficiency)
        val vol = volumeM3(gross, areaHa)
        return DoseResult(
            kc = kc,
            etCropMmPerDay = etc,
            intervalDays = interval,
            netMm = net,
            grossMm = gross,
            volumeM3 = vol,
            durationHours = durationHours(vol, flowLs),
            seasonNetM3PerHa = seasonNetMm(crop, etoMmPerDay) * 10.0
        )
    }

    data class PlanEvent(val dayOffset: Int, val volumeM3: Double, val durationHours: Double)

    /** Next [count] watering events starting today, spaced by [intervalDays]. */
    fun planEvents(dose: DoseResult, count: Int = 5): List<PlanEvent> =
        List(count.coerceIn(1, 30)) { i ->
            PlanEvent(dayOffset = i * dose.intervalDays, volumeM3 = dose.volumeM3, durationHours = dose.durationHours)
        }
}
