package dev.lucasnlm.antimine.wear.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import dev.lucasnlm.antimine.core.scope.ActivityScope
import dev.lucasnlm.antimine.wear.WatchLevelFragment

@Module
interface FragmentModule {
    @ActivityScope
    @ContributesAndroidInjector
    fun contributeWatchLevelFragmentInjector(): WatchLevelFragment
}
