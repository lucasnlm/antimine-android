package dev.lucasnlm.antimine.core.di

import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        CommonModule::class
    ]
)
abstract class LevelComponent {
    @Component.Builder
    interface Builder {
        fun levelModule(levelModule: CommonModule): Builder
        fun build(): LevelComponent
    }
}
