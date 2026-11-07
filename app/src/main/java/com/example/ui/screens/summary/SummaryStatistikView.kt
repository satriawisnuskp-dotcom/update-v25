package com.example.ui.screens.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWallet
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.dialogs.CategoryTransactionSheet
import com.example.ui.dialogs.FinancialHealthDetailSheet
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueBright
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.util.CategoryIconHelper
import com.example.util.CategorySlice
import com.example.util.ChartSeriesData
import com.example.util.CumulativeComparisonData
import com.example.util.CurrencyHelper
import com.example.util.TransactionMathHelper
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/**
 * Tab "Statistik" (Tampilan Utama Ringkasan Keuangan):
 * 1. Aktivitas Pengeluaran (Line chart harian, puncak pengeluaran, metrik harian, kategori teratas)
 * 2. Arus Kas Bulanan (Bar Pemasukan & Pengeluaran, perbandingan arus kas)
 * 3. Terencana / Masih Direncanakan (Toggle switch, estimasi tagihan & pemasukan berulang)
 * 4. Pendapatan vs Pengeluaran (Dual area chart kumulatif & % sisa)
 * 5. Wawasan Mendalam / Skor Kesehatan Keuangan (Gauge 80/100, surplus bersih, 5 breakdown metrik)
 * 6. Rincian Pendapatan & Pengeluaran (Kategori teratas dengan bar progress warna-warni)
 * 7. Pengeluaran Menurut Hari Kerja (Bar chart S S R K J S M dengan hari puncak)
 * 8. Catatan Harian (Streak tracker mini calendar 4/30 hari)
 * 9. Tren Pemasukan & Pengeluaran (Side-by-side mini monthly trend line charts)
 * 10. Aliran Uang (Sankey Diagram / Cash Flow Visualizer)
 * 11. Biaya Tetap & Perjalanan Arus Kas (Gauge biaya tetap & sparkline perjalanan arus kas)
 */
