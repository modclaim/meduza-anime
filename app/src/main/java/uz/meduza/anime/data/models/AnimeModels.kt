package uz.meduza.anime.data.models

import com.google.gson.annotations.SerializedName

data class AnimeDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("originalTitle") val originalTitle: String? = null,
    @SerializedName("englishTitle") val englishTitle: String? = null,
    @SerializedName("slug") val slug: String,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("bannerUrl") val bannerUrl: String? = null,
    @SerializedName("trailerUrl") val trailerUrl: String? = null,
    @SerializedName("type") val type: String = "TV",
    @SerializedName("status") val status: String = "ONGOING",
    @SerializedName("season") val season: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("releaseDay") val releaseDay: String? = null,
    @SerializedName("releaseTime") val releaseTime: String? = null,
    @SerializedName("views") val views: Int = 0,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("ratingCount") val ratingCount: Int = 0,
    @SerializedName("ageRating") val ageRating: String? = "PG-13",
    @SerializedName("studio") val studio: String? = null,
    @SerializedName("genres") val genres: List<GenreDto> = emptyList(),
    @SerializedName("totalReleasedEpisodes") val totalReleasedEpisodes: Int = 0,
    @SerializedName("latestEpisode") val latestEpisode: LatestEpisodeDto? = null,
    @SerializedName("availableDubTracks") val availableDubTracks: List<DubSummaryDto> = emptyList(),
    @SerializedName("seasons") val seasons: List<SeasonDto> = emptyList(),
    @SerializedName("userInteraction") val userInteraction: UserInteractionDto? = null
)

data class GenreDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("animeCount") val animeCount: Int? = 0
)

data class SeasonDto(
    @SerializedName("id") val id: String,
    @SerializedName("seasonNumber") val seasonNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("releasedEpisodesCount") val releasedEpisodesCount: Int = 0
)

data class LatestEpisodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("releaseDate") val releaseDate: String? = null
)

data class DubSummaryDto(
    @SerializedName("language") val language: String,
    @SerializedName("dubGroup") val dubGroup: String? = null
)

data class UserInteractionDto(
    @SerializedName("bookmarkStatus") val bookmarkStatus: String? = null,
    @SerializedName("rating") val rating: Int? = null
)

data class RateAnimeRequest(
    @SerializedName("score") val score: Int
)

data class RateAnimeResponse(
    @SerializedName("animeSlug") val animeSlug: String,
    @SerializedName("userScore") val userScore: Int,
    @SerializedName("animeRating") val animeRating: Double,
    @SerializedName("totalRatings") val totalRatings: Int
)

data class AnimesPageResponse(
    @SerializedName("items") val items: List<AnimeDto> = emptyList(),
    @SerializedName("meta") val meta: MetaDto? = null
)
