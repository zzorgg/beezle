package com.github.zzorgg.beezle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TwoSwordsIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Canvas(
        modifier = modifier.size(size)
    ) {
        drawTwoSwords(color)
    }
}

private fun DrawScope.drawTwoSwords(color: Color) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val swordLength = size.width * 0.35f
    val handleLength = size.width * 0.12f
    val guardWidth = size.width * 0.15f
    val strokeWidth = size.width * 0.04f

    // First sword (tilted left, pointing up-right)
    val angle1 = -Math.PI / 4 // -45 degrees
    drawSword(
        centerX = centerX - size.width * 0.1f,
        centerY = centerY + size.height * 0.1f,
        angle = angle1,
        swordLength = swordLength,
        handleLength = handleLength,
        guardWidth = guardWidth,
        strokeWidth = strokeWidth,
        color = color
    )

    // Second sword (tilted right, pointing up-left)
    val angle2 = Math.PI / 4 + Math.PI // 225 degrees (opposite direction)
    drawSword(
        centerX = centerX + size.width * 0.1f,
        centerY = centerY + size.height * 0.1f,
        angle = angle2,
        swordLength = swordLength,
        handleLength = handleLength,
        guardWidth = guardWidth,
        strokeWidth = strokeWidth,
        color = color
    )
}

private fun DrawScope.drawSword(
    centerX: Float,
    centerY: Float,
    angle: Double,
    swordLength: Float,
    handleLength: Float,
    guardWidth: Float,
    strokeWidth: Float,
    color: Color
) {
    val cosAngle = cos(angle).toFloat()
    val sinAngle = sin(angle).toFloat()

    // Blade (main line)
    val bladeStartX = centerX - (handleLength * cosAngle)
    val bladeStartY = centerY - (handleLength * sinAngle)
    val bladeEndX = centerX + (swordLength * cosAngle)
    val bladeEndY = centerY + (swordLength * sinAngle)

    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(bladeStartX, bladeStartY),
        end = androidx.compose.ui.geometry.Offset(bladeEndX, bladeEndY),
        strokeWidth = strokeWidth
    )

    // Handle
    val handleStartX = centerX - (handleLength * cosAngle)
    val handleStartY = centerY - (handleLength * sinAngle)
    val handleEndX = centerX - (handleLength * 1.5f * cosAngle)
    val handleEndY = centerY - (handleLength * 1.5f * sinAngle)

    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(handleStartX, handleStartY),
        end = androidx.compose.ui.geometry.Offset(handleEndX, handleEndY),
        strokeWidth = strokeWidth * 1.2f
    )

    // Cross guard
    val guardAngle = angle + Math.PI / 2 // Perpendicular to sword
    val guardCosAngle = cos(guardAngle).toFloat()
    val guardSinAngle = sin(guardAngle).toFloat()

    val guardStartX = centerX - (guardWidth / 2 * guardCosAngle)
    val guardStartY = centerY - (guardWidth / 2 * guardSinAngle)
    val guardEndX = centerX + (guardWidth / 2 * guardCosAngle)
    val guardEndY = centerY + (guardWidth / 2 * guardSinAngle)

    drawLine(
        color = color,
        start = androidx.compose.ui.geometry.Offset(guardStartX, guardStartY),
        end = androidx.compose.ui.geometry.Offset(guardEndX, guardEndY),
        strokeWidth = strokeWidth * 1.1f
    )

    // Pommel (small circle at the end of handle)
    val pommelX = centerX - (handleLength * 1.7f * cosAngle)
    val pommelY = centerY - (handleLength * 1.7f * sinAngle)

    drawCircle(
        color = color,
        radius = strokeWidth * 0.8f,
        center = androidx.compose.ui.geometry.Offset(pommelX, pommelY)
    )
}
