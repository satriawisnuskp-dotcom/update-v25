package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.util.CurrencyHelper
import kotlin.math.roundToInt

/**
 * Data model for an individual interactive financial chart point
 */
data class FinancialDataPoint(
    val dateLabel: String,         // e.g. "26 Okt 2026"
    val shortDateLabel: String,    // e.g. "26 Okt"
    val value: Double,             // e.g. 458228.0
    val formattedValue: String = CurrencyHelper.formatRupiah(value),
    val percentChange: Double = 0.0,
    val deltaAmount: Double = 0.0
)

/**
 * Default color palette matching the financial reference video
 */
object FinancialChartTheme {
    val NeonGreen = Color(0xFF10B981) // Vibrant neon green line
    val NeonGreenGlow = Color(0xFF34D399)
    val DarkChartBg = Color(0xFF131A24) // Dark card surface
    val GridLineColor = Color(0xFF334155).copy(alpha = 0.35f)
    val TooltipBg = Color(0xFF1E2633)
}

/**
 * Generates dummy / sample series for each timeframe matching the video recording
 */
object FinancialLineChartDefaults {

    fun getDummyData(timeframe: String, baseBalance: Double = 2793428.0): List<FinancialDataPoint> {
        return when (timeframe) {
            "7D" -> listOf(
                FinancialDataPoint("1 Nov 2026", "1 Nov", 2146828.0, percentChange = 0.0, deltaAmount = 0.0),
                FinancialDataPoint("2 Nov 2026", "2 Nov", 2200000.0, percentChange = 2.5, deltaAmount = 53172.0),
                FinancialDataPoint("3 Nov 2026", "3 Nov", 2180000.0, percentChange = 1.5, deltaAmount = 33172.0),
                FinancialDataPoint("4 Nov 2026", "4 Nov", 2350000.0, percentChange = 9.5, deltaAmount = 203172.0),
                FinancialDataPoint("5 Nov 2026", "5 Nov", 2400000.0, percentChange = 11.8, deltaAmount = 253172.0),
                FinancialDataPoint("6 Nov 2026", "6 Nov", 2580000.0, percentChange = 20.2, deltaAmount = 433172.0),
                FinancialDataPoint("7 Nov 2026", "7 Nov", baseBalance, percentChange = 21.3, deltaAmount = 646600.0)
            )

            "1M" -> listOf(
                FinancialDataPoint("9 Okt 2026", "9 Okt", 791691.0, percentChange = 0.0, deltaAmount = 0.0),
                FinancialDataPoint("13 Okt 2026", "13 Okt", 944191.0, percentChange = 19.3, deltaAmount = 152500.0),
                FinancialDataPoint("18 Okt 2026", "18 Okt", 820000.0, percentChange = 3.6, deltaAmount = 28309.0),
                FinancialDataPoint("22 Okt 2026", "22 Okt", 650000.0, percentChange = -17.9, deltaAmount = -141691.0),
                FinancialDataPoint("26 Okt 2026", "26 Okt", 458228.0, percentChange = -42.1, deltaAmount = -333463.0),
                FinancialDataPoint("30 Okt 2026", "30 Okt", 1120000.0, percentChange = 41.5, deltaAmount = 328309.0),
                FinancialDataPoint("3 Nov 2026", "3 Nov", 2180000.0, percentChange = 175.4, deltaAmount = 1388309.0),
                FinancialDataPoint("7 Nov 2026", "7 Nov", baseBalance, percentChange = 125.8, deltaAmount = 2001737.0)
            )

            "3M" -> listOf(
                FinancialDataPoint("26 Agu 2026", "Agu 26", 1960000.0, percentChange = 0.0, deltaAmount = 0.0),
                FinancialDataPoint("4 Sep 2026", "4 Sep", 3444461.0, percentChange = 75.7, deltaAmount = 1484461.0),
                FinancialDataPoint("14 Sep 2026", "14 Sep", 1750809.0, percentChange = -10.7, deltaAmount = -209191.0),
                FinancialDataPoint("27 Sep 2026", "27 Sep", 720593.0, percentChange = -63.2, deltaAmount = -1239407.0),
                FinancialDataPoint("13 Okt 2026", "13 Okt", 944191.0, percentChange = -51.8, deltaAmount = -1015809.0),
                FinancialDataPoint("26 Okt 2026", "26 Okt", 458228.0, percentChange = -76.6, deltaAmount = -1501772.0),
                FinancialDataPoint("7 Nov 2026", "Nov 26", baseBalance, percentChange = 42.4, deltaAmount = 1069636.0)
            )

            "YTD" -> listOf(
                FinancialDataPoint("1 Jan 2026", "Jan 26", 1723792.0, percentChange = 0.0, deltaAmount = 0.0),
                FinancialDataPoint("15 Mar 2026", "Mar 26", 2150000.0, percentChange = 24.7, deltaAmount = 426208.0),
                FinancialDataPoint("1 Mei 2026", "Mei 26", 1890000.0, percentChange = 9.6, deltaAmount = 166208.0),
                FinancialDataPoint("4 Sep 2026", "Sep 26", 3444461.0, percentChange = 99.8, deltaAmount = 1720669.0),
                FinancialDataPoint("26 Okt 2026", "Okt 26", 458228.0, percentChange = -73.4, deltaAmount = -1265564.0),
                FinancialDataPoint("7 Nov 2026", "Nov 26", baseBalance, percentChange = 62.1, deltaAmount = 1069636.0)
            )

            else -> listOf( // "ALL"
                FinancialDataPoint("1 Jan 2025", "Jan 25", 850000.0, percentChange = 0.0, deltaAmount = 0.0),
                FinancialDataPoint("1 Jun 2025", "Jun 25", 1400000.0, percentChange = 64.7, deltaAmount = 550000.0),
                FinancialDataPoint("1 Jan 2026", "Jan 26", 1723792.0, percentChange = 102.8, deltaAmount = 873792.0),
                FinancialDataPoint("4 Sep 2026", "Sep 26", 3444461.0, percentChange = 305.2, deltaAmount = 2594461.0),
                FinancialDataPoint("26 Okt 2026", "Okt 26", 458228.0, percentChange = -46.1, deltaAmount = -391772.0),
                FinancialDataPoint("7 Nov 2026", "Nov 26", baseBalance, percentChange = 228.6, deltaAmount = 1943428.0)
            )
        }
    }
}

