package com.github.tvbox.osc.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.tvbox.osc.R
import com.github.tvbox.osc.ui.components.HeroSpotlightFadeStops
import com.github.tvbox.osc.ui.components.ImagePalette
import com.github.tvbox.osc.ui.components.LocalTopBarGlassBackdrop
import com.github.tvbox.osc.ui.components.TopBarActionBox
import com.github.tvbox.osc.ui.components.VodPoster
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DetailTopScrimAlpha = 0.32f

private const val HeroCircleAlpha = 0.6f

private const val HERO_HEIGHT_RATIO = 0.70f

private val DetailTopScrimExtra = 24.dp

private val HeroMinHeight = 480.dp

private val HeroMaxHeight = 660.dp

private val HeroCaptionHorizontalPadding = 24.dp

private val HeroCaptionBottomPadding = 16.dp

private val HeroCaptionSpacing = 6.dp

private val HeroPlayCapsuleHeight = 52.dp

private val HeroPlayCapsuleMinWidth = 180.dp

private val HeroPlayCapsuleHorizontalPadding = 32.dp

private val HeroCircleButtonSize = 52.dp

private val HeroCircleButtonSpacing = 24.dp

private val HeroActionSpacing = 20.dp

private val HeroButtonRowSpacing = 16.dp

private val MetaSeparator = " · "

private val TypeSeparators = Regex("[,，、/|;；]+")

@Composable
internal fun DetailTopScrim(modifier: Modifier = Modifier) {
    val statusTop = with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(statusTop + DetailTopScrimExtra)
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = DetailTopScrimAlpha),
                    1f to Color.Transparent,
                ),
            ),
    )
}

@Composable
internal fun DetailHero(
    title: String,
    picture: String?,
    year: Int,
    area: String?,
    type: String?,
    collected: Boolean,
    followed: Boolean,
    onPosterPic: (String) -> Unit,
    onSeed: (Int?) -> Unit,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onMusic: () -> Unit,
    onCast: () -> Unit,
    onCollect: () -> Unit,
    onFollow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(picture) {
        if (!picture.isNullOrEmpty()) onPosterPic(picture)
    }
    val glassBackdrop = rememberLayerBackdrop(onDraw = { drawContent() })
    val circleColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = HeroCircleAlpha)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(
                (LocalConfiguration.current.screenHeightDp * HERO_HEIGHT_RATIO)
                    .dp
                    .coerceIn(HeroMinHeight, HeroMaxHeight),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            *HeroSpotlightFadeStops
                                .map { (position, alpha) -> position to Color.White.copy(alpha = 1f - alpha) }
                                .toTypedArray(),
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                },
        ) {
            Box(modifier = Modifier.fillMaxSize().layerBackdrop(glassBackdrop)) {
                VodPoster(
                    name = title,
                    pic = picture,
                    preferLarge = true,
                    modifier = Modifier.fillMaxSize(),
                    onImage = { image ->
                        scope.launch {
                            val seed = withContext(Dispatchers.Default) { ImagePalette.seedOf(image) }
                            onSeed(seed)
                        }
                    },
                )
            }
        }
        CompositionLocalProvider(LocalTopBarGlassBackdrop provides glassBackdrop) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TopBarActionBox(
                    iconRes = R.drawable.ic_arrow_left,
                    contentDescription = stringResource(R.string.common_back),
                    onClick = onBack,
                    fallbackColor = circleColor,
                )
                Spacer(Modifier.weight(1f))
                TopBarActionBox(
                    iconRes = if (collected) R.drawable.ic_tab_collect_filled else R.drawable.ic_tab_collect,
                    contentDescription = stringResource(
                        if (collected) R.string.detail_uncollect else R.string.detail_collect,
                    ),
                    onClick = onCollect,
                    fallbackColor = circleColor,
                    tint = if (collected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = HeroCaptionHorizontalPadding,
                    end = HeroCaptionHorizontalPadding,
                    bottom = HeroCaptionBottomPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight(800),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            val yearArea = listOfNotNull(
                year.takeIf { it > 0 }?.toString(),
                area?.takeIf { it.isNotBlank() },
            ).joinToString(MetaSeparator)
            if (yearArea.isNotEmpty()) {
                DetailHeroMetaLine(yearArea)
            }
            val types = type.orEmpty()
                .split(TypeSeparators)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(MetaSeparator)
            if (types.isNotEmpty()) {
                DetailHeroMetaLine(types)
            }
            Box(
                modifier = Modifier
                    .padding(top = HeroActionSpacing)
                    .widthIn(min = HeroPlayCapsuleMinWidth)
                    .height(HeroPlayCapsuleHeight)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onPlay)
                    .padding(horizontal = HeroPlayCapsuleHorizontalPadding),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.player_ic_play),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = stringResource(R.string.common_play),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
            DetailActionRow(
                followed = followed,
                onMusic = onMusic,
                onCast = onCast,
                onFollow = onFollow,
                modifier = Modifier.padding(top = HeroButtonRowSpacing),
            )
        }
    }
}

@Composable
internal fun DetailActionRow(
    followed: Boolean,
    onMusic: () -> Unit,
    onCast: () -> Unit,
    onFollow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(HeroCircleButtonSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeroCircleButton(
            iconRes = R.drawable.ic_detail_music_player,
            contentDescription = stringResource(R.string.detail_music_player),
            selected = false,
            onClick = onMusic,
        )
        HeroCircleButton(
            iconRes = R.drawable.ic_detail_cast,
            contentDescription = stringResource(R.string.common_cast),
            selected = false,
            onClick = onCast,
        )
        HeroCircleButton(
            iconRes = R.drawable.ic_tab_following,
            contentDescription = stringResource(R.string.tab_following),
            selected = followed,
            onClick = onFollow,
        )
    }
}

@Composable
private fun HeroCircleButton(
    iconRes: Int,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(HeroCircleButtonSize)
            .detailGlass(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun DetailHeroMetaLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = HeroCaptionSpacing),
    )
}
