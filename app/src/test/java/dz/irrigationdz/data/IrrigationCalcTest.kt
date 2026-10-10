package dz.irrigationdz.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * JVM unit tests for the pure [Agronomy] engine — no device needed.
 * Expected values are hand-computed from the FAO formulas documented in
 * Agronomy.kt; an independent Python cross-check lives in docs/TEST_TABLE.md.
 */
class IrrigationCalcTest {

    private val wheat = Agronomy.cropById("wheat")
    private val potato = Agronomy.cropById("potato")
    private val loam = Agronomy.soilById("loam")
    private val drip = Agronomy.methodById("drip")

    @Test
    fun `wheat mid-season ETc is 6,9 mm per day at ETo 6`() {
        val etc = Agronomy.etCropMmPerDay(6.0, Agronomy.kcForStage(wheat, Agronomy.GrowthStage.MID))
        assertEquals(6.9, etc, 1e-9)
    }

    @Test
    fun `net dose over 3 days without rain is 20,7 mm`() {
        assertEquals(20.7, Agronomy.netDoseMm(6.9, 3.0, 0.0), 1e-9)
    }

    @Test
    fun `rain above need clamps net dose to zero`() {
        assertEquals(0.0, Agronomy.netDoseMm(6.9, 1.0, 25.0), 1e-9)
    }

    @Test
    fun `gross drip dose divides net by 0,90`() {
        assertEquals(23.0, Agronomy.grossDoseMm(20.7, drip.efficiency), 1e-9)
    }

    @Test
    fun `23 mm over 1 ha is 230 m3`() {
        assertEquals(230.0, Agronomy.volumeM3(23.0, 1.0), 1e-9)
    }

    @Test
    fun `230 m3 at 10 L per s takes 6,39 h`() {
        assertEquals(230.0 / 36.0, Agronomy.durationHours(230.0, 10.0), 1e-9)
    }

    @Test
    fun `potato on loam waters every 5 days at ETc 5,75`() {
        // RAW = 170 x 0,5 x 0,35 = 29,75 mm ; 29,75 / 5,75 = 5,17 -> 5 days
        assertEquals(5, Agronomy.intervalDays(potato, loam, 5.75))
    }

    @Test
    fun `sandy soil shortens interval versus clay`() {
        val sandy = Agronomy.soilById("sandy")
        val clay = Agronomy.soilById("clay")
        val iSandy = Agronomy.intervalDays(potato, sandy, 5.75)
        val iClay = Agronomy.intervalDays(potato, clay, 5.75)
        assert(iSandy < iClay) { "sandy=$iSandy clay=$iClay" }
    }

    @Test
    fun `full dose pipeline for 2 ha tomato drip`() {
        val dose = Agronomy.computeDose(
            crop = Agronomy.cropById("tomato"),
            soil = loam,
            method = drip,
            etoMmPerDay = 6.0,
            stage = Agronomy.GrowthStage.MID,
            areaHa = 2.0,
            flowLs = 5.0
        )
        // ETc = 6,0 x 1,15 = 6,9 ; RAW = 170 x 0,7 x 0,4 = 47,6 -> 7 days
        assertEquals(6.9, dose.etCropMmPerDay, 1e-9)
        assertEquals(7, dose.intervalDays)
        assertEquals(48.3, dose.netMm, 1e-9)
        assertEquals(48.3 / 0.9, dose.grossMm, 1e-9)
        assertEquals(48.3 / 0.9 * 20.0, dose.volumeM3, 1e-6)
        assertEquals(dose.volumeM3 / 18.0, dose.durationHours, 1e-9)
    }

    @Test
    fun `plan events are spaced by interval`() {
        val dose = Agronomy.computeDose(wheat, loam, drip, 6.0, Agronomy.GrowthStage.MID, 1.0, 5.0)
        val events = Agronomy.planEvents(dose, 3)
        assertEquals(3, events.size)
        assertEquals(0, events[0].dayOffset)
        assertEquals(dose.intervalDays, events[1].dayOffset)
        assertEquals(2 * dose.intervalDays, events[2].dayOffset)
    }

    @Test
    fun `season estimate scale is sane for wheat`() {
        // avgKc = (0,4+0,8+1,15+0,4)/4 = 0,6875 ; 0,6875 x 6 x 150 = 618,75 mm
        assertEquals(618.75, Agronomy.seasonNetMm(wheat, 6.0), 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero flow is rejected`() {
        Agronomy.durationHours(100.0, 0.0)
    }
}
