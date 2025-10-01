package com.github.zzorgg.beezle.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter.State
import coil.compose.AsyncImagePainter.Companion.DefaultTransform
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageScope
import com.github.zzorgg.beezle.R

@Composable
fun MonochromeAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alternateImageModifier: Modifier = Modifier,
    transform: (State) -> State = DefaultTransform,
    loading: @Composable (SubcomposeAsyncImageScope.(State.Loading) -> Unit)? = null,
    success: @Composable (SubcomposeAsyncImageScope.(State.Success) -> Unit)? = null,
    error: @Composable (SubcomposeAsyncImageScope.(State.Error) -> Unit)? = null,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
) = SubcomposeAsyncImage(
    model = model,
    contentDescription = contentDescription,
    modifier = modifier.clip(RoundedCornerShape(4.dp)),
    transform = transform,
    loading = loading ?: {
        Box(
            contentAlignment = Alignment.Center,
            modifier = alternateImageModifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            CircularProgressIndicator()
        }
    },
    success = success,
    error = error ?: {
        Box(
            contentAlignment = Alignment.Center,
            modifier = alternateImageModifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }
    },
    onLoading = onLoading,
    onSuccess = onSuccess,
    onError = onError,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
)
