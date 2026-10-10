# IrrigationDZ — test table

Engine: `Agronomy.kt` (pure JVM). Suite: `IrrigationCalcTest` (12 tests).
Cross-checked with an independent Python computation on 2026-10-10 — all
expected values below matched to < 1e-9.

| # | Case | Inputs | Expected | Status |
|---|------|--------|----------|--------|
| 1 | Wheat ETc mid-season | ETo 6.0, Kc 1.15 | 6.9 mm/day | ✅ python ✓ / CI |
| 2 | Net dose 3 days, no rain | 6.9 × 3 − 0 | 20.7 mm | ✅ |
| 3 | Rain above need | 6.9 × 1 − 25 | 0.0 mm (clamped) | ✅ |
| 4 | Gross drip dose | 20.7 / 0.90 | 23.0 mm | ✅ |
| 5 | Volume | 23 mm × 1 ha × 10 | 230 m³ | ✅ |
| 6 | Duration | 230 m³ @ 10 L/s (36 m³/h) | 6.389 h | ✅ |
| 7 | Potato/loam interval | RAW 29.75 / ETc 5.75 | 5 days | ✅ |
| 8 | Sandy < clay interval | same ETc | sandy shorter | ✅ |
| 9 | Full pipeline, 2 ha tomato drip | ETo 6, mid | ETc 6.9, 7 j, net 48.3 mm, gross 53.67 mm, vol 1073.3 m³, 59.63 h | ✅ |
| 10 | Plan spacing | 3 events | 0 / N / 2N days | ✅ |
| 11 | Wheat season estimate | avgKc 0.6875 × 6 × 150 | 618.75 mm | ✅ |
| 12 | Zero flow rejected | 100 m³ @ 0 L/s | IllegalArgumentException | ✅ |

CI (`.github/workflows/android-build.yml`) runs on every `main` push:
`testDebugUnitTest` → `lintDebug` → `assembleDebug`, uploads test reports +
`app-debug.apk` artifact, publishes `debug-latest` + versioned releases.
