package uz.meduza.anime.core.network

import retrofit2.Response
import retrofit2.http.*
import uz.meduza.anime.data.models.*

interface MeduzaApiService {

    // ==================== AUTH ====================
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<TokenDto>>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<ApiResponse<Any?>>

    // ==================== USERS ====================
    @GET("users/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserDto>>

    @PATCH("users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Any?>>

    @POST("users/me/upgrade-premium")
    suspend fun upgradePremium(@Body request: UpgradePremiumRequest): Response<ApiResponse<UserDto>>

    @POST("users/notifications/device")
    suspend fun updateDeviceNotification(@Body request: DeviceNotificationRequest): Response<ApiResponse<Any?>>

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<ApiResponse<Any?>>

    // ==================== GENRES ====================
    @GET("genres")
    suspend fun getGenres(): Response<ApiResponse<List<GenreDto>>>

    // ==================== ANIMES ====================
    @GET("animes")
    suspend fun getAnimes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("genre") genre: String? = null,
        @Query("year") year: Int? = null,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null,
        @Query("sortBy") sortBy: String = "latest",
        @Query("order") order: String = "desc"
    ): Response<ApiResponse<List<AnimeDto>>>

    @GET("animes/trending")
    suspend fun getTrendingAnimes(@Query("limit") limit: Int = 10): Response<ApiResponse<List<AnimeDto>>>

    @GET("animes/latest")
    suspend fun getLatestAnimes(@Query("limit") limit: Int = 10): Response<ApiResponse<List<AnimeDto>>>

    @GET("animes/search")
    suspend fun searchAnimes(
        @Query("q") query: String? = null,
        @Query("genre") genre: String? = null,
        @Query("year") year: Int? = null,
        @Query("status") status: String? = null,
        @Query("type") type: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<AnimeDto>>>

    @GET("animes/{slug}")
    suspend fun getAnimeDetail(@Path("slug") slug: String): Response<ApiResponse<AnimeDto>>

    @GET("animes/{slug}/episodes")
    suspend fun getAnimeEpisodes(
        @Path("slug") slug: String,
        @Query("season") season: Int? = null
    ): Response<ApiResponse<AnimeEpisodesResponse>>

    @POST("animes/{slug}/rate")
    suspend fun rateAnime(
        @Path("slug") slug: String,
        @Body request: RateAnimeRequest
    ): Response<ApiResponse<RateAnimeResponse>>

    // ==================== EPISODES & STREAMS ====================
    @GET("episodes/{episode_id}")
    suspend fun getEpisodeDetail(@Path("episode_id") episodeId: String): Response<ApiResponse<EpisodeDetailDto>>

    @GET("episodes/{episode_id}/streams")
    suspend fun getEpisodeStreams(@Path("episode_id") episodeId: String): Response<ApiResponse<EpisodeStreamsResponse>>

    @GET("episodes/{episode_id}/dubs")
    suspend fun getEpisodeDubs(@Path("episode_id") episodeId: String): Response<ApiResponse<List<DubTrackDto>>>

    @GET("episodes/{episode_id}/subtitles")
    suspend fun getEpisodeSubtitles(@Path("episode_id") episodeId: String): Response<ApiResponse<List<SubtitleTrackDto>>>

    // ==================== LIBRARY — HISTORY ====================
    @GET("library/history")
    suspend fun getWatchHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<WatchHistoryItemDto>>>

    @POST("library/history")
    suspend fun saveProgress(@Body request: SaveProgressRequest): Response<ApiResponse<Any?>>

    @DELETE("library/history/{episode_id}")
    suspend fun deleteHistoryItem(@Path("episode_id") episodeId: String): Response<ApiResponse<Any?>>

    @DELETE("library/history")
    suspend fun clearHistory(): Response<ApiResponse<Any?>>

    // ==================== LIBRARY — BOOKMARKS ====================
    @GET("library/bookmarks")
    suspend fun getBookmarks(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<BookmarkItemDto>>>

    @POST("library/bookmarks")
    suspend fun setBookmark(@Body request: SetBookmarkRequest): Response<ApiResponse<Any?>>

    @DELETE("library/bookmarks/{slug}")
    suspend fun removeBookmark(@Path("slug") slug: String): Response<ApiResponse<Any?>>

    // ==================== COMMENTS ====================
    @GET("episodes/{episode_id}/comments")
    suspend fun getEpisodeComments(
        @Path("episode_id") episodeId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<CommentDto>>>

    @POST("episodes/{episode_id}/comments")
    suspend fun postComment(
        @Path("episode_id") episodeId: String,
        @Body request: CreateCommentRequest
    ): Response<ApiResponse<CommentDto>>

    @POST("comments/{comment_id}/like")
    suspend fun toggleCommentLike(@Path("comment_id") commentId: String): Response<ApiResponse<ToggleLikeResponse>>

    @DELETE("comments/{comment_id}")
    suspend fun deleteComment(@Path("comment_id") commentId: String): Response<ApiResponse<Any?>>

    // ==================== SCHEDULE ====================
    @GET("schedule")
    suspend fun getWeeklySchedule(): Response<ApiResponse<ScheduleResponse>>

    @GET("schedule/day/{day}")
    suspend fun getScheduleByDay(@Path("day") day: String): Response<ApiResponse<DayScheduleResponse>>

    // ==================== PAYMENTS (inPAY) ====================
    @GET("payments/plans")
    suspend fun getPaymentPlans(): Response<ApiResponse<List<PaymentPlanDto>>>

    @POST("payments/create")
    suspend fun createPayment(@Body request: CreatePaymentRequest): Response<ApiResponse<CreatePaymentResponse>>

    @GET("payments/status/{orderId}")
    suspend fun getPaymentStatus(@Path("orderId") orderId: String): Response<ApiResponse<PaymentStatusResponse>>

    @POST("payments/simulate/{orderId}")
    suspend fun simulatePaymentSuccess(@Path("orderId") orderId: String): Response<ApiResponse<Any?>>
}
