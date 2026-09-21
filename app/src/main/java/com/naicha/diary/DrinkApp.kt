package com.naicha.diary

import android.app.Application
import com.naicha.diary.data.AppSettings
import com.naicha.diary.data.DrinkRepository
import com.naicha.diary.notify.LiveUpdateNotifier
import com.naicha.diary.util.PhotoStore
import java.io.File

class DrinkApp : Application() {

    lateinit var repository: DrinkRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        installCrashLogger()
        AppSettings.init(this)
        repository = DrinkRepository(this)
        PhotoStore.init(this)
        LiveUpdateNotifier.createChannels(this)
    }

    private fun installCrashLogger() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                File(filesDir, "last_crash.txt").writeText(
                    buildString {
                        append(java.util.Date().toString())
                        append("\n")
                        append(throwable.stackTraceToString())
                    }
                )
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        lateinit var instance: DrinkApp
            private set

        fun lastCrash(context: android.content.Context): String? = runCatching {
            val file = File(context.filesDir, "last_crash.txt")
            if (file.exists()) file.readText() else null
        }.getOrNull()

        fun clearCrash(context: android.content.Context) {
            runCatching { File(context.filesDir, "last_crash.txt").delete() }
        }
    }
}

val app: DrinkApp get() = DrinkApp.instance
