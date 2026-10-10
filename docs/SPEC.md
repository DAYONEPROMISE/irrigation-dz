# IrrigationDZ — MVP spec (v1.0.0)

## Scope (SELECT)

IN: dose calculator (crop × soil × area × flow × method × zone/ETo × stage),
parcel register (Room, offline CRUD), 14-day watering planner per parcel,
FR/AR with RTL, no account, no network.
OUT: weather API, maps/GPS, cloud sync, notifications, multi-user, iOS.

## Architecture

- `data/Agronomy.kt` — pure Kotlin engine, zero Android deps (unit-tested).
- `data/Parcel.kt` — Room entity/DAO/database (parcels table, v1).
- `data/ParcelRepository.kt` — DAO wrapper + dose computation per parcel.
- `MainActivity.kt` — Jetpack Compose + Material 3, 3 tabs
  (Dose / Parcelles / Planning), per-app locale switch (AppCompat FR↔AR).
- `res/values/strings.xml` (FR default) + `res/values-ar/strings.xml` (AR).
- minSdk 24 / targetSdk 34 / AGP 8.6.1 / Kotlin 1.9.25 (proven template).
- 100 % offline: no permissions, no network calls.

## Validation rules

- area > 0, flow > 0 required before computing; ETo clamped 0–15;
- efficiency fixed per method (0.90 / 0.75 / 0.60); net dose floored at 0;
- interval clamped 1–30 days; season need labelled "ordre de grandeur".
