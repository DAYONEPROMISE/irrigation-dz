# OpenPencil pass — irrigation-dz

## Design (Shared/OpenPencil/irrigation-dz/)
- `design.html` (parcel card + dose planner + 3-tab nav, DZ tokens) → `design.fig`
- `lint --preset accessibility`: 0 errors, 0 warnings

## Code review (no rewrite needed)
Nav/FAB icons already use `stringResource` descriptions. No code change.

## APKs
`android-build.yml` adds `assembleRelease` (unsigned) + `app-release` artifact + a versioned
`release-v*-b*` GitHub Release for the release APK (debug flow unchanged).
