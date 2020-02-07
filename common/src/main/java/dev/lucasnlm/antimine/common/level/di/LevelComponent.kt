package dev.lucasnlm.antimine.common.level.di

import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        LevelModule::class
    ]
)
abstract class LevelComponent {
    @Component.Builder
    interface Builder {
        fun levelModule(levelModule: LevelModule): Builder
        fun build(): LevelComponent
    }
}