/**
 * Interactive financial line chart canvas with touch scrubbing, vertical dashed cursor,
 * neon green gradient, and floating dynamic tooltip.
 */
@Composable
fun FinancialLineCanvas(
    points: List<FinancialDataPoint>,
    lineColor: Color = FinancialChartTheme.NeonGreen,
    modifier: Modifier = Modifier,
    onScrub: (FinancialDataPoint?) -> Unit = {},
    showHorizontalGrid: Boolean = true
) {
    if (points.isEmpty()) return

    val minVal = remember(points) { points.minOf { it.value } }
    val maxVal = remember(points) { points.maxOf { it.value } }
    val range = remember(minVal, maxVal) { (maxVal - minVal).coerceAtLeast(1.0) }

    // Touch scrubbing states
    var isDragging by remember { mutableStateOf(false) }
    var touchX by remember { mutableFloatStateOf(0f) }
    var scrubIndex by remember { mutableIntStateOf(-1) }

    Box(
        modifier = modifier
            .pointerInput(points) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        touchX = offset.x
                        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                        val idx = (offset.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                        scrubIndex = idx
                        onScrub(points[idx])

                        tryAwaitRelease()
                        isDragging = false
                        scrubIndex = -1
                        onScrub(null)
                    }
                )
            }
            .pointerInput(points) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        touchX = offset.x
                        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                        val idx = (offset.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                        scrubIndex = idx
                        onScrub(points[idx])
                    },
                    onDragEnd = {
                        isDragging = false
                        scrubIndex = -1
                        onScrub(null)
                    },
                    onDragCancel = {
                        isDragging = false
                        scrubIndex = -1
                        onScrub(null)
                    },
                    onDrag = { change, _ ->
                        touchX = change.position.x
                        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                        val idx = (change.position.x / stepX).roundToInt().coerceIn(0, points.lastIndex)
                        scrubIndex = idx
                        onScrub(points[idx])
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Horizontal dashed grid reference lines
            if (showHorizontalGrid) {
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                val gridLines = 3
                for (i in 1..gridLines) {
                    val y = h * (i.toFloat() / (gridLines + 1))
                    drawLine(
                        color = FinancialChartTheme.GridLineColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }
            }

            if (points.size < 2) return@Canvas

            val stepX = w / (points.size - 1).coerceAtLeast(1)

            // Convert raw values to normalized Y coordinates (with 12% padding top & bottom)
            val coords = points.mapIndexed { idx, pt ->
                val x = idx * stepX
                val normalizedY = ((pt.value - minVal) / range).toFloat()
                val y = h - (normalizedY * h * 0.76f + h * 0.12f)
                Offset(x, y)
            }

            // 2. Smooth Bezier Curve Path
            val linePath = Path()
            val fillPath = Path()
            fillPath.moveTo(coords.first().x, h)
            fillPath.lineTo(coords.first().x, coords.first().y)
            linePath.moveTo(coords.first().x, coords.first().y)

            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val cx1 = p0.x + (p1.x - p0.x) / 2f
                val cy1 = p0.y
                val cx2 = p0.x + (p1.x - p0.x) / 2f
                val cy2 = p1.y

                linePath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                fillPath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
            }

            fillPath.lineTo(coords.last().x, h)
            fillPath.close()

            // 3. Draw Gradient Area Fill under Line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.28f),
                        lineColor.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                )
            )

            // 4. Draw Neon Green Line
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // 5. Active Touch Scrubbing Guideline & Marker Point
            if (isDragging && scrubIndex in coords.indices) {
                val activePt = coords[scrubIndex]
                val activeX = activePt.x
                val activeY = activePt.y

                // Vertical dashed tracking line
                val vDashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                drawLine(
                    color = lineColor.copy(alpha = 0.75f),
                    start = Offset(activeX, 0f),
                    end = Offset(activeX, h),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = vDashEffect
                )

                // Glowing outer marker ring
                drawCircle(
                    color = lineColor.copy(alpha = 0.35f),
                    radius = 9.dp.toPx(),
                    center = Offset(activeX, activeY)
                )

                // Solid neon green dot
                drawCircle(
                    color = lineColor,
                    radius = 5.5.dp.toPx(),
                    center = Offset(activeX, activeY)
                )

                // Inner white sparkle
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = Offset(activeX, activeY)
                )
            }
        }

        // 6. Floating Tooltip Card over the active point
        if (isDragging && scrubIndex in points.indices) {
            val point = points[scrubIndex]
            val stepXPercent = scrubIndex.toFloat() / (points.size - 1).coerceAtLeast(1)

            // Calculate tooltip horizontal alignment offset smoothly
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                val alignBias = (stepXPercent * 2f - 1f).coerceIn(-0.85f, 0.85f)

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = FinancialChartTheme.TooltipBg),
                    border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.8f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .align(BiasAlignment(alignBias, -0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = point.dateLabel,
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = point.formattedValue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }
        }
    }
}

