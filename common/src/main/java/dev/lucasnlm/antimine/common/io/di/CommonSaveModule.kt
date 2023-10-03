package dev.lucasnlm.antimine.common.io.di

import dev.lucasnlm.antimine.common.io.SaveFileManager
import dev.lucasnlm.antimine.common.io.SaveFileManagerImpl
import dev.lucasnlm.antimine.common.io.SaveListManager
import dev.lucasnlm.antimine.common.io.SaveListManagerImpl
import dev.lucasnlm.antimine.common.io.StatsFileManager
import dev.lucasnlm.antimine.common.io.StatsFileManagerImpl
import dev.lucasnlm.external.FeatureFlagManager
import org.koin.dsl.bind
import org.koin.dsl.module

val CommonIoModule =
    module {
        single {
            val featureFlagManager = get<FeatureFlagManager>()
            when {
                featureFlagManager.isFoss -> {
                    SaveListManagerImpl(get(), 5)
                }
                else -> {
                    SaveListManagerImpl(get(), 1)
                }
            }
        } bind SaveListManager::class

        single {
            SaveFileManagerImpl(get(), get())
        } bind SaveFileManager::class

        single {
            StatsFileManagerImpl(get(), get())
        } bind StatsFileManager::class
    }
