# 2022-04-?? Version 75.3.0 (97)

* Add Brave Beta and Brave Nightly

# 2022-04-17 Version 75.2.1 (96)

* Fix the "can't install a new app" bug
* Add Russian translation for the changelog (thanks Nickoriginal - Tobi823/ffupdater#127)

# 2022-04-10 Version 75.2.0 (95)

* If FFUpdater is installed from the GitHub/GitLab/Repository, it can update itself.
* Overhauled settings menu with more control over network and background activities.
* More background network retries to avoid unnecessary error messages

# 2022-04-08 Version 75.1.0 (94)

* Add Czech translation (thanks Juraj Motuz - Tobi823/ffupdater#122)
* Replace the Android DownloadManager with the library OkHttp

# 2022-03-18 Version 75.0.2 (93)
* Add Italian translation (thanks Giovanni - Tobi823/ffupdater#111)
* Update Bulgarian translation (thanks StoyanDimitrov - Tobi823/ffupdater#119)
* Update Russian translation (thanks Nickoriginal - Tobi823/ffupdater#116)
* Update Brazilian Portuguese translation (thanks Ghost and ms - Tobiwan/ffupdater#98)
* Update dependencies

# 2022-03-01 Version 75.0.1 (92)
* Adapt Firefox Klar and Firefox Focus to the new file names in the Github repository (fix missed Focus/Klar updates)

# 2022-01-02 Version 75.0.0 (91)

* Remove Styx because it is not longer maintained (Tobi823/ffupdater#101).
* Improve version comparison with G00fY2/version-compare (Tobi823/ffupdater#98).
* Fix opt-out of apps from background update check ("Excluded applications") for Firefox Release, Firefox Beta and Firefox Nightly (Tobi823/ffupdater#97).
* Don't crash during app installation/update when no or only very little storage is available (Tobi823/ffupdater#96).
* When app update is unsuccessful, show more detailed error message.
* Update dependencies

# 2021-12-05 Version 74.5.3 (90)

* Fix Brave update check

# 2021-11-08 Version 74.5.2 (89)

* Update warning for Vivaldi (thanks gnuhead-chieb - Tobi823/ffupdater#92)
* Make links in dialogs clickable

# 2021-11-07 Version 74.5.1 (88)

* Add Vivaldi (Tobiwan/ffupdater#89)
* Add Ungoogled Chromium (Tobi823/ffupdater#50)
* Update description of Firefox Klar (thanks cosify - Tobi823/ffupdater#91)
* Crash report can partially selected and copied
* Cleanup build scripts

# 2021-11-01 Version 74.5.0 (87)

* Improve internet check. (Tobi823/ffupdater#87)
  * Use the old network check API for Android 10 and below.
  * Handle network errors better in the app.
* Reduce storage usage for devices with low internal memory. (Tobiwan/ffupdater#88)
  * If an app update fails, keep the cached app update (APK file).
  * If an app update is successful or a critical error (wrong certificate fingerprint) occurs, delete the
    cached app update (APK file).
  * Download and keep the APK file in the Android/data/de.marmaro.krt.ffupdater/files/Download folder and use this folder as a cache folder.
  * Do not copy the downloaded APK file to a official cache folder which is invisible to the user.
  * User can delete the downloaded app updates (APK files) in the Android app settings > Storage > Manage
    Space.
  * If an app update fails, show buttons for:
    * deleting the app update in the cache folder.
    * opening the cache folder with a file manager app.
  * When migrating to this version, the app will delete old cache files.
* Update dependencies:
  * androidx.core:core-ktx to 1.7.0
  * androidx.lifecycle:lifecycle-runtime-ktx to 2.4.0

# 2021-10-20 Version 74.4.7 (86)

* Fix "Permission Denial" bug on Android devices (Tobiwan/ffupdater#86)
* Speed-up app installation/update by not requiring to press a button
* Improve crash report styling and add important device information to the crash report
* Improve internal condition checks and add improved error messages
* Remove unused dependencies androidx.fragment:fragment-ktx and org.jetbrains.kotlin:kotlin-stdlib-jdk7

# 2021-10-25 Version 74.4.6 (85)

* Download only "Firefox Focus/Klar" and not "Firefox Focus/Klar Beta" (Tobi823/ffupdater#89)
* Try out the Android 12 "silent update" feature when user manual updates an app (Tobi823/ffupdater#88)

# 2021-10-07 Version 74.4.5 (84)

* Small mistake, I forget to add important changes to 74.4.4
* Improve handling of Kotlin coroutine errors (Tobiwan/ffupdater#85 Tobi823/ffupdater#86)
* Improve and speed up internet check
* Handle disabled "Download Manager" better
* Improve translation
* Update dependencies

# 2021-10-07 Version 74.4.4 (83)

* Improve handling of Kotlin coroutine errors (Tobiwan/ffupdater#85 Tobi823/ffupdater#86)
* Improve and speed up internet check
* Handle disabled "Download Manager" better
* Improve translation
* Update dependencies

# 2021-09-29 Version 74.4.3 (82)

* Improve handling of concurrent code
* Fix crash when starting app (NetworkOnMainThreadException) ()
* Fix missing icon (in repomaker and maybe F-Droid)
* Improve translation files
* Cleanup and refactor code

# Version 74.4.2 (81)

* Skip version

# 2021-09-24 Version 74.4.1 (80)

* Fix crash when canceling a download (Tobi823/ffupdater#77)
* Ignore error and retry background update (with exponential increasing waiting time) for
  CancellationException, GithubRateLimitExceededException and UnknownHostException (Tobi823/ffupdater#74)
* Improve error handling
* Fix download error for Focus/Klar by switching to the GitHub API (Tobiwan/ffupdater#81)

# 2021-08-30 Version 74.4.0 (79)

* Update Brazilian Portuguese translation (thanks mezysinc; Tobiwan/ffupdater#78)
* Abort background update check if airplane mode is enabled (Tobi823/ffupdater#74)
* Prevent simultaneous download by the background job and the user (Tobiwan/ffupdater#80)
* Reduce app permission (remove WRITE_EXTERNAL_STORAGE permission)

# 2021-08-04 Version 74.3.5 (78)

* Fix update check for "Firefox Focus" and "Firefox Klar" (By removing everything after the dash + the dash
  itself from the version name of the installed app. "8.18.0-rc.1" will be converted to
  "8.18.0"; Tobi823/ffupdater#69)

# 2021-07-30 Version 74.3.4 (77)

* Make update check more robust for "Firefox Release", "Firefox Beta", "Firefox Focus" and "Firefox Klar"

# 2021-07-26 Version 74.3.3 (76)

* Fix crash when checking for "Firefox Focus" or "Firefox Klar"
* Update Firefox Focus/Klar icon

# 2021-07-22 Version 74.3.2 (75)

* Fix crash when checking for "Firefox Beta" (Tobi823/ffupdater#60)

# 2021-07-19 Version 74.3.1 (74)

* Fix crash when checking for "Firefox Release" or "Firefox Beta" (Tobi823/ffupdater#57)

# 2021-07-06 Version 74.3.0 (73)

* Add Polish language (thanks Eryk Michalak, Tobiwan/ffupdater_gitlab#11)
* Fix crash when checking for "Firefox Release" updates (Tobi823/ffupdater#52)

# 2021-06-22 Version 74.2.0 (72)

* Add Japanese language (thanks gnuhead-chieb Tobi823/ffupdater#47)

# 2021-06-03 Version 74.1.1 (71)

* Fix Firefox Beta by adapting to the new structure of the chain_of_trust.log (Tobi823/ffupdater#44)

# 2021-05-29 Version 74.1.0 (70)

* Add Brave Browser because the APK files are published on GitHub again. (https://github.com/brave/brave-browser/issues/15878)

# 2021-05-18 Version 74.0.0 (69)

* Remove Brave Browser because the APK files are no longer published on GitHub. (https://github.com/brave/brave-browser/issues/15878)

# 2021-05-18 Version 73.1.4 (68)

* Instruct user if app installation is aborted by "MIUI Optimization" (Tobi823/ffupdater#41)

# 2021-05-07 Version 73.1.3 (67)

* Fix crash caused by the new Firefox Nightly version name schema (Tobi823/ffupdater#40 Tobi823/ffupdater#66)

# 2021-04-25 Version 73.1.2 (66)

* If it is likely that it is just a network error during manual update search, then display "No network
  connection" instead of crashing (Tobi823/ffupdater#38)

# 2021-04-18 Version 73.1.1 (65)

* Fix wrong warning for Bromite (thanks mpeter, Tobiwan/ffupdater#64)

# 2021-04-18 Version 73.1.0 (64)

* Trust user certificates when checking for updates and downloading updates (for AdGuard, Tobi823/ffupdater#37)
* Prevent automatic backup of failed downloads by Google's "Auto Backup for Apps"

# 2021-04-17 Version 73.0.1 (63)

* There has been a discussion on Github if the Kiwi browser is spyware: https://github.com/Tobi823/ffupdater/issues/35 (Tobi823/ffupdater#35)
  I think that Kiwi is not spyware but stays removed because FFUpdater is about privacy and Kiwi has no
  additional privacy features. F-Droid users can use other stores (like Aurora Store) to install the Kiwi
  browser.
* Bug fix: automatically download app updates after disabling airplane mode
* Bug fix: catch the JobCancellationException correctly (occurs when changing the network during background
  update check)

# 2021-04-17 Version 73.0.0 (62)

* Remove Kiwi Browser because it is at least not privacy friendly and in the worst case a spyware (thanks
  nyanpasu64 Tobi823/ffupdater#35)

# 2021-04-13 Version 72.1.0 (61)

* Add support for Bromite https://github.com/bromite/bromite (Tobiwan/ffupdater#59 Tobi823/ffupdater#22)
* Add support for Kiwi Browser https://github.com/kiwibrowser/src (Tobi823/ffupdater#22)
* Automatically download app updates in the background if the current network is unmetered and the device has
  enough storage (Tobi823/ffupdater#34)
* Cache already downloaded updates (Tobi823/ffupdater#33)
* Keep the last version of an app in the folder "/sdcard/Android/data/de.marmaro.krt.ffupdater/cache/Download"
  for manual downgrading. These cached versions can be deleted by using the "CLEAR CACHE" button in the
  settings (Tobiwan/ffupdater#62)
* Decrease the number of false positives "background network exception" error notifications (thanks
  bershanskiy Tobi823/ffupdater#31)
* Improve Brazilian Portuguese translation (thanks mezysinc; Tobiwan/ffupdater#58)
* Improve Russian translation (thanks DeenHyper74; Tobiwan/ffupdater#56)
* Make download status translatable (Tobiwan/ffupdater#57)
* Ask for confirmation when the user wants to update an app but the latest app version is already installed (
  Tobiwan/ffupdater#60)
* Generate UI partially programmatically (and don't use a static UI)

# 2021-03-14 Version 72.0.0 (60)

* !!! Remove support for Firefox Light because updates are no longer signed and therefore pose a security risk (thanks opened and mega-stoffel)
* User can disable the background update check on metered networks (thanks williamtheaker)
* By clicking on the "i"-Icon, you can see the time of the last successful background update check.
* Fix Brazilian Portuguese translation (thanks mezysinc)
* Fix Bulgarian translation (thanks StoyanDimitrov)
* Fix spelling (thanks ku)
* Fix crash when rotating device (thanks floringolintchi)
* Distinguish the morning and evening version of Firefox Nightly (thanks DctrBnsttr)

# 2021-02-26 Version 71.0.3 (59)

* Fix crash when installing app on Android 8 (thanks bershanskiy)
* Show the correct notification if the background check failed due to a network exception (thanks Average_User
  and Diridibindy)
* If it's likely that the user has enabled 'MIUI Optimization', instruct him to disable it (thanks Rafa ML)

# 2021-02-21 Version 71.0.2 (58)

* Fix double download (thanks Redpillbug)
* Fix crash when rotating in the settings view (thanks DeenHyper74)

# 2021-02-18 Version 71.0.1 (57)

* Improve error message when the background update check failed (thanks duck-rh)

# 2021-02-15 Version 71.0.0 (56)

* Add Iceraven browser
* Display the real available versions for Release, Beta, Nightly, Focus and Klar
* Reduce likeliness of background errors
* Fix installation problems on older devices
* Migrate from Java to Kotlin for better concurrency
* Thanks StoyanDimitrov for updating the Bulgarian translation
* Bug fixes and many more
* Thanks mega-stoffel, Iey4iej3, Redpillbug, NANASHI0X74, StoyanDimitrov, lucker999, codingepaduli, borisovg,
  H-Sachse, mpeter, DeenHyper74, duck-rh, mikeklem and darkludao for bug reports
* Thanks CharmCityCrab, TheOneWithTheBraid, codingepaduli and User1l0 for feature requests

# 2020-11-13 Version 70.0.1 (55)

* Check if system download app is installed (thanks Quantumrider)
* Fix crash during downloading (thanks hsol)

# 2020-11-02 Version 70.0.0 (54)

* Add Bulgarian translation (thanks StoyanDimitrov)
* Fix short description in F-Droid (thanks linsui)
* Fix typo (thanks GPery and DeenHyper74)
* A different notification for each installed app will be displayed
* Clicking on notification will update the app
* Better detect ABI of device - Firefox Focus can be installed on Android emulators
* Use Crasher (https://github.com/fennifith/Crasher) for crash reports
* Delete old downloaded APK files more reliable
* Drop permission READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE because they should not be necessary
* Check and fail if external storage is not available
* Query GitHub API with less network traffic
* Cleanup code

# 2020-09-23 Version 69.0.5 (53)

* Disable error when the already installed app has a different signature (because Android won't install an
  update with a different signature) - thanks pheki for reporting this bug
* Add translations for this bug
* Make the installation activity a little big more resilient

# 2020-09-08 Version 69.0.4 (52)

* Fix old download urls / update check urls for Firefox Release and Firefox Beta - thanks DctrBnsttr for
  reporting this bug
* Add tests to ensure that FFUpdater is always using the latest download urls

# 2020-08-17 Version 69.0.3 (51)

* Fix broken Firefox Nightly installation/update - thanks 132ikl for reporting this bug

# 2020-08-13 Version 69.0.2 (50)

* Thanks aevw for adding brazilian portuguese translation

# 2020-08-12 Version 69.0.1 (49)

* Thanks DeenHyper74 for updating the russian translation

# 2020-08-05 Version 69.0.0 (48)

* Remove Fennec Release because it's no longer supported by Mozilla
* Rename Fenix Release, Fenix Beta and Fenix Nightly to Firefox Release, Firefox Beta, Firefox Nightly
* Fix download URLs for Firefox Release, Firefox Beta, Firefox Nightly
* Use the more reliable PackageInstaller-method for installing the apps (instead of the old
  ACTION_INSTALL_PACKAGE-method)
* Increase minimum SDK for FFUpdater to Lollipop/21 (because PackageInstaller needs 21 and all Firefox
  browsers need at least 21)
* Fix bug "empty installed text field"
* Thanks trymeout, guysoft, rantpalas and RomainL972 for reporting bugs

# 2020-07-02 Version 68.4.1 (47)

* Fix broken Fenix download - if the download is still broken for you, wait 10 minutes or delete the storage
  of the app
* Check for enough free space and display warning if < 100MB

# 2020-06-07 Version 68.4.0 (46)

* Add Fenix Beta, Fenix Nightly and Lockwise
* Download Fenix Release/Beta/Nightly, Focus and Klar from Mozilla's Taskcluster (continuous integration
  server)
* Fix "Light theme is always shown at first run"
* Thanks Rail Aliiev and Johan Lorenzo from Mozilla for their support <https://bugzilla.mozilla.org/show_bug.cgi?id=1627518>
* Thanks KarlHeinz and DeenHyper74 for their error reporting and support

# 2020-05-20 Version 68.3.7 (42)

* Add simple crash reporter (by opening the mail app with the error message)
* Fix crash by asking for WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE permissions
* Show progress bar when verifying the downloaded APK
* Download APK to the public download directory of the app (for example:
  /storage/sdcard0/Android/data/de.marmaro.krt.ffupdater/files/Download)
* Remove old debug messages
* Thanks yhoyhoj, UltraBlackLinux, rvandegrift, vikajon, wchen342, Ulfschaper, prox and danceswithcats for
  your error reporting

# 2020-05-18 Version 68.3 (35)

* Try to fix error "Failed to check certificate hash" by switching from apksig-library to
  PackageManager#getPackageArchiveInfo (thanks rvandegrift). This will reduce the size of FFUpdater and
  improve the maintenance for future releases.
* Color of collapsed title will be always white (thanks DeenHyper74)

# 2020-05-13 Version 68.2 (34)

* Fix Fenix download from Github (thanks yhoyhoj)
* Show correct download progress when downloading an app

# 2020-05-13 Version 68.1 (33)

* Thanks DeenHyper74 for the Russian translation
* Add support for Dark Theme (thanks DeenHyper74 for the tip)
* Add setting for switching between Dark and Light Theme

# 2020-05-06 Version 68.0 (32)

* Really big update
* Add support for Firefox Klar, Firefox Focus, Firefox Lite and Fenix
* Download and install the app inside FFUpdater (thanks wolfgang42 for the groundwork)
* Improve UI
* Verify the certificate of the downloaded and installed app
* Many improvements
* Thanks DeenHyper74 and xin for translations
* Remove Fennec Beta and Fennec Nightly because their are not developed anymore https://bugzilla.mozilla.org/show_bug.cgi?id=1627518

# 2019-06-28 Version 67.4 (31)

* Fix warning dialogue disappears after screen rotation (thanks DeenHyper74)

# 2019-06-28 Version 67.3 (30)

* Fix crash when selecting an entry after rotating the channel dialog (thanks DeenHyper74)

# 2019-06-28 Version 67.2 (29)

* Fix crash when rotating on channel dialog (thanks DeenHyper74)

# 2019-06-28 Version 67.1 (28)

* Update Russian translation (thanks DeenHyper74)

# 2019-06-28 Version 67.0 (27)

* Fix broken nightly download (thanks dannycolin for the info)
* Display warning when switching from 'Release' channel to the 'Nightly' or 'Beta' channel (thanks DeenHyper74)
* Interval between update checks is now configurable (thanks aplufr, wah6Me1l and DeenHyper74)

# 2019-04-25 Version 66.2 (26)

* Improve french translation (thanks xinxinxinxinxin)
* Fix "update notification will be shown every 5 minutes" (bug discovered by aplufr)

# 2019-04-25 Version 66.1 (25)

* Add grammar fixes (thanks DeenHyper74)

# 2019-04-25 Version 66.0 (24)

* Notification (for a Firefox update) now works on Android 9
* Replace BackgroundService with WorkManager (AndroidX) for requesting Mozilla's API
* Clean up code (thanks DeenHyper74)
* Fix some minor bugs (thanks DeenHyper74)
* App requires API level 18 because Firefox requires API level 18
* Update translation

# 2019-04-01 Version 65.0 (23)

* Added support for beta and nightly channels
* Switched to light theme
* Update russian translation
* Handover maintainership to https://notabug.org/Tobiwan/ffupdater

# 2018-01-07 Version 57.0 (22)

* Add some translations

# 2017-06-13 Version 54.0 (21)

* Add license report
* Update icon
* Add some translations

# 2017-05-20 Version 53.0 (20)

* Update to 53.0
* Use new Mozilla API to det version information
* Remove a lot of unused code.
* Add icon
* Fix crash with SDK < 17
* Enable smaller builds

# 2017-01-22 Version 51.0 (18)

* Update to 51.0
* Remove a lot of unused code.
* Re-implement actual checking.

# 2016-04-29 Version 46.0 (16)

* No changes, just bump to remind people

# 2016-04-06 Version 45.0.1 (15)

* Remove everything but URL generator and download button.

# 2016-03-09 Version 45.0a (14)

* Really, really quickfix Android6 issues..
* Remove version check since it's broken: Just download the APK.

# 2016-03-06 Version 45.0 (13)

* Use lower target to quickfix Android6 permissions

# 2016-01-26 Version 44.0 (12)

* Update to reflect new Firefox version, but no change in architecture. Mozilla "-latest" URL still works...

# 2015-12-31 Version 43.0.x (11)

* Mozilla removed /latest/* downloads from archive. As recommended in
  https://archive.mozilla.org/pub/mobile/releases/latest/README.txt
  we use https://download.mozilla.org/?product=fennec-latest now.

# 2015-12-25 Version 43.0 (10)

* Update to 43.0
* Mozilla does not populate /latest/ anymore, see
  https://bugzilla.mozilla.org/show_bug.cgi?id=1233399

# 2015-12-24 Version 42.0.2 (9)

* Update to 42.0.2

# 2015-11-23 Version 42.0.1 (8)

* Update to 42.0.1

# 2015-11-02 Version 42.0 (7)

* Update to 42.0

# 2015-09-23 Version 41.0 (6)

* Bump to 41.0 (6)

# 2015-09-01 Version 40.0.3 (5)

* Mark background setting as non-functional for now.
* Update to 40.0.3

# 2015-08-10 Version 40.0 (3)

* Remove FTP lookup since Mozilla is shutting down the servers.
* Handle both request type by a single button.
* "I am feeling lucky" now gets the next release, not the latest.
* Add preferences to restrict connections: WiFi-only, metered, roaming.
* Use actionbar.

# 2015-07-08 Version 39.0 (1)

* Select download uri based on arch and api.
* Check filename via FTP.
* Download update file via HTTPS and DownloadManager.
* Log errors and status.
* Toast on updates.
* Prompt for update (if necessary).
* Option to use fixed/tested download location.
* Add proper LICENSE (GPLv3+).