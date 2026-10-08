@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.github.tvbox.osc.ui.activity

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.github.tvbox.osc.R
import com.github.tvbox.osc.ui.components.LoadState
import com.github.tvbox.osc.ui.components.LoadStateBox
import com.github.tvbox.osc.ui.components.PosterBackdrop
import com.github.tvbox.osc.ui.components.VodCardMenu
import com.github.tvbox.osc.ui.components.rememberVodCardMenuState
import com.github.tvbox.osc.ui.theme.AppThemeState
import kotlinx.coroutines.delay
import com.github.tvbox.osc.ui.page.jumpToSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(activity: DetailActivity, vm: DetailViewModel) {
    val menuContext = LocalContext.current
    val pageState by vm.pageState.collectAsState()
    val full by vm.fullScreen.collectAsState()
    val rotating by vm.rotating.collectAsState()
    val revision by vm.revision.collectAsState()
    val playSignal by vm.playSignal.collectAsState()
    val toast by vm.toastEvent.collectAsState()
    val finish by vm.finishEvent.collectAsState()
    val vodMenu = rememberVodCardMenuState()

    val configuration = LocalConfiguration.current
    val isLandscapeNow = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fullBox = if (rotating) isLandscapeNow else full

    val container = activity.playContainer
    var backdropPic by remember { mutableStateOf("") }
    var backdropSeed by remember { mutableStateOf<Int?>(null) }
    val darkTheme = AppThemeState.isDark(isSystemInDarkTheme())
    val colorScheme = backdropSeed?.let { argb ->
        remember(argb, darkTheme) {
            AppThemeState.customScheme(argb, darkTheme, AppThemeState.config.style)
        }
    } ?: MaterialTheme.colorScheme

    LaunchedEffect(full, playSignal) {
        if (activity.playContainer == null && (full || playSignal > 0)) activity.ensurePlayContainer()
    }

    LaunchedEffect(vm) {
        vm.playbackCommands.collect { command ->
            val c = activity.playContainer ?: return@collect
            when (command) {
                is PlaybackCommand.StopForContentSwitch -> c.stopForContentSwitch()
                is PlaybackCommand.StopForSourceSwitch -> c.stopForSourceSwitch(command.tip)
                is PlaybackCommand.ClearSourceSwitchTip -> c.clearSourceSwitchTip()
                is PlaybackCommand.SetEpisodeSheetOpen -> c.setEpisodeSheetOpen(command.open)
                is PlaybackCommand.SelectQuality -> c.selectQuality(command.position)
            }
        }
    }

    LaunchedEffect(playSignal) {
        if (playSignal > 0) activity.playCurrent()
    }

    var musicWatch by remember { mutableStateOf(false) }
    var musicArmed by remember { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { musicWatch = true }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { musicWatch = false }
    LaunchedEffect(playSignal) {
        if (playSignal > 0) musicArmed = true
    }
    LaunchedEffect(musicWatch, musicArmed) {
        if (!musicWatch || !musicArmed) return@LaunchedEffect
        while (true) {
            delay(300)
            if (!activity.musicPlaybackDetected()) continue
            if (!activity.handOffToMusicPlayer()) continue
            musicArmed = false
            return@LaunchedEffect
        }
    }

    LaunchedEffect(full) {
        activity.applyFullscreen(full)
    }

    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
            vm.clearToast()
        }
    }

    LaunchedEffect(finish) {
        if (finish) {
            vm.consumeFinish()
            activity.finish()
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            PosterBackdrop(pic = backdropPic)

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = if (fullBox) {
                            Modifier.fillMaxSize().background(Color.Black)
                        } else {
                            Modifier.fillMaxWidth().height(0.dp)
                        },
                    ) {
                        val playerContainer = container
                        if (playerContainer != null) {
                            AndroidView(
                                factory = { playerContainer },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    if (!fullBox) {
                        when (val state = pageState) {
                            is DetailViewModel.PageState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    ContainedLoadingIndicator(Modifier.size(64.dp))
                                }
                            }

                            is DetailViewModel.PageState.Empty -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    LoadStateBox(
                                        state = LoadState.Empty,
                                        emptyText = state.msg ?: stringResource(R.string.detail_empty_source),
                                        errorText = "",
                                        retryText = "",
                                        modifier = Modifier.weight(1f),
                                    )
                                    SourceSection(vm, currentSourceName = null, revision = revision)
                                }
                            }

                            is DetailViewModel.PageState.Ready -> {
                                DetailContent(
                                    activity = activity,
                                    vm = vm,
                                    revision = revision,
                                    onPosterPic = { backdropPic = it },
                                    onSeed = { argb -> if (argb != null) backdropSeed = argb },
                                    onCardLongClick = { vodMenu.show(it) },
                                )
                            }
                        }
                    }
                }
                if (!fullBox) {
                    DetailTopScrim(modifier = Modifier.align(Alignment.TopCenter))
                }
            }
        }

        EpisodeSheet(vm, revision, slideFromEnd = fullBox && isLandscapeNow)
        VodCardMenu(vodMenu) { menuContext.jumpToSearch(it) }
    }
}
