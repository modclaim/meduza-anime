package uz.meduza.anime

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test
import uz.meduza.anime.core.config.*
import uz.meduza.anime.data.models.*
import uz.meduza.anime.ui.detail.*
import uz.meduza.anime.ui.profile.planSavingPercent

class RedesignRegressionTest {
    @Test
    fun guestsNeverPlayEvenWhenBrowsingIsEnabled() {
        assertTrue(AccessPolicy.canBrowseAsGuest(AppRuntimeConfig()))
        assertFalse(AccessPolicy.canPlay(false, AppRuntimeConfig(guestEnabled = true)))
    }

    @Test
    fun disablingGuestsDoesNotBlockSignedInPlayback() {
        val config = AppRuntimeConfig(guestEnabled = false)
        assertFalse(AccessPolicy.canBrowseAsGuest(config))
        assertTrue(AccessPolicy.canPlay(true, config))
    }

    @Test
    fun maintenanceBlocksAllPlaybackAndGuestEntry() {
        val config = AppRuntimeConfig(maintenanceEnabled = true)
        assertFalse(AccessPolicy.canPlay(true, config))
        assertFalse(AccessPolicy.canBrowseAsGuest(config))
    }

    @Test
    fun malformedRemoteFlagsNeverOverwriteVerifiedSettings() {
        for (json in
            listOf(
                "{}",
                "{\"guestEnabled\":\"false\"}",
                "{\"guestEnabled\":false,\"maintenanceEnabled\":null}",
            )) assertNull(parseRuntimeConfig(JsonParser.parseString(json).asJsonObject))
        val valid =
            parseRuntimeConfig(
                JsonParser.parseString(
                        "{\"guestEnabled\":false,\"maintenanceEnabled\":true,\"maintenanceTitle\":\"Ishlar\",\"maintenanceMessage\":\"Kuting\"}"
                    )
                    .asJsonObject
            )
        assertNotNull(valid)
        assertFalse(valid!!.guestEnabled)
        assertTrue(valid.maintenanceEnabled)
    }

    @Test
    fun ratingFailureRemainsVisibleAndKeepsThePreviousScore() {
        val initial = AnimeDetailUiState(isRating = true, userRating = 6, ratingSaved = 2)
        val result = initial.withRatingResult(9, Result.failure(Exception("401")))
        assertFalse(result.isRating)
        assertNotNull(result.actionError)
        assertEquals(6, result.userRating)
        assertEquals(2, result.ratingSaved)
    }

    @Test
    fun ratingSuccessUpdatesVisibleAverageAndVoteCount() {
        val anime =
            AnimeDto(
                id = "anime",
                title = "Anime",
                slug = "anime",
                posterUrl = "",
                rating = 6.0,
                ratingCount = 3,
            )
        val result =
            AnimeDetailUiState(anime = anime, isRating = true)
                .withRatingResult(8, Result.success(RateAnimeResponse("anime", 8, 6.5, 4)))
        assertEquals(6.5, result.anime!!.rating, 0.001)
        assertEquals(4, result.anime!!.ratingCount)
        assertEquals(8, result.userRating)
        assertNull(result.actionError)
        assertEquals(1, result.ratingSaved)
    }

    @Test
    fun premiumSavingsUseActualComparablePrices() {
        val monthly = PaymentPlanDto("month", "Bir oy", 30, 30000)
        val quarter = PaymentPlanDto("quarter", "Uch oy", 90, 81000)
        assertEquals(10, planSavingPercent(quarter, listOf(monthly, quarter)))
        assertEquals(0, planSavingPercent(monthly, listOf(monthly, quarter)))
        assertEquals(0, planSavingPercent(quarter.copy(days = 0), listOf(monthly)))
        assertEquals(0, planSavingPercent(quarter.copy(currency = "USD"), listOf(monthly)))
    }
}