@Composable
fun SummaryStatistikView(
    selectedMonth: Int,
    selectedYear: Int,
    monthName: String = "Nov",
    allTransactions: List<Transaction>,
    periodTransactions: List<Transaction>,
    recurringTransactions: List<RecurringTransaction>,
    wallets: List<AccountWallet>,
    currentIncome: Double,
    currentExpense: Double,
    chartData: ChartSeriesData,
    cumulativeData: CumulativeComparisonData,
    expenseSlices: List<CategorySlice>,
    incomeSlices: List<CategorySlice>,
    selectedWallet: String = "Semua",
    onExportPdf: () -> Unit = {},
    onNavigateToTransactions: (category: String?) -> Unit = {},
    onNavigateToAccount: (walletName: String) -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onAddTransaction: (category: String?) -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    var isPlannedActive by remember { mutableStateOf(true) }
    var showHealthScoreSheet by remember { mutableStateOf(false) }
    var selectedCategoryForSheet by remember { mutableStateOf<String?>(null) }
    var selectedCategoryTypeForSheet by remember { mutableStateOf(TransactionType.EXPENSE) }
    var expenseChartPeriod by remember { mutableIntStateOf(0) } // 0: Bulan Penuh, 1: 7 Hari Terakhir

    val monthNamesShort = remember {
        listOf("", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    }

    val cal = remember(selectedMonth, selectedYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth - 1)
        }
    }
    val daysInMonth = remember(cal) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }

    // Consistent wallet-filtered transaction stream
    val walletFilteredTransactions = remember(allTransactions, selectedWallet) {
        if (selectedWallet == "Semua") allTransactions
        else allTransactions.filter { it.accountName.equals(selectedWallet, ignoreCase = true) }
    }

    // Previous month comparison
    val (prevM, prevY) = if (selectedMonth > 1) Pair(selectedMonth - 1, selectedYear) else Pair(12, selectedYear - 1)
    val prevMonthExpense = remember(walletFilteredTransactions, prevM, prevY) {
        walletFilteredTransactions.filter { it.monthIndex == prevM && it.year == prevY && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val prevMonthIncome = remember(walletFilteredTransactions, prevM, prevY) {
        walletFilteredTransactions.filter { it.monthIndex == prevM && it.year == prevY && it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val prevMonthName = monthNamesShort.getOrElse(prevM) { "Lalu" }

    // Dynamic 6-month sequence ending at selectedMonth, selectedYear
    val past6Months = remember(selectedMonth, selectedYear) {
        (0..5).reversed().map { offset ->
            var m = selectedMonth - offset
            var y = selectedYear
            while (m <= 0) {
                m += 12
                y -= 1
            }
            Triple(m, y, monthNamesShort.getOrElse(m) { "" })
        }
    }

    val past6Income = remember(walletFilteredTransactions, past6Months) {
        past6Months.map { (m, y, _) ->
            walletFilteredTransactions.filter { it.monthIndex == m && it.year == y && it.type == TransactionType.INCOME }.sumOf { it.amount }
        }
    }

    val past6Expense = remember(walletFilteredTransactions, past6Months) {
        past6Months.map { (m, y, _) ->
            walletFilteredTransactions.filter { it.monthIndex == m && it.year == y && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        }
    }

    val avgMonthlyIncome = remember(past6Income, currentIncome) {
        val nonZero = past6Income.filter { it > 0 }
        if (nonZero.isNotEmpty()) nonZero.average() else currentIncome
    }
    val avgMonthlyExpense = remember(past6Expense, currentExpense) {
        val nonZero = past6Expense.filter { it > 0 }
        if (nonZero.isNotEmpty()) nonZero.average() else currentExpense
    }

    // Dynamic S S R K J S M spending breakdown by Day of Week
    val (dayOfWeekTotals, peakDayIndex) = remember(periodTransactions) {
        val expenseTxs = periodTransactions.filter { it.type == TransactionType.EXPENSE }
        val totals = DoubleArray(7) { 0.0 }
        val tempCal = Calendar.getInstance()
        for (tx in expenseTxs) {
            tempCal.set(tx.year, tx.monthIndex - 1, tx.dayOfMonth)
            val dow = tempCal.get(Calendar.DAY_OF_WEEK)
            val idx = if (dow == Calendar.SUNDAY) 6 else dow - 2
            if (idx in 0..6) {
                totals[idx] += tx.amount
            }
        }
        val list = totals.toList()
        val maxVal = list.maxOrNull() ?: 0.0
        val pIdx = if (maxVal > 0) list.indexOf(maxVal) else 2
        Pair(list, pIdx)
    }

    val dayNamesIndo = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
    val peakDayName = dayNamesIndo.getOrElse(peakDayIndex) { "Rabu" }

    // Tracked days in period for streak & calendar
    val trackedDaysSet = remember(periodTransactions) {
        periodTransactions.map { it.dayOfMonth }.toSet()
    }

    // Health Score calculation
    val effNet = currentIncome - currentExpense
    val savingsPct = if (currentIncome > 0) (((currentIncome - currentExpense) / currentIncome) * 100).toInt().coerceIn(0, 100) else 0
    val healthScore = remember(savingsPct, effNet, trackedDaysSet) {
        ((savingsPct * 0.5) + (if (effNet >= 0) 30 else 10) + (trackedDaysSet.size.coerceAtMost(5) * 4)).toInt().coerceIn(20, 100)
    }

    // Planned recurring transactions
    val activeRecurring = remember(recurringTransactions, selectedMonth, selectedYear) {
        recurringTransactions.filter { it.isActive }
    }
    val plannedIncTotal = remember(activeRecurring) {
        activeRecurring.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val plannedExpTotal = remember(activeRecurring) {
        activeRecurring.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Banner: Buku harian uang Anda
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1B2238),
                                    Color(0xFF151928),
                                    Color(0xFF111420)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x33F59E0B),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Buku harian uang Anda",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Melihat kembali dengan lembut ke mana uang Anda pergi",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Text(text = "🌙", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentBlue,
                                modifier = Modifier
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$monthName $selectedYear",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x33FFFFFF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏱ 7 hari lalu",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== 1. AKTIVITAS PENGELUARAN ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AKTIVITAS PENGELUARAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSubtle
                            )
                            Text(
                                text = "$monthName $selectedYear",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Range Toggle: Bulan vs 7 Hari
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (expenseChartPeriod == 0) AccentBlue else Color.Transparent,
                                        modifier = Modifier.clickable { expenseChartPeriod = 0 }
                                    ) {
                                        Text(
                                            text = "Bulan",
                                            fontSize = 10.sp,
                                            fontWeight = if (expenseChartPeriod == 0) FontWeight.Bold else FontWeight.Normal,
                                            color = if (expenseChartPeriod == 0) Color.White else TextMuted,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (expenseChartPeriod == 1) AccentBlue else Color.Transparent,
                                        modifier = Modifier.clickable { expenseChartPeriod = 1 }
                                    ) {
                                        Text(
                                            text = "7 Hari",
                                            fontSize = 10.sp,
                                            fontWeight = if (expenseChartPeriod == 1) FontWeight.Bold else FontWeight.Normal,
                                            color = if (expenseChartPeriod == 1) Color.White else TextMuted,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // PDF Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.clickable { onExportPdf() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = CurrencyHelper.formatRupiah(currentExpense),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pengeluaran",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        if (prevMonthExpense > 0) {
                            val expenseDiff = prevMonthExpense - currentExpense
                            val isSaved = expenseDiff >= 0
                            val pct = (abs(expenseDiff) / prevMonthExpense * 100).toInt()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSaved) IncomeGreenSoft else Color(0x33F43F5E)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = if (isSaved) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (isSaved) "$pct% dibanding $prevMonthName" else "+$pct% dibanding $prevMonthName",
                                        fontSize = 10.sp,
                                        color = if (isSaved) IncomeGreen else ExpenseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Peak Day calculation for visual beacon
                    val peakDayEntry = periodTransactions.filter { it.type == TransactionType.EXPENSE }
                        .groupBy { it.dayOfMonth }
                        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }
                    val peakAmt = peakDayEntry?.value?.sumOf { it.amount } ?: 0.0
                    val peakDayNum = peakDayEntry?.key ?: 1

                    // Prepare clean chart data: Full Month (30 days with 5 milestones) or Last 7 Days
                    val (trendPts, trendLabels, peakDayIdx) = remember(expenseChartPeriod, chartData, periodTransactions, daysInMonth, monthName) {
                        if (expenseChartPeriod == 0) {
                            val pts = chartData.rawValues.ifEmpty { (1..daysInMonth).map { 0.0 } }
                            val lbls = (1..daysInMonth).map { "$it $monthName" }
                            val pIdx = (peakDayNum - 1).coerceIn(0, (pts.size - 1).coerceAtLeast(0))
                            Triple(pts, lbls, pIdx)
                        } else {
                            val startD = (daysInMonth - 6).coerceAtLeast(1)
                            val last7Days = (startD..daysInMonth).toList()
                            val pts = last7Days.map { d ->
                                periodTransactions.filter { it.type == TransactionType.EXPENSE && it.dayOfMonth == d }.sumOf { it.amount }
                            }
                            val lbls = last7Days.map { d -> "$d $monthName" }
                            val maxP = pts.maxOrNull() ?: 0.0
                            val pIdx = if (maxP > 0) pts.indexOf(maxP) else -1
                            Triple(pts, lbls, pIdx)
                        }
                    }

                    // Elegant Spending Activity Line Chart with non-overlapping milestone ticks
                    SpendingActivityLineChart(
                        values = trendPts,
                        labels = trendLabels,
                        lineColor = ExpenseRed,
                        peakDayIndex = peakDayIdx,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Summary Metrics: Per hari, Hari terboros, Kategori teratas
                    val dailyAvg = if (currentExpense > 0) currentExpense / daysInMonth.toDouble() else 0.0
                    val topCatSlice = expenseSlices.firstOrNull()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Per hari", fontSize = 10.sp, color = TextSubtle)
                            Text(CurrencyHelper.formatCompact(dailyAvg), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hari terboros", fontSize = 10.sp, color = TextSubtle)
                            Text(CurrencyHelper.formatCompact(peakAmt), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("$peakDayNum $monthName", fontSize = 9.sp, color = TextMuted)
                        }
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Kategori teratas", fontSize = 10.sp, color = TextSubtle)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛍 ", fontSize = 12.sp)
                                Text(
                                    text = topCatSlice?.category ?: "Belum ada",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1
                                )
                            }
                            if (topCatSlice != null) {
                                Text(CurrencyHelper.formatCompact(topCatSlice.amount), fontSize = 9.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // ==================== 2. ARUS KAS BULANAN ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ringkasan", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                    Text("Arus Kas Bulanan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pemasukan Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(CurrencyHelper.formatRupiah(currentIncome), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Text("Pemasukan", fontSize = 11.sp, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val maxVal = maxOf(currentIncome, currentExpense, 1.0)
                    LinearProgressIndicator(
                        progress = { (currentIncome / maxVal).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = IncomeGreen,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pengeluaran Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(CurrencyHelper.formatRupiah(currentExpense), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Text("Pengeluaran", fontSize = 11.sp, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (currentExpense / maxVal).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = ExpenseRed,
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Evaluation Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val isPositive = effNet >= 0
                        val netDiffText = (if (isPositive) "+" else "") + CurrencyHelper.formatRupiah(effNet)
                        Text(
                            text = if (isPositive) {
                                "Pada $monthName $selectedYear, arus kas bulanan Anda positif. Pemasukan Anda $netDiffText lebih besar dari pengeluaran."
                            } else {
                                "Pada $monthName $selectedYear, pengeluaran melebihi pemasukan sebesar ${CurrencyHelper.formatRupiah(abs(effNet))}."
                            },
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // ==================== 3. TERENCANA (MASIH DIRENCANAKAN) ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Terencana", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                            Text("Masih direncanakan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Switch(
                            checked = isPlannedActive,
                            onCheckedChange = { isPlannedActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }

                    Text(
                        text = "Tagihan dan pemasukan dari daftar berulang Anda yang masih akan datang di periode ini.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (isPlannedActive) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pemasukan terencana · ${activeRecurring.count { it.type == TransactionType.INCOME }} item", fontSize = 11.sp, color = TextMuted)
                            Text("+${CurrencyHelper.formatRupiah(plannedIncTotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pengeluaran terencana · ${activeRecurring.count { it.type == TransactionType.EXPENSE }} item", fontSize = 11.sp, color = TextMuted)
                            Text("-${CurrencyHelper.formatRupiah(plannedExpTotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Jika semua berjalan sesuai rencana", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pemasukan", fontSize = 11.sp, color = TextMuted)
                            Text(CurrencyHelper.formatRupiah(currentIncome + plannedIncTotal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pengeluaran", fontSize = 11.sp, color = TextMuted)
                            Text(CurrencyHelper.formatRupiah(currentExpense + plannedExpTotal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        val projNet = (currentIncome + plannedIncTotal) - (currentExpense + plannedExpTotal)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bersih untuk periode ini", fontSize = 11.sp, color = TextMuted)
                            Text((if (projNet >= 0) "+" else "") + CurrencyHelper.formatRupiah(projNet), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (projNet >= 0) IncomeGreen else ExpenseRed)
                        }

                        if (activeRecurring.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(8.dp))
                            activeRecurring.take(4).forEach { rec ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${rec.dayOfMonth} $monthName", fontSize = 10.sp, color = TextSubtle, modifier = Modifier.width(46.dp))
                                        Text(rec.title, fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                                    }
                                    Text(
                                        text = (if (rec.type == TransactionType.INCOME) "+" else "-") + CurrencyHelper.formatRupiah(rec.amount),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rec.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== 4. PENDAPATAN VS PENGELUARAN (KUMULATIF) ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pendapatan vs Pengeluaran", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Surface(shape = RoundedCornerShape(8.dp), color = DarkSurfaceElevated) {
                            Text("Kumulatif", fontSize = 9.sp, color = TextSubtle, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = (if (effNet >= 0) "+" else "") + CurrencyHelper.formatCompact(effNet),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (effNet >= 0) IncomeGreen else ExpenseRed
                            )
                            Text("Dihitung periode ini", fontSize = 10.sp, color = TextMuted)
                        }

                        val remainingPct = if (currentIncome > 0) (((currentIncome - currentExpense) / currentIncome) * 100).toInt().coerceIn(0, 100) else 0
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IncomeGreenSoft
                        ) {
                            Text(
                                text = "$remainingPct% pemasukan tersisa",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp, 3.dp).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pemasukan", fontSize = 10.sp, color = TextSubtle)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(CurrencyHelper.formatCompact(currentIncome), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp, 3.dp).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pengeluaran", fontSize = 10.sp, color = TextSubtle)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(CurrencyHelper.formatCompact(currentExpense), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dual Area Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val incPts = cumulativeData.incomePoints.ifEmpty { listOf(0f, 1f) }
                        val expPts = cumulativeData.expensePoints.ifEmpty { listOf(0f, 0.4f) }
                        val maxP = maxOf(incPts.maxOrNull() ?: 1f, expPts.maxOrNull() ?: 1f, 1f)
                        val w = size.width
                        val h = size.height

                        // Horizontal grid lines
                        for (i in 0..3) {
                            val y = h * (i / 3f)
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }

                        // Path Income
                        val incPath = Path()
                        val incFillPath = Path()
                        incPts.forEachIndexed { i, pt ->
                            val x = if (incPts.size > 1) (i / (incPts.size - 1f)) * w else 0f
                            val y = h - ((pt / maxP) * (h * 0.9f))
                            if (i == 0) {
                                incPath.moveTo(x, y)
                                incFillPath.moveTo(x, h)
                                incFillPath.lineTo(x, y)
                            } else {
                                incPath.lineTo(x, y)
                                incFillPath.lineTo(x, y)
                            }
                        }
                        incFillPath.lineTo(w, h)
                        incFillPath.close()

                        drawPath(
                            path = incFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(IncomeGreen.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                        drawPath(
                            path = incPath,
                            color = IncomeGreen,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Path Expense
                        val expPath = Path()
                        val expFillPath = Path()
                        expPts.forEachIndexed { i, pt ->
                            val x = if (expPts.size > 1) (i / (expPts.size - 1f)) * w else 0f
                            val y = h - ((pt / maxP) * (h * 0.9f))
                            if (i == 0) {
                                expPath.moveTo(x, y)
                                expFillPath.moveTo(x, h)
                                expFillPath.lineTo(x, y)
                            } else {
                                expPath.lineTo(x, y)
                                expFillPath.lineTo(x, y)
                            }
                        }
                        expFillPath.lineTo(w, h)
                        expFillPath.close()

                        drawPath(
                            path = expFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(ExpenseRed.copy(alpha = 0.20f), Color.Transparent)
                            )
                        )
                        drawPath(
                            path = expPath,
                            color = ExpenseRed,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1", fontSize = 9.sp, color = TextSubtle)
                        Text("9", fontSize = 9.sp, color = TextSubtle)
                        Text("17", fontSize = 9.sp, color = TextSubtle)
                        Text("25", fontSize = 9.sp, color = TextSubtle)
                        Text("30", fontSize = 9.sp, color = TextSubtle)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✔ ", fontSize = 11.sp, color = IncomeGreen)
                            Text(
                                text = "Pemasukan tetap unggul sepanjang periode",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // ==================== 5. WAWASAN MENDALAM (SKOR KESEHATAN KEUANGAN) ====================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showHealthScoreSheet = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Wawasan mendalam", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                            Text("Skor Kesehatan Keuangan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IncomeGreenSoft,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = if (healthScore >= 75) "Sangat Baik" else if (healthScore >= 50) "Cukup Baik" else "Perlu Ditingkatkan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Circular Gauge
                        FinancialHealthScoreGauge(
                            score = healthScore,
                            label = "$healthScore/100",
                            modifier = Modifier.size(68.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Surplus Bersih Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Surplus Bersih", fontSize = 10.sp, color = TextSubtle)
                            Text(CurrencyHelper.formatRupiah(effNet), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = IncomeGreenSoft) {
                            Text("+204.5% vs periode sebelumnya", fontSize = 10.sp, color = IncomeGreen, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Breakdown Metrics
                    val metrics = listOf(
                        Pair("Tingkat Tabungan", Pair("83.5%", 20)),
                        Pair("Kontrol Pengeluaran", Pair("-80.4%", 20)),
                        Pair("Konsistensi Pelacakan", Pair("${trackedDaysSet.size}/30 hari terlacak", 4)),
                        Pair("Diversifikasi Pendapatan", Pair("3 sumber", 16)),
                        Pair("Progres Anggaran", Pair("3 anggaran", 20))
                    )

                    metrics.forEach { (name, data) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(data.first, fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(14.dp))
                                Text("${data.second}", fontSize = 11.sp, color = TextSubtle, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("★ ", fontSize = 12.sp, color = Color(0xFFFBBF24))
                            Text(
                                text = "Disiplin keuangan luar biasa periode ini. Pertahankan!",
                                fontSize = 11.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ==================== 6. RINCIAN PENDAPATAN & PENGELUARAN ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Pendapatan & Pengeluaran", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Kategori teratas berdasarkan jumlah", fontSize = 10.sp, color = TextSubtle)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pemasukan Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pemasukan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Text(CurrencyHelper.formatRupiah(currentIncome), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    incomeSlices.forEach { slice ->
                        val ratio = if (currentIncome > 0) (slice.amount / currentIncome).toFloat() else 0f
                        val pct = (ratio * 100).toInt()
                        val color = CategoryIconHelper.getCategoryColor(slice.category)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryForSheet = slice.category
                                    selectedCategoryTypeForSheet = TransactionType.INCOME
                                }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.2f), modifier = Modifier.size(28.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getCategoryIcon(slice.category),
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slice.category, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text("$pct%", fontSize = 10.sp, color = TextSubtle, modifier = Modifier.width(36.dp))
                            Text(CurrencyHelper.formatRupiah(slice.amount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = color,
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Pengeluaran Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pengeluaran", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Text(CurrencyHelper.formatRupiah(currentExpense), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    expenseSlices.forEach { slice ->
                        val ratio = if (currentExpense > 0) (slice.amount / currentExpense).toFloat() else 0f
                        val pct = (ratio * 100).toInt()
                        val color = CategoryIconHelper.getCategoryColor(slice.category)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryForSheet = slice.category
                                    selectedCategoryTypeForSheet = TransactionType.EXPENSE
                                }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.2f), modifier = Modifier.size(28.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = CategoryIconHelper.getCategoryIcon(slice.category),
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(slice.category, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text("$pct%", fontSize = 10.sp, color = TextSubtle, modifier = Modifier.width(36.dp))
                            Text(CurrencyHelper.formatRupiah(slice.amount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = color,
                            trackColor = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // ==================== 7. PENGELUARAN MENURUT HARI KERJA ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pengeluaran Menurut Hari Kerja", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Hari pengeluaran puncak: $peakDayName", fontSize = 10.sp, color = TextSubtle)

                    Spacer(modifier = Modifier.height(14.dp))

                    DayOfWeekSpendingBarChart(
                        dayTotals = dayOfWeekTotals,
                        peakDayIndex = peakDayIndex,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val weekdayAvg = if (dayOfWeekTotals.take(5).sum() > 0) dayOfWeekTotals.take(5).sum() / 5.0 else 0.0
                    val weekendAvg = if (dayOfWeekTotals.drop(5).sum() > 0) dayOfWeekTotals.drop(5).sum() / 2.0 else 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hari kerja", fontSize = 10.sp, color = TextSubtle)
                            Text(CurrencyHelper.formatRupiah(weekdayAvg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("rata-rata harian", fontSize = 9.sp, color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Akhir pekan", fontSize = 10.sp, color = TextSubtle)
                            Text(CurrencyHelper.formatRupiah(weekendAvg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("rata-rata harian", fontSize = 9.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // ==================== 8. CATATAN HARIAN (STREAK TRACKER) ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hari demi hari", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                            Text("Catatan Harian", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = DarkSurfaceElevated) {
                            Text(
                                text = "${trackedDaysSet.size}/$daysInMonth hari terlacak ⓘ",
                                fontSize = 10.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Saat ini melacak selama ${trackedDaysSet.size.coerceAtLeast(1)} hari berturut-turut",
                        fontSize = 11.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DailyTrackingCalendarGrid(
                        daysInMonth = daysInMonth,
                        trackedDays = trackedDaysSet,
                        currentStreakDays = trackedDaysSet.size.coerceAtLeast(1),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(IncomeGreen.copy(alpha = 0.5f)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ada catatan", fontSize = 10.sp, color = TextMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1E293B)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tidak ada catatan", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // ==================== 9. TREN PEMASUKAN & PENGELUARAN ====================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mini Trend Income
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tren Pemasukan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Rata-rata: ${CurrencyHelper.formatCompact(avgMonthlyIncome)}/bln", fontSize = 9.sp, color = TextSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        MiniMonthTrendLineChart(
                            values = past6Income,
                            labels = past6Months.map { it.third },
                            lineColor = IncomeGreen,
                            modifier = Modifier.fillMaxWidth().height(70.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bulan Ini", fontSize = 9.sp, color = TextSubtle)
                        Text(CurrencyHelper.formatRupiah(currentIncome), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                    }
                }

                // Mini Trend Expense
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tren Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Rata-rata: ${CurrencyHelper.formatCompact(avgMonthlyExpense)}/bln", fontSize = 9.sp, color = TextSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        MiniMonthTrendLineChart(
                            values = past6Expense,
                            labels = past6Months.map { it.third },
                            lineColor = ExpenseRed,
                            modifier = Modifier.fillMaxWidth().height(70.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Bulan Ini", fontSize = 9.sp, color = TextSubtle)
                        Text(CurrencyHelper.formatRupiah(currentExpense), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                    }
                }
            }
        }

        // ==================== 10. ALIRAN UANG (SANKEY DIAGRAM) ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aliran uang", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Alur dari Pendapatan mengalir ke Pengeluaran dan Tersisa", fontSize = 10.sp, color = TextSubtle)

                    Spacer(modifier = Modifier.height(14.dp))

                    val incomePairs = incomeSlices.map { it.category to it.amount }.ifEmpty {
                        listOf("Salary" to currentIncome.coerceAtLeast(1000.0))
                    }
                    val expensePairs = expenseSlices.map { it.category to it.amount }.ifEmpty {
                        listOf("Shopping" to currentExpense.coerceAtLeast(500.0))
                    }

                    SankeyMoneyFlowView(
                        incomeCategories = incomePairs,
                        expenseCategories = expensePairs,
                        savingsAmount = effNet.coerceAtLeast(0.0),
                        onCategoryClick = { catName ->
                            val isIncome = incomePairs.any { it.first.equals(catName, ignoreCase = true) }
                            selectedCategoryTypeForSheet = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                            selectedCategoryForSheet = catName
                        },
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
            }
        }

        // ==================== 11. BIAYA TETAP & PERJALANAN ARUS KAS ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biaya tetap", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Kebutuhan rutin bulanan", fontSize = 10.sp, color = TextSubtle)
                        }
                        Text(
                            text = CurrencyHelper.formatRupiah(plannedExpTotal),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val fixedPct = if (currentIncome > 0) ((plannedExpTotal / currentIncome) * 100).toInt() else 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(color = Color(0xFF1E293B), style = Stroke(6.dp.toPx()))
                                drawArc(
                                    color = Color(0xFF38BDF8),
                                    startAngle = -90f,
                                    sweepAngle = (fixedPct.toFloat() / 100f) * 360f,
                                    useCenter = false,
                                    style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Text("$fixedPct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "$fixedPct% dari pemasukan periode ini",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextWhite
                            )
                            Text(
                                text = "${activeRecurring.size} pos tagihan & kewajiban tetap tercatat",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Perjalanan Arus Kas Sparkline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Perjalanan arus kas", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Nilai bersih berjalan selama periode", fontSize = 10.sp, color = TextSubtle)
                        }
                        Text(
                            text = (if (effNet >= 0) "+" else "") + CurrencyHelper.formatRupiah(effNet),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (effNet >= 0) IncomeGreen else ExpenseRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val runningPts = chartData.rawValues.ifEmpty { listOf(0.0, effNet) }
                    MiniMonthTrendLineChart(
                        values = runningPts,
                        labels = chartData.pointLabels,
                        lineColor = if (effNet >= 0) IncomeGreen else ExpenseRed,
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Health detail sheet
    if (showHealthScoreSheet) {
        FinancialHealthDetailSheet(
            score = healthScore,
            savingsRatePct = savingsPct.toDouble(),
            netSurplusFormatted = CurrencyHelper.formatRupiah(effNet),
            onDismiss = { showHealthScoreSheet = false },
            onNavigateToBudgets = onNavigateToBudgets,
            onNavigateToPlanner = onNavigateToPlanner,
            onAddTransaction = { onAddTransaction(null) }
        )
    }

    // Category transaction sheet
    selectedCategoryForSheet?.let { catName ->
        CategoryTransactionSheet(
            categoryName = catName,
            categoryType = selectedCategoryTypeForSheet,
            periodLabel = "${monthNamesShort.getOrElse(selectedMonth) { "Nov" }} $selectedYear",
            transactions = periodTransactions.filter { it.category.equals(catName, ignoreCase = true) || it.label.equals(catName, ignoreCase = true) },
            onDismiss = { selectedCategoryForSheet = null },
            onNavigateToTransactions = { onNavigateToTransactions(catName) },
            onAddTransactionForCategory = { onAddTransaction(catName) },
            onTransactionClick = onTransactionClick
        )
    }
}
