package com.naicha.diary.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.naicha.diary.MainActivity
import com.naicha.diary.R
import com.naicha.diary.data.Drink

object LiveUpdateNotifier {

    private const val CHANNEL_ID = "naicha_live"
    private const val CHANNEL_NAME = "饮品进度"
    private const val NOTI_ID = 8101

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "记录今天的饮品，支持灵动胶囊实时展示"
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    fun show(context: Context, todayCups: Int, goal: Int, latest: Drink?) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val percent = if (goal <= 0) 0 else (todayCups * 100 / goal).coerceIn(0, 100)

        val title = if (latest != null && latest.brand.isNotBlank()) {
            "今天第 $todayCups 次 · ${latest.brand}"
        } else {
            "今天已记录 $todayCups 次"
        }
        val summary = if (latest != null) {
            buildString {
                if (latest.name.isNotBlank()) append(latest.name)
                append(" · ${latest.cupSize} · ${latest.sugar}")
                if (latest.calories > 0) append(" · ${latest.calories}kcal")
            }
        } else {
            "点一杯，记录此刻的小确幸"
        }

        val chip = if (todayCups > goal) "🥤 $todayCups/$goal 超标" else "🥤 $todayCups/$goal"

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_noti_cup)
            .setContentTitle(title)
            .setContentText(summary)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setColor(0xFFC98A5B.toInt())

        runCatching { builder.setShortCriticalText(chip) }

        if (Build.VERSION.SDK_INT >= 36) {
            runCatching {
                val style = Notification.ProgressStyle()
                    .setProgress(percent)
                    .setProgressTrackerIcon(
                        android.graphics.drawable.Icon.createWithResource(
                            context,
                            R.drawable.ic_noti_cup,
                        )
                    )
                builder.setStyle(style)
            }
        }

        runCatching {
            builder.addExtras(Bundle().apply {
                putBoolean("android.requestPromotedOngoing", true)
            })
        }

        runCatching { manager.notify(NOTI_ID, builder.build()) }
    }

    fun dismiss(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.cancel(NOTI_ID) }
    }
}