/**
 * Complete Financial Chart Card Component matching the reference video:
 * - Dynamic Timeframe Tabs: [ 7D ] [ 1M ] [ 3M ] [ YTD ] [ ALL ]
 * - Expand fullscreen action button [ ⛶ ]
 * - Dark Card styling with 16dp rounded corners
 * - Responsive scrubbing with live header update
 * - Detailed BottomSheet modal with Y-axis currency scale labels
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveFinancialChartCard(
    currentBalance: Double = 2793428.0,
    walletCount: Int = 4,
    planAmount: Double = 5443428.0,
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    customPointsMap: Map<String, List<FinancialDataPoint>>? = null
) {
    var isBalanceHidden by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("1M") }
    var showExpandedSheet by remember { mutableStateOf(false) }

    val periods = listOf("7D", "1M", "3M", "YTD", "ALL")

    // Active dataset for selected period
    val activePoints = remember(selectedPeriod, currentBalance, customPointsMap) {
        customPointsMap?.get(selectedPeriod) ?: FinancialLineChartDefaults.getDummyData(selectedPeriod, currentBalance)
    }

    // Scrubbed point state (null when user is not touching the chart)
    var scrubbedPoint by remember { mutableStateOf<FinancialDataPoint?>(null) }

    // Dynamically displayed metrics based on scrubbed point vs latest point
    val latestPoint = activePoints.lastOrNull()
    val displayPoint = scrubbedPoint ?: latestPoint

    val displayAmount = displayPoint?.value ?: currentBalance
    val displayDelta = displayPoint?.deltaAmount ?: (latestPoint?.deltaAmount ?: 2001737.0)
    val displayPercent = displayPoint?.percentChange ?: (latestPoint?.percentChange ?: 125.8)
    val isPositive = displayDelta >= 0

    val trendColor = if (isPositive) IncomeGreen else ExpenseRed
    val trendIcon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
    val deltaSign = if (displayDelta >= 0) "+" else "-"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FinancialChartTheme.DarkChartBg),
        border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            // 1. Top Balance & Visibility Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBalanceHidden) "Rp ••••••••" else CurrencyHelper.formatRupiah(displayAmount),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { isBalanceHidden = !isBalanceHidden },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle saldo",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Trend Badge Pill: e.g. [ ↗ +Rp2.001.737  +125.8%  1M ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, trendColor.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${deltaSign}${CurrencyHelper.formatRupiah(Math.abs(displayDelta))}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = trendColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${deltaSign}${String.format(java.util.Locale.US, "%.1f", Math.abs(displayPercent))}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = trendColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedPeriod,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Subtext Plan note & Total Saldo caption
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitle ?: "📈 Dalam 30 hari, sesuai rencana: ${CurrencyHelper.formatRupiah(planAmount)}",
                    fontSize = 11.sp,
                    color = TextSubtle
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title ?: "Total Saldo Tersedia · $walletCount Dompet",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Timeframe Selector Capsule Tabs & Fullscreen Expand button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timeframe Selector Tabs
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        periods.forEach { p ->
                            val isSelected = selectedPeriod == p
                            val tabBg = if (isSelected) FinancialChartTheme.NeonGreen.copy(alpha = 0.22f) else Color.Transparent
                            val tabTextColor = if (isSelected) FinancialChartTheme.NeonGreen else TextMuted
                            val tabBorder = if (isSelected) BorderStroke(1.dp, FinancialChartTheme.NeonGreen.copy(alpha = 0.6f)) else null

                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = tabBg,
                                border = tabBorder,
                                modifier = Modifier.clickable {
                                    selectedPeriod = p
                                    scrubbedPoint = null
                                }
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = tabTextColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Fullscreen Expand icon button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { showExpandedSheet = true }
                ) {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Perluas grafik",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Interactive Canvas Chart
            FinancialLineCanvas(
                points = activePoints,
                lineColor = FinancialChartTheme.NeonGreen,
                onScrub = { point -> scrubbedPoint = point },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 6. X-Axis Date Labels (Left and Right dates)
            val firstDate = activePoints.firstOrNull()?.shortDateLabel ?: ""
            val lastDate = activePoints.lastOrNull()?.shortDateLabel ?: ""

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = firstDate,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = lastDate,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // 7. Expanded Modal Bottom Sheet matching video at 00:18
    if (showExpandedSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showExpandedSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF101725),
            dragHandle = {
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = Color(0xFF475569),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(width = 36.dp, height = 4.dp)
                ) {}
            }
        ) {
            ExpandedFinancialChartView(
                points = activePoints,
                selectedPeriod = selectedPeriod,
                onDismiss = { showExpandedSheet = false }
            )
        }
    }
}

/**
 * Expanded full-view chart displayed inside the ModalBottomSheet with Y-axis scale labels
 * (e.g. 4.3M, 3.3M, 2.2M, 1.1M) as shown in the reference video at 00:18-00:20.
 */
