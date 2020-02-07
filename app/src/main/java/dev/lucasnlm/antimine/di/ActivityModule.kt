package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.GameActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dev.lucasnlm.antimine.TvGameActivity
import dev.lucasnlm.antimine.core.scope.ActivityScope

@Module
interface ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector
    fun contributeGameActivityInjector(): GameActivity

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeTvGameActivityInjector(): TvGameActivity
}
