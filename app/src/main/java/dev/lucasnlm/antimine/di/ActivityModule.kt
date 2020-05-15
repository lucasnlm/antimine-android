package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.GameActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dev.lucasnlm.antimine.TvGameActivity
import dev.lucasnlm.antimine.core.scope.ActivityScope
import dev.lucasnlm.antimine.history.views.HistoryFragment

@Module
interface ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector
    fun contributeGameActivityInjector(): GameActivity

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeHistoryFragmentInjector(): HistoryFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeTvGameActivityInjector(): TvGameActivity
}
