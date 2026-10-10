# IrrigationDZ 🇩🇿

Offline-first Android app for Algerian farmers: irrigation water-dose
calculator (crop, soil, surface, flow rate), watering schedule planner and
parcel register. French + Arabic (RTL). 100 % offline, no account.

## How it works

`ETc = Kc × ETo` (FAO-56) → net dose → gross dose (method efficiency) →
volume (`1 mm/ha = 10 m³`) → pump time (`m³ ÷ L/s × 3.6`) →
interval from soil storage (`TAW × roots × p ÷ ETc`).

## Build

Cloud-built by GitHub Actions (`.github/workflows/android-build.yml`):
unit tests → lint → `assembleDebug`, APK uploaded as artifact and attached
to the `debug-latest` release. Latest APK: see **Releases**.

## Limitations (honest)

- Kc/ETo/soil tables are rounded FAO estimates, not calibrated to your
  wilaya — validate one season with a local technician (DSA/SDA).
- No weather feed (offline by design); ETo presets are hot-season values.
- Season need is an order of magnitude, not a water right.
- French default; Arabic covers app strings (numbers stay Latin digits v1).
- minSdk 24 (Android 7.0+).

## Costs

- Development: local + free tier (GitHub Actions ubuntu-latest, ~5 min/build).
- Running costs: **0** — no server, no database, no API keys, no account.
- Distribution: APK via GitHub Releases (free) / Play Store later ($25 once).

## First 10 customers (plan)

1. 2 potato farmers, El Oued — dose accuracy vs. their pivot habit.
2. 2 greenhouse tomato growers, Biskra — drip schedules.
3. 1 date-palm grower, Adrar/Touggourt — perennial intervals.
4. 2 cereal farmers, Sétif/Hauts Plateaux — supplemental irrigation.
5. 1 DSA (agriculture services) technician — expert validation + stamp.
6. 1 irrigation-equipment dealer — pre-installed demo + feedback channel.
7. 1 agricultural vocational school (ITMA) class — 20 students, usability.

Channel: on-site install of the APK (Bluetooth/share), WhatsApp group for
feedback, Friday souk demos. Success metric: 7/10 still using it after
30 days; target dose error < 20 % vs. technician recommendation.

## Docs

- `docs/RESEARCH.md` — agronomy sources & Algerian context
- `docs/SPEC.md` — MVP scope & architecture
- `docs/TEST_TABLE.md` — unit-test table & CI

License: MIT.
