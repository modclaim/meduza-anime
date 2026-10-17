package uz.meduza.anime

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import uz.meduza.anime.data.models.UserInteractionDto
import uz.meduza.anime.ui.schedule.getTodayDayOfWeek
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ContractRegressionTest {
    private fun utc(value: String): Long = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(value)!!.time

    @Test fun scheduleChangesAtTashkentMidnight() {
        assertEquals("SUNDAY", getTodayDayOfWeek(utc("2026-10-04 18:59:59")))
        assertEquals("MONDAY", getTodayDayOfWeek(utc("2026-10-04 19:00:00")))
    }
    @Test fun scheduleIgnoresDeviceTimezone() {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            assertEquals("MONDAY", getTodayDayOfWeek(utc("2026-10-04 20:00:00")))
        } finally { TimeZone.setDefault(previous) }
    }
    @Test fun ratingAcceptsCurrentApiName() {
        assertEquals(8, Gson().fromJson("{\"userRating\":8}", UserInteractionDto::class.java).rating)
    }
    @Test fun ratingAcceptsLegacyApiName() {
        assertEquals(7, Gson().fromJson("{\"rating\":7}", UserInteractionDto::class.java).rating)
    }
}
