package uz.meduza.anime.data.models

import com.google.gson.annotations.SerializedName

data class AnimeEpisodesResponse(
    @SerializedName("anime") val anime: AnimeSimpleDto,
    @SerializedName("totalEpisodes") val totalEpisodes: Int,
    @SerializedName("episodes") val episodes: List<EpisodeDto>
)

data class AnimeSimpleDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("posterUrl") val posterUrl: String? = null
)

data class EpisodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("duration") val duration: Int = 0, // in seconds
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("views") val views: Int = 0,
    @SerializedName("season") val season: SeasonSimpleDto? = null,
    @SerializedName("dubLanguages") val dubLanguages: List<String> = emptyList(),
    @SerializedName("subtitleLanguages") val subtitleLanguages: List<String> = emptyList(),
    @SerializedName("hasStreams") val hasStreams: Boolean = true,
    @SerializedName("commentsCount") val commentsCount: Int = 0
)

data class SeasonSimpleDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("seasonNumber") val seasonNumber: Int = 1,
    @SerializedName("title") val title: String = "Fasl 1"
)

data class EpisodeDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("views") val views: Int = 0,
    @SerializedName("anime") val anime: AnimeSimpleDto? = null,
    @SerializedName("season") val season: SeasonSimpleDto? = null,
    @SerializedName("dubs") val dubs: List<DubTrackDto> = emptyList(),
    @SerializedName("subtitles") val subtitles: List<SubtitleTrackDto> = emptyList(),
    @SerializedName("commentsCount") val commentsCount: Int = 0,
    @SerializedName("streamsCount") val streamsCount: Int = 0
)

data class EpisodeStreamsResponse(
    @SerializedName("episodeId") val episodeId: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("streams") val streams: List<VideoStreamDto> = emptyList(),
    @SerializedName("dubs") val dubs: List<DubTrackDto> = emptyList(),
    @SerializedName("subtitles") val subtitles: List<SubtitleTrackDto> = emptyList()
)

data class VideoStreamDto(
    @SerializedName("id") val id: String,
    @SerializedName("quality") val quality: String, // P1080, P720, P480, AUTO_HLS
    @SerializedName("streamType") val streamType: String, // HLS, MP4, DASH
    @SerializedName("url") val url: String,
    @SerializedName("serverName") val serverName: String? = "Meduza CDN",
    @SerializedName("isDirect") val isDirect: Boolean = false,
    @SerializedName("headers") val headers: Map<String, String>? = null,
    @SerializedName("isEncrypted") val isEncrypted: Boolean = false
)

data class DubTrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("language") val language: String,
    @SerializedName("dubGroup") val dubGroup: String? = "Meduza Dub",
    @SerializedName("voiceActors") val voiceActors: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false
)

data class SubtitleTrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("language") val language: String,
    @SerializedName("format") val format: String = "VTT",
    @SerializedName("url") val url: String,
    @SerializedName("isDefault") val isDefault: Boolean = false
)

data class SaveProgressRequest(
    @SerializedName("episodeId") val episodeId: String,
    @SerializedName("stoppedAt") val stoppedAt: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("isCompleted") val isCompleted: Boolean? = null
)
