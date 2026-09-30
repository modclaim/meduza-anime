package uz.meduza.anime.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import timber.log.Timber
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.data.models.DubTrackDto
import uz.meduza.anime.data.models.VideoStreamDto
import uz.meduza.anime.ui.player.components.SpeedSelectorDialog

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    episodeId: String,
    animeSlug: String,
    onBackClick: () -> Unit,
    onUpgradePremiumClick: () -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()

    var showQualityDialog by remember { mutableStateOf(false) }
    var showDubDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }
    var commentText by remember { mutableStateOf("") }
    var isSpoilerComment by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    var areControlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var hasStartedPlayback by remember { mutableStateOf(false) }
    var showResumeDialog by remember { mutableStateOf(false) }
    var hasPromptedResume by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPositionMs by remember { mutableLongStateOf(0L) }
    var showRewindFeedback by remember { mutableStateOf(false) }
    var showForwardFeedback by remember { mutableStateOf(false) }

    // -----------------------------------------------------------------------
    // Fullscreen management using WindowInsetsController (API 30+ / compat)
    // Hides status bar + navigation bar, enables immersive mode
    // -----------------------------------------------------------------------
    val activity = context as? Activity
    val window = activity?.window

    fun enterFullscreen() {
        isFullscreen = true
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        window?.let { win ->
            WindowCompat.setDecorFitsSystemWindows(win, false)
            WindowInsetsControllerCompat(win, view).apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun exitFullscreen() {
        isFullscreen = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window?.let { win ->
            WindowCompat.setDecorFitsSystemWindows(win, true)
            WindowInsetsControllerCompat(win, view).show(WindowInsetsCompat.Type.systemBars())
            win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val toggleFullscreen: () -> Unit = {
        if (isFullscreen) exitFullscreen() else enterFullscreen()
    }

    BackHandler(enabled = isFullscreen) { exitFullscreen() }

    // -----------------------------------------------------------------------
    // ExoPlayer setup
    // -----------------------------------------------------------------------
    val httpDataSourceFactory = remember {
        DefaultHttpDataSource.Factory()
            .setUserAgent("MeduzaAnime/1.0.2 (Linux; Android; ExoPlayer)")
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(20_000)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
    }

    val dataSourceFactory = remember(context, httpDataSourceFactory) {
        DefaultDataSource.Factory(context, httpDataSourceFactory)
    }

    val mediaSourceFactory = remember(dataSourceFactory) {
        DefaultMediaSourceFactory(dataSourceFactory)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setSeekForwardIncrementMs(10_000)
            .setSeekBackIncrementMs(10_000)
            .build()
            .apply { playWhenReady = false }
    }

    // Restore system bars + release player when navigating away (must be after exoPlayer remember)
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window?.let { win ->
                WindowCompat.setDecorFitsSystemWindows(win, true)
                WindowInsetsControllerCompat(win, view).show(WindowInsetsCompat.Type.systemBars())
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            viewModel.stopProgressTracking()
            try {
                val posSec = (exoPlayer.currentPosition / 1000).toInt()
                val durSec = (exoPlayer.duration / 1000).toInt()
                if (posSec > 0 && durSec > 0) {
                    viewModel.saveCurrentProgress(episodeId, posSec, durSec)
                }
            } catch (_: Exception) {}
            exoPlayer.release()
        }
    }

    // -----------------------------------------------------------------------
    // ExoPlayer listeners
    // -----------------------------------------------------------------------
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = (state == Player.STATE_BUFFERING)
                val dur = exoPlayer.duration
                if (dur > 0L) durationMs = dur
            }
            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                Timber.e(error, "ExoPlayer error: %s (code: %d)", error.message, error.errorCode)
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Position polling — 500ms is enough (saves battery vs 250ms)
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isSeeking) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                val dur = exoPlayer.duration
                if (dur > 0L) durationMs = dur
            }
            delay(500L)
        }
    }

    // Auto-hide controls after 4 s when playing
    LaunchedEffect(areControlsVisible, isPlaying, isBuffering) {
        if (areControlsVisible && isPlaying && !isBuffering) {
            delay(4_000L)
            areControlsVisible = false
        }
    }

    LaunchedEffect(showRewindFeedback) {
        if (showRewindFeedback) { delay(650L); showRewindFeedback = false }
    }
    LaunchedEffect(showForwardFeedback) {
        if (showForwardFeedback) { delay(650L); showForwardFeedback = false }
    }

    LaunchedEffect(episodeId) { viewModel.loadEpisode(episodeId) }

    LaunchedEffect(uiState.isLoading, uiState.savedProgressSeconds) {
        if (!uiState.isLoading && uiState.savedProgressSeconds > 10 && !hasPromptedResume) {
            hasPromptedResume = true
            showResumeDialog = true
        }
    }

    // Load media when stream changes
    LaunchedEffect(uiState.selectedStream, uiState.selectedStreamUrl) {
        val stream = uiState.selectedStream
        val streamUrl = stream?.url ?: uiState.selectedStreamUrl
        if (!streamUrl.isNullOrEmpty()) {
            val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)

            val isHls  = stream?.streamType.equals("HLS",  ignoreCase = true) || streamUrl.contains(".m3u8", ignoreCase = true)
            val isMp4  = stream?.streamType.equals("MP4",  ignoreCase = true) || streamUrl.contains(".mp4",  ignoreCase = true)
            val isDash = stream?.streamType.equals("DASH", ignoreCase = true) || streamUrl.contains(".mpd",  ignoreCase = true)

            val mimeType = when {
                isHls  -> MimeTypes.APPLICATION_M3U8
                isMp4  -> MimeTypes.VIDEO_MP4
                isDash -> MimeTypes.APPLICATION_MPD
                else   -> null
            }

            stream?.headers?.takeIf { it.isNotEmpty() }?.let {
                httpDataSourceFactory.setDefaultRequestProperties(it)
            }

            val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(streamUrl))
            if (mimeType != null) mediaItemBuilder.setMimeType(mimeType)
            val mediaItem = mediaItemBuilder.build()

            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (currentPos > 0L) exoPlayer.seekTo(currentPos)

            if (hasStartedPlayback) {
                exoPlayer.playWhenReady = true
                exoPlayer.play()
            } else {
                exoPlayer.playWhenReady = false
            }

            viewModel.startProgressTracking(
                episodeId = episodeId,
                getCurrentPosition = { exoPlayer.currentPosition },
                getDuration = { exoPlayer.duration }
            )
        }
    }

    LaunchedEffect(uiState.selectedDub) {
        uiState.selectedDub?.let { dub ->
            exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                .buildUpon()
                .setPreferredAudioLanguage(dub.language.lowercase())
                .build()
        }
    }

    // -----------------------------------------------------------------------
    // Root layout — fills the entire screen edge-to-edge, no Scaffold
    // -----------------------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isFullscreen) {
            // ----------------------------------------------------------------
            // FULLSCREEN — player takes the entire window, nothing else shown
            // ----------------------------------------------------------------
            PlayerSurface(
                exoPlayer = exoPlayer,
                modifier = Modifier.fillMaxSize()
            )

            PlayerGestureLayer(
                modifier = Modifier.fillMaxSize(),
                onTap = { areControlsVisible = !areControlsVisible },
                onDoubleTapLeft = {
                    val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                    exoPlayer.seekTo(target); currentPositionMs = target; showRewindFeedback = true
                },
                onDoubleTapRight = {
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    val target = (exoPlayer.currentPosition + 10_000L).coerceAtMost(dur)
                    exoPlayer.seekTo(target); currentPositionMs = target; showForwardFeedback = true
                }
            )

            SeekFeedback(showRewindFeedback = showRewindFeedback, showForwardFeedback = showForwardFeedback)

            AnimatedVisibility(
                visible = areControlsVisible && !isBuffering,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(180))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    PlayerControlsOverlay(
                        isPlaying = isPlaying,
                        isFullscreen = true,
                        currentPositionMs = if (isSeeking) seekPositionMs else currentPositionMs,
                        durationMs = durationMs,
                        isSeeking = isSeeking,
                        seekPositionMs = seekPositionMs,
                        currentSpeed = currentSpeed,
                        hasAudioTracks = uiState.streamsResponse?.dubs?.isNotEmpty() == true,
                        selectedQuality = uiState.selectedQuality,
                        episodeTitle = uiState.episodeDetail?.let {
                            "${it.episodeNumber}-qism: ${it.title}"
                        } ?: "",
                        onBack = { exitFullscreen() },
                        onPlayPause = {
                            if (!hasStartedPlayback) {
                                hasStartedPlayback = true
                                exoPlayer.playWhenReady = true
                                exoPlayer.play()
                            } else if (exoPlayer.isPlaying) exoPlayer.pause()
                            else exoPlayer.play()
                        },
                        onRewind = {
                            val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(target); currentPositionMs = target
                        },
                        onForward = {
                            val dur = exoPlayer.duration.coerceAtLeast(0L)
                            val target = (exoPlayer.currentPosition + 10_000L).coerceAtMost(dur)
                            exoPlayer.seekTo(target); currentPositionMs = target
                        },
                        onSeekChange = { isSeeking = true; seekPositionMs = it.toLong() },
                        onSeekFinished = {
                            exoPlayer.seekTo(seekPositionMs)
                            currentPositionMs = seekPositionMs
                            isSeeking = false
                        },
                        onToggleFullscreen = { toggleFullscreen() },
                        onShowSpeed = { showSpeedDialog = true },
                        onShowDub = { showDubDialog = true },
                        onShowQuality = { showQualityDialog = true }
                    )
                }
            }

            // Buffering spinner — always on top, not blocked by controls
            if (isBuffering && hasStartedPlayback) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCyan, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                }
            }

            // Initial play button (fullscreen)
            if (!hasStartedPlayback && !uiState.isLoading && !uiState.isPremiumRestricted) {
                InitialPlayButton(
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        hasStartedPlayback = true
                        exoPlayer.playWhenReady = true
                        exoPlayer.play()
                    }
                )
            }

        } else {
            // ----------------------------------------------------------------
            // PORTRAIT — player on top, scrollable content below
            // ----------------------------------------------------------------
            Column(modifier = Modifier.fillMaxSize()) {

                // Player surface box — delegated to a private composable so
                // AnimatedVisibility inside it is NOT inside a ColumnScope lambda.
                PlayerPortraitContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(220.dp),
                    isPremiumRestricted = uiState.isPremiumRestricted,
                    isLoading = uiState.isLoading,
                    hasError = uiState.errorMessage != null && uiState.selectedStreamUrl == null,
                    errorMessage = uiState.errorMessage,
                    exoPlayer = exoPlayer,
                    areControlsVisible = areControlsVisible,
                    isBuffering = isBuffering,
                    isPlaying = isPlaying,
                    hasStartedPlayback = hasStartedPlayback,
                    isSeeking = isSeeking,
                    seekPositionMs = seekPositionMs,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    currentSpeed = currentSpeed,
                    hasAudioTracks = uiState.streamsResponse?.dubs?.isNotEmpty() == true,
                    selectedQuality = uiState.selectedQuality,
                    episodeTitle = uiState.episodeDetail?.let {
                        "${it.episodeNumber}-qism: ${it.title}"
                    } ?: "",
                    showRewindFeedback = showRewindFeedback,
                    showForwardFeedback = showForwardFeedback,
                    onUpgrade = onUpgradePremiumClick,
                    onBack = onBackClick,
                    onTap = { areControlsVisible = !areControlsVisible },
                    onDoubleTapLeft = {
                        val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                        exoPlayer.seekTo(target); currentPositionMs = target; showRewindFeedback = true
                    },
                    onDoubleTapRight = {
                        val dur = exoPlayer.duration.coerceAtLeast(0L)
                        val target = (exoPlayer.currentPosition + 10_000L).coerceAtMost(dur)
                        exoPlayer.seekTo(target); currentPositionMs = target; showForwardFeedback = true
                    },
                    onPlayPause = {
                        if (!hasStartedPlayback) {
                            hasStartedPlayback = true
                            exoPlayer.playWhenReady = true
                            exoPlayer.play()
                        } else if (exoPlayer.isPlaying) exoPlayer.pause()
                        else exoPlayer.play()
                    },
                    onRewind = {
                        val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                        exoPlayer.seekTo(target); currentPositionMs = target
                    },
                    onForward = {
                        val dur = exoPlayer.duration.coerceAtLeast(0L)
                        val target = (exoPlayer.currentPosition + 10_000L).coerceAtMost(dur)
                        exoPlayer.seekTo(target); currentPositionMs = target
                    },
                    onSeekChange = { isSeeking = true; seekPositionMs = it.toLong() },
                    onSeekFinished = {
                        exoPlayer.seekTo(seekPositionMs)
                        currentPositionMs = seekPositionMs
                        isSeeking = false
                    },
                    onToggleFullscreen = { toggleFullscreen() },
                    onShowSpeed = { showSpeedDialog = true },
                    onShowDub = { showDubDialog = true },
                    onShowQuality = { showQualityDialog = true },
                    onFirstPlay = {
                        hasStartedPlayback = true
                        exoPlayer.playWhenReady = true
                        exoPlayer.play()
                    }
                )

                // Scrollable content below player
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        val ep = uiState.episodeDetail
                        if (ep != null) {
                            Text(
                                text = "${ep.season?.title ?: "Fasl 1"} • ${ep.episodeNumber}-qism",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ep.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = DividerDark)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = "IZOHLAR (${uiState.comments.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(uiState.comments, key = { it.id }) { comment ->
                        var isSpoilerHidden by remember { mutableStateOf(comment.isSpoiler) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CardDarkElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.user.username.take(1).uppercase(),
                                    color = PrimaryCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = comment.user.username,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextWhite
                                        )
                                        if (comment.user.isPremium) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PremiumGold)
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "VIP",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "VIP",
                                                    color = Color.Black,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { viewModel.toggleCommentLike(comment.id) }
                                    ) {
                                        Icon(
                                            imageVector = if (comment.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like",
                                            tint = if (comment.isLikedByMe) BadgeSeriesRed else TextGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${comment.likesCount}", fontSize = 11.sp, color = TextGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                if (isSpoilerHidden) {
                                    Text(
                                        text = "⚠️ Spoiler mavjud (ko'rish uchun bosing)",
                                        fontSize = 12.sp,
                                        color = PremiumGold,
                                        modifier = Modifier.clickable { isSpoilerHidden = false }
                                    )
                                } else {
                                    Text(text = comment.content, fontSize = 13.sp, color = TextWhite)
                                }
                            }
                        }
                    }
                }

                // Comment input (portrait only)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Fikringizni yozing...", color = TextGray, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = InputBackground,
                            unfocusedContainerColor = InputBackground,
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = InputBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.postComment(episodeId, commentText, isSpoilerComment)
                            commentText = ""
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryCyan)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = OnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Dialogs (outside the layout tree so they always appear on top)
    // -----------------------------------------------------------------------
    if (showSpeedDialog) {
        SpeedSelectorDialog(
            currentSpeed = currentSpeed,
            onSpeedSelected = { newSpeed ->
                currentSpeed = newSpeed
                exoPlayer.setPlaybackSpeed(newSpeed)
            },
            onDismiss = { showSpeedDialog = false }
        )
    }

    if (showResumeDialog) {
        AlertDialog(
            onDismissRequest = { showResumeDialog = false },
            containerColor = CardDark,
            title = {
                Text("Ko'rishni davom ettirasizmi?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(
                    "Siz ushbu qismni ${formatPlayerTime(uiState.savedProgressSeconds * 1000L)} daqiqasigacha ko'rgansiz. Davom ettirishni xohlaysizmi?",
                    color = TextGray, fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResumeDialog = false
                        hasStartedPlayback = true
                        exoPlayer.seekTo(uiState.savedProgressSeconds * 1000L)
                        exoPlayer.playWhenReady = true
                        exoPlayer.play()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Davom ettirish", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResumeDialog = false
                    hasStartedPlayback = true
                    exoPlayer.seekTo(0L)
                    exoPlayer.playWhenReady = true
                    exoPlayer.play()
                }) { Text("Boshidan", color = TextWhite) }
            }
        )
    }

    if (showQualityDialog && uiState.streamsResponse != null) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            containerColor = CardDark,
            title = { Text("Video Sifatini Tanlang", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                val streams = uiState.streamsResponse!!.streams
                if (streams.isEmpty()) {
                    Text("Ushbu qism uchun boshqa sifatlar mavjud emas", color = TextGray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        streams.forEach { stream ->
                            val isSelected = uiState.selectedQuality == stream.quality &&
                                    (uiState.selectedStream?.id == stream.id || uiState.selectedStreamUrl == stream.url)
                            val displayQuality = when (stream.quality.uppercase()) {
                                "AUTO_HLS", "AUTO", "AVTO" -> "Auto (Moslashuvchan HLS)"
                                "P1080", "1080P" -> "1080p Full HD"
                                "P720",  "720P"  -> "720p HD"
                                "P480",  "480P"  -> "480p SD"
                                "P360",  "360P"  -> "360p"
                                else -> stream.quality.removePrefix("P")
                            }
                            val isEncrypted = stream.isEncrypted || stream.url.contains("aes", ignoreCase = true)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryCyan.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { viewModel.selectQuality(stream); showQualityDialog = false }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = displayQuality,
                                        color = if (isSelected) PrimaryCyan else TextWhite,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (isEncrypted) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PremiumGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) { Text("AES-128", color = PremiumGold, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CardDarkElevated)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) { Text(stream.streamType, color = PrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showDubDialog && uiState.streamsResponse != null) {
        AlertDialog(
            onDismissRequest = { showDubDialog = false },
            containerColor = CardDark,
            title = { Text("Dublyaj / Ovozni Tanlang", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                val dubs = uiState.streamsResponse!!.dubs
                if (dubs.isEmpty()) {
                    Text("Ushbu qism uchun boshqa dublyaj mavjud emas", color = TextGray)
                } else {
                    Column {
                        dubs.forEach { dub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectDub(dub); showDubDialog = false }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(dub.language, color = TextWhite, fontWeight = FontWeight.SemiBold)
                                Text(dub.dubGroup ?: "Meduza", color = PrimaryCyan, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// =============================================================================
// Private helper composables
// =============================================================================

@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurface(exoPlayer: ExoPlayer, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
        update = { view ->
            if (view.player != exoPlayer) view.player = exoPlayer
        },
        modifier = modifier
    )
}

@Composable
private fun PlayerGestureLayer(
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit
) {
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { offset ->
                    if (offset.x < size.width / 2) onDoubleTapLeft() else onDoubleTapRight()
                },
                onTap = { onTap() }
            )
        }
    )
}

@Composable
private fun SeekFeedback(showRewindFeedback: Boolean, showForwardFeedback: Boolean) {
    if (showRewindFeedback) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(14.dp)
            ) {
                Icon(Icons.Default.FastRewind, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(32.dp))
                Text("-10 soniya", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showForwardFeedback) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(14.dp)
            ) {
                Icon(Icons.Default.FastForward, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(32.dp))
                Text("+10 soniya", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    isPlaying: Boolean,
    isFullscreen: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isSeeking: Boolean,
    seekPositionMs: Long,
    currentSpeed: Float,
    hasAudioTracks: Boolean,
    selectedQuality: String,
    episodeTitle: String,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowDub: () -> Unit,
    onShowQuality: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.52f))
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Text(
                text = episodeTitle,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            )

            // Speed pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable { onShowSpeed() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("${currentSpeed}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(6.dp))

            if (hasAudioTracks) {
                IconButton(onClick = onShowDub, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Audiotrack, contentDescription = "Dubbing", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Quality pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryCyan.copy(alpha = 0.25f))
                    .clickable { onShowQuality() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(formatQualityLabel(selectedQuality), color = PrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Center: rewind / play-pause / forward
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            IconButton(
                onClick = onRewind,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FastRewind, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(22.dp))
                    Text("10s", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryCyan)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = onForward,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FastForward, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(22.dp))
                    Text("10s", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom bar: time / slider / fullscreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .let { if (isFullscreen) it.navigationBarsPadding() else it }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatPlayerTime(currentPositionMs),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            val totalMs = durationMs.coerceAtLeast(1L).toFloat()
            val currentMs = currentPositionMs.toFloat()

            Slider(
                value = currentMs.coerceIn(0f, totalMs),
                onValueChange = onSeekChange,
                onValueChangeFinished = onSeekFinished,
                valueRange = 0f..totalMs,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryCyan,
                    activeTrackColor = PrimaryCyan,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )

            Text(
                text = formatPlayerTime(durationMs),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onToggleFullscreen, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InitialPlayButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.42f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(PrimaryCyan),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Boshlash",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun PremiumPaywall(onUpgrade: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ushbu qism faqat Premium obunachilar uchun!",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onUpgrade,
            colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Premium Obunani Faollashtirish", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlayerErrorState(message: String?, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message ?: "Ushbu qism videosi hali yuklanmagan yoki mavjud emas",
            color = TextWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) { Text("Orqaga qaytish", fontWeight = FontWeight.Bold) }
    }
}

// =============================================================================
// Portrait player box — extracted so AnimatedVisibility is NOT inside a
// ColumnScope lambda (avoids "ColumnScope.AnimatedVisibility implicit receiver" error)
// =============================================================================

@OptIn(UnstableApi::class)
@Composable
private fun PlayerPortraitContent(
    modifier: Modifier,
    isPremiumRestricted: Boolean,
    isLoading: Boolean,
    hasError: Boolean,
    errorMessage: String?,
    exoPlayer: ExoPlayer,
    areControlsVisible: Boolean,
    isBuffering: Boolean,
    isPlaying: Boolean,
    hasStartedPlayback: Boolean,
    isSeeking: Boolean,
    seekPositionMs: Long,
    currentPositionMs: Long,
    durationMs: Long,
    currentSpeed: Float,
    hasAudioTracks: Boolean,
    selectedQuality: String,
    episodeTitle: String,
    showRewindFeedback: Boolean,
    showForwardFeedback: Boolean,
    onUpgrade: () -> Unit,
    onBack: () -> Unit,
    onTap: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowDub: () -> Unit,
    onShowQuality: () -> Unit,
    onFirstPlay: () -> Unit
) {
    Box(
        modifier = modifier.background(Color.Black)
    ) {
        when {
            isPremiumRestricted -> PremiumPaywall(onUpgrade = onUpgrade)

            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryCyan)
            }

            hasError -> PlayerErrorState(message = errorMessage, onBack = onBack)

            else -> {
                PlayerSurface(exoPlayer = exoPlayer, modifier = Modifier.fillMaxSize())

                PlayerGestureLayer(
                    modifier = Modifier.fillMaxSize(),
                    onTap = onTap,
                    onDoubleTapLeft = onDoubleTapLeft,
                    onDoubleTapRight = onDoubleTapRight
                )

                SeekFeedback(
                    showRewindFeedback = showRewindFeedback,
                    showForwardFeedback = showForwardFeedback
                )

                // Controls overlay — hidden while buffering.
                // AnimatedVisibility is inside BoxScope here → resolves correctly.
                AnimatedVisibility(
                    visible = areControlsVisible && !isBuffering,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlay(
                        isPlaying = isPlaying,
                        isFullscreen = false,
                        currentPositionMs = if (isSeeking) seekPositionMs else currentPositionMs,
                        durationMs = durationMs,
                        isSeeking = isSeeking,
                        seekPositionMs = seekPositionMs,
                        currentSpeed = currentSpeed,
                        hasAudioTracks = hasAudioTracks,
                        selectedQuality = selectedQuality,
                        episodeTitle = episodeTitle,
                        onBack = onBack,
                        onPlayPause = onPlayPause,
                        onRewind = onRewind,
                        onForward = onForward,
                        onSeekChange = onSeekChange,
                        onSeekFinished = onSeekFinished,
                        onToggleFullscreen = onToggleFullscreen,
                        onShowSpeed = onShowSpeed,
                        onShowDub = onShowDub,
                        onShowQuality = onShowQuality
                    )
                }

                // Buffering spinner — z-order: rendered after controls, always on top
                if (isBuffering && hasStartedPlayback) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = PrimaryCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }

                // Initial play button
                if (!hasStartedPlayback && !isLoading && !isPremiumRestricted) {
                    InitialPlayButton(
                        modifier = Modifier.fillMaxSize(),
                        onClick = onFirstPlay
                    )
                }
            }
        }
    }
}

// =============================================================================
// Pure functions
// =============================================================================

private fun formatPlayerTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    val remMinutes = minutes % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, remMinutes, seconds)
    } else {
        String.format("%02d:%02d", remMinutes, seconds)
    }
}

private fun formatQualityLabel(quality: String): String = when (quality.uppercase()) {
    "AUTO_HLS", "AUTO", "AVTO" -> "Auto"
    "P1080", "1080P", "1080"   -> "1080p"
    "P720",  "720P",  "720"    -> "720p"
    "P480",  "480P",  "480"    -> "480p"
    "P360",  "360P",  "360"    -> "360p"
    else -> if (quality.startsWith("P", ignoreCase = true)) quality.substring(1) + "p" else quality
}
