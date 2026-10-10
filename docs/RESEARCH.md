# IrrigationDZ — Research notes

Date: 2026-10-10. All values below are published FAO reference data, rounded;
the app stores them as documented field estimates, not calibrated advice.

## 1. Algerian context (verified)

- Irrigated area ≈ 1.38 M ha; total irrigation water need ≈ 8.1 billion m³/yr,
  ~2/3 from groundwater (open-access study using FAO CROPWAT 8.0, 2026).
- Winter cereals (wheat, staple food): ETc 1–2.6 mm/day sub-humid north,
  2.14–9.43 mm/day hyper-arid south; Kc 0.78–1.04 / 0.50–1.35 by zone
  (Laaboudi et al., IJAPR 2015, lysimeter study, 5 Algerian stations).
- Potato (major crop, e.g. El Oued): 120–150 day season needs 500–700 mm;
  Kc 0.4–0.5 ini / 0.7–0.8 dev / 1.05–1.2 mid / 0.7–0.95 late; keep depletion
  ≤ 30–50 %; 70 % of uptake from top 0.3 m (FAO Land & Water, potato page).
- Core formula (FAO Irrigation & Drainage Paper 24, Ch. 3):
  `ETc = Kc × ETo`, with Kc per growth stage (ini/dev/mid/late).
- Conversion used everywhere: **1 mm over 1 ha = 10 m³**.
- Indicative application efficiencies: drip ≈ 0.90, sprinkler ≈ 0.75,
  surface/gravity ≈ 0.60 (FAO field ranges).
- Soil TAW (total available water, mm/m): sand ≈ 70, sandy loam ≈ 120,
  loam ≈ 170, clay ≈ 200 (FAO soil-water tables, rounded).
- Scheduling rule: `interval = RAW / ETc`, `RAW = TAW × rootDepth × p`
  (p = depletion fraction; potato 0.35, shallow veg 0.3–0.4, cereals 0.55).

## 2. User need (assumptions, to validate with field test)

- Smallholders in Biskra, El Oued, Adrar, Hauts Plateaux: intermittent
  connectivity → 100 % offline is the top requirement.
- Language: French + Arabic (RTL). No account, no signup — farmers will not
  create accounts for a calculator.
- Inputs a farmer actually knows: crop, soil type (by feel), field size (ha),
  pump/well flow (L/s), irrigation method, climate zone. ETo override kept
  optional with sensible zone presets.
- Competitors: FAO CROPWAT (desktop, expert), assorted online calculators
  (need internet, English). Gap = offline mobile FR/AR dose + schedule.

## Sources

- FAO Irrigation & Drainage Paper 24, "Crop Water Requirements"
  (https://www.fao.org/4/s8376e/s8376e.pdf)
- FAO Training Manual 3, Ch. 3 "Crop water needs"
  (https://www.fao.org/4/s2022e/s2022e07.htm)
- FAO Land & Water — Potato crop information
  (https://www.fao.org/land-water/databases-and-software/crop-information/potato/ar/)
- Laaboudi et al. 2015, wheat Kc/ETo across Algerian climate zones
  (https://journalissues.org/wp-archive/ijapr/abstract/laaboudi-et-al-august-2015/)
- Agricultural Water Management under Water Scarcity in Algeria (2026,
  CROPWAT-based national estimates)
  (https://openagriculturejournal.com/VOLUME/20/ELOCATOR/e18743315442659/FULLTEXT/)
