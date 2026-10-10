# DZ-Apps 2026 — Shared Design Direction

Single design language for all 20 DZ apps. Defined as tokens below so it can be
built once as a **Penpot library** ("DZ-Apps 2026") and implemented 1:1 in
Kotlin/Compose Material 3. Offline-first, French/Arabic, phone-first.

## 1. Color

| Role | Light | Dark | Penpot token |
|---|---|---|---|
| primary (app-tinted) | app seed, e.g. `#2E7D32` | `primary-80` | `primary-40` / `primary-80` |
| onPrimary | `#FFFFFF` | seed `primary-20` | — |
| primaryContainer | `primary-90` | `primary-30` | `primary-90` / `primary-30` |
| secondary (water blue `#0277BD` family) | `secondary-40` | `secondary-80` | `secondary-40` / `secondary-80` |
| surface | neutral `#F7F9F4`-ish | `neutral-10` | `surface` / `surface-dark` |
| error | M3 default | M3 default | — |

Rules:
- Android 12+: use **dynamic color** (`dynamicLight/DarkColorScheme`); static
  seed schemes above are the fallback below API 31.
- Dark mode: **system-follow**, no in-app toggle (less code, fewer bugs).
- Contrast: body text on surface ≥ 4.5:1; verify with Penpot's contrast plugin.

## 2. Typography (M3 defaults, no custom font = smaller APK, offline-safe)

Screen title `titleLarge` · card title `titleMedium` · results `bodyLarge` ·
labels `bodyMedium` · disclaimer/footnote `bodySmall`. System font only
(Arabic glyphs guaranteed on all devices; no font download = offline-safe).

## 3. Shape & spacing

- Corners: cards 12dp (`corner/medium`), dialogs 28dp (`corner/xl`), FAB default.
- Spacing scale: 4 / 8 / 12 / 16 / 24 dp. Screen padding 16dp, card content 12–16dp.
- Touch targets ≥ 48dp (IconButton/FAB/NavBar are compliant by default).

## 4. Layout (adaptive, edge-to-edge)

- `enableEdgeToEdge()` + Scaffold content padding; never hard-code status-bar insets.
- Content column capped at **840dp**, centered (phone full-bleed, tablet readable).
- Width ≥ 840dp (tablet/landscape): form + result **side-by-side** (`Row`,
  16dp gutter); below that single scrollable column.
- Bottom nav: 2–4 destinations max, **always with icon + label**, icons carry
  `contentDescription` = label (TalkBack).

## 5. FR / AR RTL rules

- `android:supportsRtl="true"`; use `Arrangement.SpaceBetween`, `weight()`,
  padding — never `left/right`, always `start/end`.
- Mirror rule: navigation, progress and media direction flips automatically;
  **numbers, units (m³, ha, h), formulas and clock times stay LTR**.
- Per-app language switch via `AppCompatDelegate.setApplicationLocales(fr/ar)`;
  locale lives outside the theme so switching never loses state.

## 6. Accessibility checklist (per screen)

1. TalkBack pass: every icon has a description; result cards read as sentences.
2. Decimal inputs use `KeyboardType.Decimal`; input filter accepts `,` and `.`.
3. No color-only meaning: interval/volume always paired with text + unit.
4. Min text scale 1.0 → 1.3: layouts scroll, no fixed heights on text.

## 7. Penpot ↔ Compose mapping

| Penpot | Compose |
|---|---|
| Library "DZ-Apps 2026": color tokens, 4/8/12/16/24 spacing, 12/28 radii | `ui/Theme.kt` (`IrrigationDzTheme`) |
| Components: AppBar, BottomNav, DZCard, DZDropdown, DZNumberField, ResultRow | `MainActivity.kt` composables (`DoseCard`, `DropDown`, `NumberField`, `InputForm`) |
| Board "Phone 360×640" + "Tablet 840×1180", light + dark variants | `BoxWithConstraints(maxWidth ≥ 840.dp)` branches |
| Prototype flows: Dose → Parcels → Plan | bottom-nav tabs 0/1/2 |

> Note: screens were implemented directly from this spec in code (no Penpot
> web access in the build environment). Before pixel-polish, recreate the
> boards above in Penpot and attach screenshots/links here.
