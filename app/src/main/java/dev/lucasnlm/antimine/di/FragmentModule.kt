package dev.lucasnlm.antimine.di

import dev.lucasnlm.antimine.level.view.CustomLevelDialogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dev.lucasnlm.antimine.core.scope.ActivityScope
import dev.lucasnlm.antimine.level.view.EndGameDialogFragment
import dev.lucasnlm.antimine.level.view.LevelFragment

@Module
interface FragmentModule {
    @ActivityScope
    @ContributesAndroidInjector
    fun contributeLevelFragmentInjector(): LevelFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeCustomLevelDialogFragmentInjector(): CustomLevelDialogFragment

    @ActivityScope
    @ContributesAndroidInjector
    fun contributeGameOverDialogFragmentInjector(): EndGameDialogFragment
}
