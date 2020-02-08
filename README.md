# Anti-mine

Anti-Mine is a minesweeper puzzle game. The objective is: search for all hidden mines and clear the minefield without explode any of them.

Flag the spaces with mines to make the board a safer place. You win the game when you've flagged every mine on the board. Be careful not to trigger one, or the game is over!

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
- Game assistant to auto-flag discovered mines
- Assessability: large toachable area

## Technical Details

- Android SDK 29
- AndroidX
- Lifecycle
- Dagger
- Room
- Coroutines


