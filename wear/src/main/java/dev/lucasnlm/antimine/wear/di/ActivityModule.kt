package dev.lucasnlm.antimine.wear.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dev.lucasnlm.antimine.core.scope.ActivityScope
import dev.lucasnlm.antimine.wear.WatchGameActivity

@Module
interface ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector
    fun contributeWatchGameActivityInjector(): WatchGameActivity
}
