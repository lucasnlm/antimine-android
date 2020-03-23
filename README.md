# Anti-mine ![Android CI](https://github.com/lucasnlm/antimine-android/workflows/Android%20CI/badge.svg) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/) [![crowdin](https://badges.crowdin.net/antimine-android/localized.svg)](https://crowdin.com/project/antimine-android)

Anti-Mine is a minesweeper puzzle game. The objective is: search for all hidden mines and clear the minefield without explode any of them.

Flag the spaces with mines to make the board a safer place. You win the game when you've flagged every mine on the board. Be careful not to trigger one, or the game is over!

<p align="center">
    <img src="https://raw.githubusercontent.com/lucasnlm/antimine-android/master/extras/store/image_1.png" width="275px"/>
    <img src="https://raw.githubusercontent.com/lucasnlm/antimine-android/master/extras/store/image_5.png" width="275px"/>
    <img src="https://raw.githubusercontent.com/lucasnlm/antimine-android/master/extras/store/image_3.png" width="275px"/>
</p>

## Contributing

Feel free to contribute with pull requests.

## Download

<a href="https://play.google.com/store/apps/details?id=com.logical.minato">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
     alt="Get it on Google Play" height="80"/></a>

## Building

You can build from source code by running:

```bash
git clone git@github.com:lucasnlm/antimine-android.git
cd antimine-android

./gradlew assembleRelease -Dorg.gradle.java.home=$ANDROID_JRE
```

Where `ANDROID_JRE` is the Java runtime provided by Android Studio.

## Features

- Game levels: begginner, intermediate, expert and custom
- Android Wear OS support
- Dark Theme support
- Save/Resume state when quit/resume game
- Put flag on long press
- Put question mark on double long press
- Open multiple areas by long pressing the numbers
- Game assistant to auto-flag discovered mines
- Share game
- App shortcuts
- Split screen
- Assessability: large toachable area
- Assessability: screen reader

## Technical Details

- Android SDK 29
- AndroidX
- Lifecycle
- Dagger
- Room
- Coroutines

