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
### Install Pogo Account Checker
You can get the APK [here](https://github.com/pogo-account-checker/Pogo-Account-Checker/blob/master/apk/PAC.apk).
### Disable Magisk superuser popup
The superuser popup may interfere with the text recognition from PAC. Therefore, it is recommended to disable this popup by setting `Superuser Notification` to None in the Magisk settings.
## Checking accounts
First you have to put your PTC accounts in a `.txt` file, username and password should be seperated by a delimiter of choice. The delimiter can be chosen in the settings menu. Make sure to place each account on a new line. `.txt` file example:
```txt
username01:password01
username02:password02
```
Then transfer the file to your phone. Next open the app, press the `set accounts` button, grant root permission forever, and select the file. Press start to start checking the accounts. The checked accounts can be found in the `PAC` folder on your phone. In this folder you can find the following files:
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
1. Install PogoDroid and setup account, you can find more information [here](https://mad-docs.readthedocs.io/en/latest/device_setup/android_dev/#pogodroid).
2. Disable `Send selected set of serialized data (json)` (PogoDroid -> settings -> External communication).
3. Enable `Send raw data (base64 encoded)` (PogoDroid -> settings -> External communication).
4. Disable `GZIP the raw data that is to be posted.` (PogoDroid -> settings -> External communication).
5. Set `RAW POST Destination` to `http://127.0.0.1:8080/raw` (PogoDroid -> settings -> External communication).
6. Turn on `Disable last sent notifications` (PogoDroid -> settings -> External communication).
7. **Important:** disable full screen notifications from PogoDroid in the Android settings.
8. Optional: enable `Injection detection` (PogoDroid -> settings -> App) if you experience PoGo crashes after PogoDroid has injected.

Below you can find an example of the data you can retrieve.
```
{
  "username":"bob",
  "password":"bob123",
  "status":"notBanned",
  "level":30,
  "playerData":{
    "battleLockoutEndMs":0,
    "hasWarning":false,
    "inGameUsername":"bob",
    "lastTeamChangeTimestamp":0,
    "maxItemStorage":350,
    "maxPokemonStorage":300,
    "pokeCoins":0,
    "remainingNameChanges":3,
    "stardust":780881,
    "startDate":"2017-04-19T17:17:05.817",
    "startTimestamp":1492622225817,
    "suspendedMessageAcknowledged":true,
    "team":1,
    "teamChanges":0,
    "tutorialCompleted":true,
    "warningExpireMs":0,
    "wasSuspended":true,
    "banned":false,
    "nameBlacklisted":false,
    "warningMessageAcknowledged":false
  },
  "playerStats":{
    "battleLeagueStats":{
      "greatLeague":{
        "battlesTotal":0,
        "battlesWon":0
      },
      "ultraLeague":{
        "battlesTotal":0,
        "battlesWon":0
      },
      "masterLeague":{
        "battlesTotal":0,
        "battlesWon":0
      }
    },
    "berriesFed":0,
    "bestBuddies":0,
    "bestFriends":0,
    "bigMagikarpCaught":0,
    "collectionChallengesCompleted":0,
    "eggsHatched":41,
    "eventBadges":[ // HoloBadgeType (https://github.com/Furtif/POGOProtos/blob/master/base/v0.223.x_p_obf.proto#L525)
      5231,
      5233
    ],
    "experience":2441350,
    "gruntsDefeated":0,
    "gymAttacksTotal":0,
    "gymAttacksWon":0,
    "gymDefencesWon":0,
    "gymDefendTimeMs":0,
    "kilometersWalked":6152.178,
    "legendaryRaidsTotal":1,
    "legendaryRaidsWon":1,
    "level":30,
    "nextLevelExperience":2500000,
    "photoBombEncounters":0,
    "playersReferred":0,
    "pokeballThrown":3650,
    "pokemonCaught":1974,
    "pokemonCaughtAtMyLures":0,
    "pokemonCaughtByType":{
      "flying":846,
      "ice":2,
      "normal":1008,
      "fairy":29,
      "steel":2,
      "poison":512,
      "water":183,
      "ground":39,
      "dragon":7,
      "psychic":58,
      "bug":474,
      "dark":250,
      "fire":89,
      "rock":29,
      "fighting":1,
      "ghost":11,
      "electric":12,
      "grass":173
    },
    "pokemonDeployedInGyms":0,
    "pokemonEncountered":1127652,
    "pokemonEvolved":234,
    "pokemonFormChanges":0,
    "pokemonPurified":0,
    "pokestopsScanned":0,
    "pokestopsVisited":6279,
    "previousLevelExperience":2000000,
    "questsCompleted":5,
    "raidsTotal":2,
    "raidsWon":2,
    "raidsWonWithFriends":1,
    "rocketBalloonBattlesTotal":0,
    "rocketBalloonBattlesWon":0,
    "sevenDayStreaks":0,
    "smallRattataCaught":17,
    "timesOnRaidAchievementsScreen":1,
    "totalMegaEvolutions":0,
    "tradeAccumulatedDistance":0,
    "tradesDone":0,
    "trainingBattlesTotal":0,
    "trainingBattlesWon":0,
    "uniqueMegaEvolutions":0,
    "uniquePokedexEntries":134,
    "uniqueRaidBossesDefeated":3,
    "wayfarerAgreements":0
  },
  "activeBuddyData":{
    "internalPokemonId":2104927287868420184,
    "souvenirs":{
      
    },
    "stats":{
      "battles":1,
      "berriesFed":3,
      "kilometersWalked":0.0,
      "newVisits":0,
      "photosTaken":0,
      "pointsEarned":2,
      "timesPlayed":1,
      "totalDaysBuddy":15
    },
    "validationResult":"SUCCESS"
  },
  "buddyHistory":[
    {
      "internalPokemonId":7298435912171532184,
      "lastSetTimestamp":1635610744353,
      "lastUnsetTimestamp":1635615330023,
      "pokemonId":6,
      "souvenirs":{
        
      },
      "stats":{
        "battles":0,
        "berriesFed":0,
        "kilometersWalked":0.0,
        "newVisits":0,
        "photosTaken":0,
        "pointsEarned":0,
        "timesPlayed":0,
        "totalDaysBuddy":0
      }
    }
  ],
  "totalPokemon":177,
  "pokemon":[
    {
      "costumeId":0,
      "cp":883,
      "creationTimestamp":1492717155143,
      "formId":0,
      "gender":2,
      "height":1.1083649,
      "id":185,
      "individualAttack":2,
      "individualDefense":6,
      "individualStamina":12,
      "internalId":2665671451381774279,
      "ivsPercentage":44.444447,
      "level":16,
      "maxStamina":98,
      "move1Id":227,
      "move2Id":31,
      "move3Id":0,
      "stamina":98,
      "weight":38.243694,
      "bad":true,
      "lucky":false,
      "purified":false,
      "shadow":false,
      "shiny":false,
      "name":"Sudowoodo",
      "formName":"",
      "familyId":185,
      "legendary":false,
      "mythical":false,
      "move1Name":"Rock Throw",
      "move2Name":"Earthquake",
      "move3Name":""
    },
    {
      "costumeId":0,
      "cp":1126,
      "creationTimestamp":1492635010624,
      "formId":0,
      "gender":1,
      "height":0.7064952,
      "id":215,
      "individualAttack":12,
      "individualDefense":10,
      "individualStamina":12,
      "internalId":-3700442291440212596,
      "ivsPercentage":75.55556,
      "level":20,
      "maxStamina":94,
      "move1Id":238,
      "move2Id":254,
      "move3Id":0,
      "stamina":94,
      "weight":20.100939,
      "bad":false,
      "lucky":false,
      "purified":false,
      "shadow":false,
      "shiny":false,
      "name":"Sneasel",
      "formName":"",
      "familyId":215,
      "legendary":false,
      "mythical":false,
      "move1Name":"Feint Attack",
      "move2Name":"Avalanche",
      "move3Name":""
    },
    {
      "costumeId":0,
      "cp":530,
      "creationTimestamp":1492712338529,
      "formId":0,
      "gender":1,
      "height":1.657962,
      "id":206,
      "individualAttack":2,
      "individualDefense":2,
      "individualStamina":2,
      "internalId":-5874475902380893727,
      "ivsPercentage":13.333334,
      "level":13,
      "maxStamina":109,
      "move1Id":263,
      "move2Id":26,
      "move3Id":0,
      "stamina":109,
      "weight":17.474646,
      "bad":true,
      "lucky":false,
      "purified":false,
      "shadow":false,
      "shiny":false,
      "name":"Dunsparce",
      "formName":"",
      "familyId":206,
      "legendary":false,
      "mythical":false,
      "move1Name":"Astonish",
      "move2Name":"Dig",
      "move3Name":""
    }
  ],
  "candyData":{
    "165":{
      "candyCount":4,
      "xlCandyCount":0
    },
    "223":{
      "candyCount":27,
      "xlCandyCount":0
    },
    "190":{
      "candyCount":25,
      "xlCandyCount":0
    }
  },
  "megaEnergyData":{
    "6":50
  },
  "totalItems":374,
  "items":{
    "1104":{
      "count":1,
      "name":"Dragon Scale"
    },
    "701":{
      "count":49,
      "name":"Razz Berry"
    },
    "901":{
      "count":1,
      "name":"Unlimited Incubator"
    }
  },
  "loginEmails":{
    "ptc":"bob@bob.com",
    "google":"bob@gmail.com"
  }
}
```
