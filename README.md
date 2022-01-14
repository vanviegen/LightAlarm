## LightAlarm
<img src="screenshot.png" align="right" width="300px">

#### Android night clock and wake-up light

LightAlarm is an Android app that does exactly this:

1. When I throw my phone in the wireless charger (which is on my bedside table), it automatically starts the app, turning on the screen and bypassing the lock screen.
2. The app normally shows the current time and the time until the next alarm (presumably set using the phone's Clock app) on a black background. This is intended for AMOLED screens.
3. Half an hour before the alarm rings, the screen will start to brighten, going from black to dim deep red, to very bright yellow. Ideally, this gently wakes me up before the (less gentle) ringer does.

That's it. There are no settings, this is just meant for me (and perhaps for people with similar preferences).

Unfortunately, feature #1 requires the use of deprecated Android APIs (dating back to the time that API calls were commands rather than mere suggestions to the operating system). Therefore, the app is not allowed in the Play Store. [Sideloading](https://github.com/vanviegen/LightAlarm/releases/download/dev/app-release-unsigned.apk) to the rescue!