@Composable
fun ExpandedFinancialChartView(
    points: List<FinancialDataPoint>,
    selectedPeriod: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val minVal = points.minOf { it.value }
    val maxVal = points.maxOf { it.value }

    // Format Y-axis scale values (e.g. 4.3M, 3.3M, 2.2M, 1.1M)
    val yLabels = remember(minVal, maxVal) {
        val count = 4
        val step = (maxVal - minVal) / (count - 1).coerceAtLeast(1)
        (0 until count).map { i ->
            val v = maxVal - i * step
            if (v >= 1_000_000) {
                String.format(java.util.Locale.US, "%.1fM", v / 1_000_000.0)
            } else if (v >= 1_000) {
                String.format(java.util.Locale.US, "%.0fK", v / 1_000.0)
            } else {
                v.toInt().toString()
            }
        }
    }

    var scrubbedPoint by remember { mutableStateOf<FinancialDataPoint?>(null) }
    val displayPoint = scrubbedPoint ?: points.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Sheet Title & Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Saldo Tersedia",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large balance and date readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = displayPoint?.formattedValue ?: "-",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = displayPoint?.dateLabel ?: "-",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = FinancialChartTheme.NeonGreen.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, FinancialChartTheme.NeonGreen.copy(alpha = 0.4f))
            ) {
                Text(
                    text = selectedPeriod,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FinancialChartTheme.NeonGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Chart area with Y-axis labels on left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            // Y-Axis labels
            Column(
                modifier = Modifier
                    .width(42.dp)
                    .height(240.dp)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yLabels.forEach { lbl ->
                    Text(
                        text = lbl,
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Canvas
            FinancialLineCanvas(
                points = points,
                lineColor = FinancialChartTheme.NeonGreen,
                onScrub = { scrubbedPoint = it },
                modifier = Modifier
                    .weight(1f)
                    .height(240.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // X-Axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = points.firstOrNull()?.shortDateLabel ?: "",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = points.lastOrNull()?.shortDateLabel ?: "",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
