package com.antigravity.aegis

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.antigravity.aegis.data.worker.BudgetFollowUpWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AegisApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        val workRequest = PeriodicWorkRequestBuilder<BudgetFollowUpWorker>(1, TimeUnit.DAYS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BudgetFollowUp",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
