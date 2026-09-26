# Even Better Moon and Sun (Gameoverse fork)

Fork of [Even Better Moon and Sun](https://modrinth.com/mod/even-better-moon-sun)
1.6 for Fabric 26.1 by arnav (Modrinth: ar_tyyuu), MIT. Client-only. The original
has no public source repository, so the first commit is the Vineflower decompile of
the Modrinth jar (`even-better-moon-sun-1.6.jar`), unmodified; every Gameoverse
change is a readable diff on top of it. Local-only git, not published.

The original drives everything from the world's tick count: a 29-day moon and a
365-day year of 20-minute days (about 5 real days). This fork follows the real
calendar instead, using `gameoverse-sky-sync` (required):

- **Moon phase**: no override. Sky Sync already drives vanilla's moon phase from the
  real moon on both sides, so the sky matches server mechanics.
- **Sun tilt**: the real solar declination for today, on the sun only (the original
  tilted the moon and stars with it).
- **Moon position**: follows the real moon instead of sitting opposite the sun. It
  trails the sun by its elongation (a new moon rises with the sun, first quarter at
  noon, full moon at sunset, last quarter at midnight, about 50 minutes later each
  real day), stays up as long as its own declination allows at the server's latitude,
  and its arc tilts by that declination (a winter full moon rides high, a summer one
  low). Shader packs that draw their own moon may still put it opposite the sun.
- **Year, seasons and the lunar day** for events and `/astro`: the real date,
  with the year starting at the March equinox. The time of day for night-only
  events is the world's, warped to today's daylight length like the sun.
- **Blood moon and harvest moon tints**: off by default (`bloodMoonInterval: 0`
  now also turns off the harvest moon), since Enhanced Celestials runs the real
  Blood and Harvest Moon events here.
- **`/year`**: fixed to the real calendar.
- Aurora animation uses game time instead of the large real-day tick value.

- **Eclipses**: the original's 174-day timer (a black overlay over the whole screen,
  HUD included) is gone. Solar and lunar eclipses happen when the real sun and moon
  line up (maths in `gameoverse-sky-sync`'s `Astronomy`, checked against every
  2026-27 eclipse). During a solar eclipse the sun fades as it's covered and the sky
  darkens, with stars out at totality, timed as seen from the eclipse's centre line;
  during a lunar eclipse the moon dims in Earth's penumbra and turns coppery red in
  its umbra. `/eclipse` and `/astro` show the next real ones.

Meteor showers (every 14 days) and auroras (peaking at the equinoxes) keep their
original intervals, counted in real days.

## Build

Build `../gameoverse-sky-sync` first (compiled against its jar), then
`./gradlew build`. Copy `build/libs/even-better-moon-sun-gameoverse-<version>.jar`
to `fabric 26.1/automodpack/host-modpack/main/mods/` (client-only set).

The original's F3 lines are dropped: its `DebugScreenMixin` targets
`DebugScreenOverlay.getGameInformation`, which 26.1.2 no longer has (F3 moved to
debug entries), and the failed injection crashed the client at startup. The
original 1.6 jar crashes the same way on 26.1.2. `/astro` shows the same info.

The original's sun-tilt mixin targets `PoseStack;pushPose:()V`, a stray colon that
isn't valid descriptor syntax (it's in the Modrinth jar too, not a decompiler
artifact). Mixin rejected it, SkyRenderer failed to transform, and the client showed
only a black screen from the main menu on. Fixed to `pushPose()V` in 1.6+gameoverse.3.
