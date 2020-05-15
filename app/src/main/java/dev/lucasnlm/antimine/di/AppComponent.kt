package dev.lucasnlm.antimine.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dev.lucasnlm.antimine.MainApplication
import dev.lucasnlm.antimine.common.level.di.LevelModule
import dev.lucasnlm.antimine.core.di.CommonModule
import javax.inject.Singleton

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        LevelModule::class,
        ActivityModule::class,
        FragmentModule::class,
        CommonModule::class
    ]
)
@Singleton
interface AppComponent : AndroidInjector<MainApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun levelModule(application: LevelModule): Builder

        fun appModule(module: AppModule): Builder

        fun build(): AppComponent
    }
}
