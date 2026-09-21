package com.naicha.diary.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object TimeUtil {

    private val dayFormat = SimpleDateFormat("yyyy年M月d日", Locale.CHINA)
    private val shortFormat = SimpleDateFormat("M月d日 HH:mm", Locale.CHINA)
    private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.CHINA)

    fun dayLabel(time: Long): String {
        val target = Calendar.getInstance().apply { timeInMillis = time }
        val now = Calendar.getInstance()
        val days = daysBetween(target, now)
        return when (days) {
            0L -> "今天"
            1L -> "昨天"
            2L -> "前天"
            else -> dayFormat.format(Date(time))
        }
    }

    fun fullLabel(time: Long): String = shortFormat.format(Date(time))

    fun monthKey(time: Long): String = monthKeyFormat.format(Date(time))

    fun startOfDay(time: Long): Long = Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun isSameDay(a: Long, b: Long): Boolean = startOfDay(a) == startOfDay(b)

    fun isToday(time: Long): Boolean = isSameDay(time, System.currentTimeMillis())

    fun daysBetween(from: Calendar, to: Calendar): Long {
        val a = startOfDay(from.timeInMillis)
        val b = startOfDay(to.timeInMillis)
        return abs((b - a) / 86_400_000L)
    }

    fun greeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..10 -> "早上好呀"
            in 11..13 -> "中午好呀"
            in 14..17 -> "下午好呀"
            in 18..22 -> "晚上好呀"
            else -> "夜深了"
        }
    }
}
