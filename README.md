# Pogo-Account-Checker
## Information
Pogo Account Checker (PAC) is an Android app that checks if Pokémon Go PTC accounts are banned or not. It does so by using the Pokémon Go app, no third party APIs that can put your accounts at risk are used. PAC can also retrieve account data such as level, experience, stardust, Pokémon data, and item bag data.
* [Discord](https://discord.gg/sNv8sPr) - For support
## Requirements
* Rooted Android device with Android 5.0 or higher and the language set to english.
* Pokémon Go installed.
## Setup
### Rooting
1. Ensure your phone has an unlocked bootloader and is able to support root. [LineageOS](https://lineageos.org/) is a good place to start for a custom ROM and they have good installation instruction for each device.
2. Install [Magisk](https://www.xda-developers.com/how-to-install-magisk/) (v20.3 or newer) to root the phone via recovery. Repackage the MagiskManager App and add Pokémon Go to Magisk Hide. Make sure to delete the folder `/sdcard/MagiskManager` after repackaging. It's necessary to pass the Safetynet check to run Pokémon Go on rooted phones. Check the Safetynet status in the MagiskManager App.
### APK
You can get the APK by directly downloading it from Github [here](https://github.com/pogo-account-checker/Pogo-Account-Checker/blob/master/apk/pogo-account-checker.apk), or by building it yourself. You will have to take some additional steps if you want to build the APK yourself:
1. Install the latest version of [Android Studio](https://developer.android.com/studio/).
2. `git clone` and open the project in Android Studio.
3. The application uses the [ML Kit's text recognition APIs](https://firebase.google.com/docs/ml-kit/recognize-text). In order for the text recognition to work a `google-services.json` file must be downloaded and placed in the `app` folder. This file contains confidential information, hence it's not added to this repo. [Here](https://firebase.google.com/docs/android/setup) is explained how to obtain this file.
4. Install the app on your phone by clicking the green play button in Android Studio.
## Checking accounts
First you have to put your PTC accounts in a `.txt` file, username and password should be seperated by a delimiter of choice. The delimiter can be chosen in the settings menu. Make sure to place each account on a new line. `.txt` file example:
```txt
username01,password01
username02,password02
```
Then transfer the file to your phone. Next open the app, press the `set accounts` button, grant root permission forever, and select the file. Press start to start checking the accounts. The checked accounts can be found in the `PogoAccountChecker` folder on your phone. In this folder you can find the following files:
* `not_banned.txt` - contains accounts that still work.
* `not_banned_L{lvl}.txt` - contains accounts with level `lvl` that still work. Check `Detect account level` in settings to enable level detection.
* `tutorial.txt` - contains unbanned accounts that still need to complete the tutorial. Check `Check for tutorial` in settings for tutorial detection.
* `tutorial_L{lvl}.txt` - contains unbanned accounts with level `lvl` that still need to complete the tutorial.
* `banned.txt` - contains accounts that are banned.
* `temp_banned.txt` - contains accounts that are temporarily banned.
* `wrong_credentials.txt` - contains accounts that don't exist or have a wrong username/password.
* `new.txt` - contains accounts that have never been logged in to Pokémon Go.
* `not_activated.txt` - contains accounts that have not been activated via the activation email.
* `locked.txt` - contains accounts that are locked. Locked accounts can't be accessed untill their password gets changed.
* `error.txt` - contains accounts for which checking failed 10 times in a row, this can for example happen when there is no internet connection.
## Retrieving additional data with PogoDroid
You can use [MAD's](https://github.com/Map-A-Droid/MAD) PogoDroid to get additional information such as experience, stardust, Pokémon data, and item bag data. PogoDroid is a MITM app used to extract data from PoGo for maps.\
Setup PogoDroid:
1. Install PogoDroid and setup account, you can find more information [here](https://mad-docs.readthedocs.io/en/latest/device-setup/#pogodroid).
2. Disable `Send selected set of serialized data (json)` (PogoDroid -> settings -> External communication).
3. Enable `Send raw data (base64 encoded)` (PogoDroid -> settings -> External communication).
4. Disable `GZIP the raw data that is to be posted.` (PogoDroid -> settings -> External communication).
5. Set `RAW POST Destination` to `http://127.0.0.1:8080/raw` (PogoDroid -> settings -> External communication).
6. Set `Injection delay` to 5 (PogoDroid -> settings -> App).
7. **Important:** Disable full screen notifications from PogoDroid in the Android settings.
8. Optional: enable `Injection detection` (PogoDroid -> settings -> App) if you experience PoGo crashes after PogoDroid has injected.
