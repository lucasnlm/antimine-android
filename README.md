# Antimine - Minesweeper
![Android CI](https://github.com/lucasnlm/antimine-android/workflows/Android%20CI/badge.svg) [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![GitHub release](https://img.shields.io/github/release/lucasnlm/antimine-android.svg?maxAge=60)](https://github.com/lucasnlm/antimine-android/releases) [![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/) [![crowdin](https://badges.crowdin.net/antimine-android/localized.svg)](https://crowdin.com/project/antimine-android) [![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Flucasnlm%2Fantimine-android.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2Flucasnlm%2Fantimine-android?ref=badge_shield)

### Description

Antimine is a minesweeper-like puzzle game. The objective is to flag the spaces with mines to make the field a safer place without exploding any of them.

You win the game when you've flagged every mine in the minefield. Be careful not to trigger one!

### Screeshots

<p align="center">
    <img src="https://github.com/lucasnlm/antimine-android/blob/master/fastlane/metadata/android/image_1.png" width="275px"/>
    <img src="https://github.com/lucasnlm/antimine-android/blob/master/fastlane/metadata/android/image_5.png" width="275px"/>
    <img src="https://github.com/lucasnlm/antimine-android/blob/master/fastlane/metadata/android/image_3.png" width="275px"/>
</p>

## Contributing

Feel free to contribute with [issues](https://github.com/lucasnlm/antimine-android/issues), [feature requests](https://github.com/lucasnlm/antimine-android/issues), [pull requests](https://github.com/lucasnlm/antimine-android/pulls), or [translating](https://crowdin.com/project/antimine-android).

## Download

<a href="https://f-droid.org/packages/dev.lucanlm.antimine/">
    <img src="https://raw.githubusercontent.com/lucasnlm/antimine-android/master/.github/fdroid.png" alt="Get it on F-Droid" height="80"/>
</a>
<a href="https://play.google.com/store/apps/details?id=com.logical.minato">
    <img src="https://raw.githubusercontent.com/lucasnlm/antimine-android/master/.github/google_play.png" alt="Get it on Google Play" height="80"/>
</a>

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
- Game statistics
- Android Wear OS support
- Dark Theme support
- Save/Resume state when quit/resume game
- Put flag on long press or double click
- Put question mark on double long press
- Open multiple areas by long pressing the numbers
- Game assistant to auto-flag discovered mines
- Share game
- App shortcuts
- Split screen
- Assessability: large toachable area
- Assessability: screen reader

## Technical Details

- [Android SDK 29](https://developer.android.com/about/versions/10)
- [AndroidX](https://developer.android.com/jetpack/androidx)
- [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle)
- [Dagger](https://dagger.dev/android.html)
- [Room](https://developer.android.com/training/data-storage/room)
- [Robolectric](http://robolectric.org/)
- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2Flucasnlm%2Fantimine-android.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2Flucasnlm%2Fantimine-android?ref=badge_large)
