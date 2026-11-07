package com.example.ui.screens.summary

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWallet
import com.example.data.model.AssetItem
import com.example.data.model.RecurringTransaction
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.dialogs.CategoryTransactionSheet
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
import com.example.ui.viewmodel.MonthOption
import com.example.util.CategoryIconHelper
import com.example.util.CategorySlice
import com.example.util.CurrencyHelper
import kotlin.math.abs

/**
 * Tab "Laporan":
 * 1. Ringkasan Tahunan (Pertumbuhan aset tahunan, total pendapatan, pengeluaran, bersih, alokasi aset)
 * 2. Rincian Bulanan (Tabel komparasi bulanan Jan-Des & metrik rata-rata, kartu tabungan & aset)
 * 3. Donut Chart Kategori (Grafik donat interaktif Pemasukan & Pengeluaran)
 * 4. Pengeluaran per Label (Donat chart dan statistik nominal & persentase per label)
 * 5. Periode Ini vs Lalu & Kategori Paling Berubah (Perbandingan komparatif)
 */
@Composable
fun SummaryLaporanView(
    selectedMonth: Int,
    selectedYear: Int,
    monthOptions: List<MonthOption>,
    allTransactions: List<Transaction>,
    periodTransactions: List<Transaction>,
    wallets: List<AccountWallet>,
    assets: List<AssetItem>,
    currentIncome: Double,
    currentExpense: Double,
    expenseSlices: List<CategorySlice>,
    incomeSlices: List<CategorySlice>,
    selectedWallet: String = "Semua",
    onExportPdf: () -> Unit = {},
    onNavigateToTransactions: (category: String?) -> Unit = {},
    onNavigateToAccount: (walletName: String) -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onSelectMonth: (monthIndex: Int) -> Unit = {},
    onSelectYear: (year: Int) -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val monthName = monthOptions.find { it.first == selectedMonth }?.second ?: "Nov"
    var selectedCategoryForSheet by remember { mutableStateOf<String?>(null) }
    var selectedCategoryTypeForSheet by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedAssetTab by remember { mutableIntStateOf(0) } // 0: Pertumbuhan Aset, 1: Total Pendapatan, 2: Total Pengeluaran

    val monthNamesShort = remember {
        listOf("", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    }

    // Consistent wallet-filtered transaction stream
    val walletFilteredTransactions = remember(allTransactions, selectedWallet) {
        if (selectedWallet == "Semua") allTransactions
        else allTransactions.filter { it.accountName.equals(selectedWallet, ignoreCase = true) }
    }

    // Annual computations for Tahunan view
    val yearTransactions = remember(walletFilteredTransactions, selectedYear) {
        walletFilteredTransactions.filter { it.year == selectedYear }
    }
    val yearIncome = remember(yearTransactions) {
        yearTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val yearExpense = remember(yearTransactions) {
        yearTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val yearNet = yearIncome - yearExpense

    val totalWalletBalance = remember(wallets) { wallets.sumOf { it.balance } }
    val totalAssetValue = remember(assets) { assets.sumOf { it.value } }
    val totalWealth = totalWalletBalance + totalAssetValue
    val walletPct = if (totalWealth > 0) ((totalWalletBalance / totalWealth) * 100).toInt() else 28
    val assetPct = if (totalWealth > 0) ((totalAssetValue / totalWealth) * 100).toInt() else 72

    // Monthly data across all 12 months of selectedYear
    val yearMonthlyData = remember(yearTransactions, selectedYear) {
        (1..12).map { m ->
            val inc = yearTransactions.filter { it.monthIndex == m && it.type == TransactionType.INCOME }.sumOf { it.amount }
            val exp = yearTransactions.filter { it.monthIndex == m && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            Triple(m, inc, exp)
        }
    }
    val activeYearMonths = remember(yearMonthlyData) {
        yearMonthlyData.filter { it.second > 0 || it.third > 0 }
    }
    val bestMonthIdx = remember(activeYearMonths) {
        activeYearMonths.maxByOrNull { it.second - it.third }?.first ?: selectedMonth
    }
    val worstMonthIdx = remember(activeYearMonths) {
        activeYearMonths.minByOrNull { it.second - it.third }?.first ?: 1
    }

    val avgYearlyInc = if (activeYearMonths.isNotEmpty()) activeYearMonths.map { it.second }.average() else currentIncome
    val avgYearlyExp = if (activeYearMonths.isNotEmpty()) activeYearMonths.map { it.third }.average() else currentExpense
    val avgYearlyNet = avgYearlyInc - avgYearlyExp
    val yearlySavingsRate = if (yearIncome > 0) (((yearIncome - yearExpense) / yearIncome) * 100).toInt() else 27

    // Previous month comparison
    val (prevM, prevY) = if (selectedMonth > 1) Pair(selectedMonth - 1, selectedYear) else Pair(12, selectedYear - 1)
    val prevMonthExpense = remember(walletFilteredTransactions, prevM, prevY) {
        walletFilteredTransactions.filter { it.monthIndex == prevM && it.year == prevY && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }
    val prevMonthIncome = remember(walletFilteredTransactions, prevM, prevY) {
        walletFilteredTransactions.filter { it.monthIndex == prevM && it.year == prevY && it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val prevMonthNet = prevMonthIncome - prevMonthExpense
    val prevMonthName = monthNamesShort.getOrElse(prevM) { "Okt" }

    // Label Breakdown: Group transactions by label
    val labelBreakdown = remember(periodTransactions) {
        val expenseTxs = periodTransactions.filter { it.type == TransactionType.EXPENSE }
        val grouped = expenseTxs.groupBy { it.label.ifBlank { it.category } }
        val total = grouped.values.sumOf { txs -> txs.sumOf { it.amount } }.coerceAtLeast(1.0)
        val colors = listOf(
            Color(0xFF38BDF8),
            Color(0xFFF43F5E),
            Color(0xFFFBBF24),
            Color(0xFF10B981),
            Color(0xFF818CF8),
            Color(0xFFA855F7)
        )
        grouped.entries.mapIndexed { idx, entry ->
            val amt = entry.value.sumOf { it.amount }
            DonutSliceData(
                label = entry.key,
                amount = amt,
                percentage = (amt / total).toFloat(),
                color = colors[idx % colors.size]
            )
        }.sortedByDescending { it.amount }
    }

    // Top Changed Categories comparison (Month vs Prev Month)
    val categoryDeltas = remember(walletFilteredTransactions, selectedMonth, selectedYear, prevM, prevY) {
        val curTxs = walletFilteredTransactions.filter { it.monthIndex == selectedMonth && it.year == selectedYear && it.type == TransactionType.EXPENSE }
        val prevTxs = walletFilteredTransactions.filter { it.monthIndex == prevM && it.year == prevY && it.type == TransactionType.EXPENSE }
        val curCatMap = curTxs.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
        val prevCatMap = prevTxs.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
        val allCats = (curCatMap.keys + prevCatMap.keys).toSet()
        allCats.map { cat ->
            val c = curCatMap[cat] ?: 0.0
            val p = prevCatMap[cat] ?: 0.0
            val diff = c - p
            Triple(cat, diff, c)
        }.sortedBy { it.second } // Most reduced or increased
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==================== 1. RINGKASAN TAHUNAN ====================
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
                            Text("Ringkasan Tahunan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Pertumbuhan aset, pendapatan & pengeluaran", fontSize = 10.sp, color = TextSubtle)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSelectYear(selectedYear - 1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Tahun Lalu", tint = TextWhite, modifier = Modifier.size(18.dp))
                            }
                            Text("$selectedYear", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            IconButton(
                                onClick = { onSelectYear(selectedYear + 1) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Tahun Depan", tint = TextWhite, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                            IconButton(onClick = onExportPdf, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = AccentBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 Metric Cards: Total Pendapatan, Total Pengeluaran, Bersih
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(IncomeGreen))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Total Pendapatan", fontSize = 9.sp, color = TextSubtle)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(CurrencyHelper.formatCompact(yearIncome), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ExpenseRed))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Total Pengeluaran", fontSize = 9.sp, color = TextSubtle)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(CurrencyHelper.formatCompact(yearExpense), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentBlueBright))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bersih", fontSize = 9.sp, color = TextSubtle)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text((if (yearNet >= 0) "+" else "") + CurrencyHelper.formatCompact(yearNet), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (yearNet >= 0) AccentBlueBright else ExpenseRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pertumbuhan Aset Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌱", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pertumbuhan Aset", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${CurrencyHelper.formatRupiah(totalWealth)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = IncomeGreenSoft) {
                                Text("+0.8%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Asset Allocation
                    Text("Asset allocation", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToAccount("Semua") },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF38BDF8)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tunai & Bank", fontSize = 12.sp, color = TextWhite)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(CurrencyHelper.formatRupiah(totalWalletBalance), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$walletPct%", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToAssets() },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF818CF8)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Investasi", fontSize = 12.sp, color = TextWhite)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(CurrencyHelper.formatRupiah(totalAssetValue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$assetPct%", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector for Trend: Pertumbuhan Aset, Total Pendapatan, Total Pengeluaran
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Pertumbuhan Aset", "Total Pendapatan", "Total Pengeluaran").forEachIndexed { idx, tabTitle ->
                            val isSel = selectedAssetTab == idx
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) AccentBlue else DarkSurfaceElevated,
                                modifier = Modifier.clickable { selectedAssetTab = idx }
                            ) {
                                Text(
                                    text = tabTitle,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val trendPoints = remember(selectedAssetTab, yearMonthlyData) {
                        when (selectedAssetTab) {
                            1 -> yearMonthlyData.map { it.second }
                            2 -> yearMonthlyData.map { it.third }
                            else -> yearMonthlyData.map { (it.second - it.third).coerceAtLeast(0.0) }
                        }
                    }

                    MiniMonthTrendLineChart(
                        values = trendPoints,
                        labels = listOf("Jan", "Mar", "Mei", "Jul", "Sep", "Nov", "Des"),
                        lineColor = when (selectedAssetTab) {
                            1 -> IncomeGreen
                            2 -> ExpenseRed
                            else -> AccentBlueBright
                        },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            }
        }

        // ==================== 2. RICIAN BULANAN ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rincian Bulanan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Tabel komparasi bulanan tahun $selectedYear", fontSize = 10.sp, color = TextSubtle)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Table Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bulan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.1f))
                        Text("Pemasukan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                        Text("Pengeluaran", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                        Text("Bersih", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                        Text("Perubahan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(4.dp))

                    val activeMonthsList = (1..12).filter { m ->
                        val item = yearMonthlyData[m - 1]
                        item.second > 0 || item.third > 0 || m == selectedMonth
                    }

                    activeMonthsList.forEach { m ->
                        val item = yearMonthlyData[m - 1]
                        val inc = item.second
                        val exp = item.third
                        val net = inc - exp
                        val isCurrent = m == selectedMonth
                        val isBest = m == bestMonthIdx && activeYearMonths.isNotEmpty()
                        val mName = monthNamesShort.getOrElse(m) { "" }

                        // Month over month change
                        val prevItem = if (m > 1) yearMonthlyData[m - 2] else null
                        val prevExp = prevItem?.third ?: 0.0
                        val expDeltaPct = if (prevExp > 0) (((exp - prevExp) / prevExp) * 100).toInt() else 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) AccentBlue.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { onSelectMonth(m) }
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mName + (if (isBest) " ★" else ""),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent || isBest) FontWeight.Bold else FontWeight.Medium,
                                color = if (isBest) IncomeGreen else TextWhite,
                                modifier = Modifier.weight(1.1f)
                            )
                            Text(CurrencyHelper.formatCompact(inc), fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                            Text(CurrencyHelper.formatCompact(exp), fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                            Text(
                                text = (if (net >= 0) "+" else "") + CurrencyHelper.formatCompact(net),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (net >= 0) IncomeGreen else ExpenseRed,
                                modifier = Modifier.weight(1.3f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = if (expDeltaPct == 0) "0%" else (if (expDeltaPct > 0) "+$expDeltaPct%" else "$expDeltaPct%"),
                                fontSize = 9.5.sp,
                                color = if (expDeltaPct <= 0) IncomeGreen else ExpenseRed,
                                modifier = Modifier.weight(1.1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // 6 Average Metric Cards
                    val statGrid = listOf(
                        Pair("Pemasukan Bulanan Rata-rata", CurrencyHelper.formatCompact(avgYearlyInc)),
                        Pair("Pengeluaran Bulanan Rata-rata", CurrencyHelper.formatCompact(avgYearlyExp)),
                        Pair("Rata-rata bersih bulanan", (if (avgYearlyNet >= 0) "+" else "") + CurrencyHelper.formatCompact(avgYearlyNet)),
                        Pair("Tingkat tabungan", "$yearlySavingsRate%"),
                        Pair("Bulan terbaik", "${monthNamesShort.getOrElse(bestMonthIdx) { "Jun" }} $selectedYear 🏆"),
                        Pair("Bulan tersulit", "${monthNamesShort.getOrElse(worstMonthIdx) { "Mar" }} $selectedYear ⏳")
                    )

                    for (r in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val s1 = statGrid[r * 2]
                            val s2 = statGrid[r * 2 + 1]
                            Surface(shape = RoundedCornerShape(12.dp), color = DarkSurfaceElevated, modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(s1.first, fontSize = 9.sp, color = TextSubtle)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(s1.second, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = DarkSurfaceElevated, modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(s2.first, fontSize = 9.sp, color = TextSubtle)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(s2.second, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card: Disisihkan ke tabungan & aset
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DarkSurfaceElevated,
                        border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛡 ", fontSize = 14.sp)
                                    Text("Disisihkan ke tabungan & aset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                                Text(CurrencyHelper.formatRupiah(totalWealth), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentBlueBright)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tunai & Bank", fontSize = 11.sp, color = TextMuted)
                                Text(CurrencyHelper.formatRupiah(totalWalletBalance), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Investasi", fontSize = 11.sp, color = TextMuted)
                                Text(CurrencyHelper.formatRupiah(totalAssetValue), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                            }
                        }
                    }
                }
            }
        }

        // ==================== 3. DONUT CHART KATEGORI (PEMASUKAN & PENGELUARAN) ====================
        item {
            val incomeDonutSlices = remember(incomeSlices, currentIncome) {
                incomeSlices.map { slice ->
                    DonutSliceData(
                        label = slice.category,
                        amount = slice.amount,
                        percentage = if (currentIncome > 0) (slice.amount / currentIncome).toFloat() else 0f,
                        color = CategoryIconHelper.getCategoryColor(slice.category)
                    )
                }
            }

            CategoryDonutCard(
                title = "Pemasukan",
                totalAmount = currentIncome,
                slices = incomeDonutSlices,
                isExpense = false,
                onSliceClick = { cat ->
                    selectedCategoryForSheet = cat
                    selectedCategoryTypeForSheet = TransactionType.INCOME
                }
            )
        }

        item {
            val expenseDonutSlices = remember(expenseSlices, currentExpense) {
                expenseSlices.map { slice ->
                    DonutSliceData(
                        label = slice.category,
                        amount = slice.amount,
                        percentage = if (currentExpense > 0) (slice.amount / currentExpense).toFloat() else 0f,
                        color = CategoryIconHelper.getCategoryColor(slice.category)
                    )
                }
            }

            CategoryDonutCard(
                title = "Pengeluaran",
                totalAmount = currentExpense,
                slices = expenseDonutSlices,
                isExpense = true,
                onSliceClick = { cat ->
                    selectedCategoryForSheet = cat
                    selectedCategoryTypeForSheet = TransactionType.EXPENSE
                }
            )
        }

        // ==================== 4. PENGELUARAN PER LABEL ====================
        item {
            val labelTotal = labelBreakdown.sumOf { it.amount }
            CategoryDonutCard(
                title = "Pengeluaran per label",
                totalAmount = labelTotal,
                slices = labelBreakdown,
                isExpense = true,
                onSliceClick = { lbl ->
                    selectedCategoryForSheet = lbl
                    selectedCategoryTypeForSheet = TransactionType.EXPENSE
                }
            )
        }

        // ==================== 5. PERIODE INI VS LALU & KATEGORI PALING BERUBAH ====================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Periode ini vs lalu", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Perbandingan $monthName $selectedYear terhadap $prevMonthName", fontSize = 10.sp, color = TextSubtle)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Metrik", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.2f))
                        Text("$monthName $selectedYear", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                        Text("$prevMonthName", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Pemasukan
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pemasukan", fontSize = 11.sp, color = TextWhite, modifier = Modifier.weight(1.2f))
                        Text(CurrencyHelper.formatRupiah(currentIncome), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                        Text(CurrencyHelper.formatRupiah(prevMonthIncome), fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                    }

                    // Pengeluaran
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pengeluaran", fontSize = 11.sp, color = TextWhite, modifier = Modifier.weight(1.2f))
                        Text(CurrencyHelper.formatRupiah(currentExpense), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseRed, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                        Text(CurrencyHelper.formatRupiah(prevMonthExpense), fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                    }

                    // Bersih
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bersih", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.weight(1.2f))
                        Text((if (currentIncome - currentExpense >= 0) "+" else "") + CurrencyHelper.formatRupiah(currentIncome - currentExpense), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (currentIncome - currentExpense >= 0) IncomeGreen else ExpenseRed, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                        Text((if (prevMonthNet >= 0) "+" else "") + CurrencyHelper.formatRupiah(prevMonthNet), fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1.4f), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Kategori paling berubah", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Perubahan terbesar dibanding periode sebelumnya", fontSize = 10.sp, color = TextSubtle)

                    Spacer(modifier = Modifier.height(10.dp))

                    if (categoryDeltas.isEmpty()) {
                        Text("Belum ada perbandingan data yang cukup", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        categoryDeltas.take(5).forEach { (cat, delta, _) ->
                            val color = CategoryIconHelper.getCategoryColor(cat)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCategoryForSheet = cat
                                        selectedCategoryTypeForSheet = TransactionType.EXPENSE
                                    }
                                    .padding(vertical = 5.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.2f), modifier = Modifier.size(24.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(imageVector = CategoryIconHelper.getCategoryIcon(cat), contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = (if (delta >= 0) "+" else "-") + CurrencyHelper.formatRupiah(abs(delta)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (delta <= 0) IncomeGreen else ExpenseRed
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (delta <= 0) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = if (delta <= 0) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    selectedCategoryForSheet?.let { catName ->
        CategoryTransactionSheet(
            categoryName = catName,
            categoryType = selectedCategoryTypeForSheet,
            periodLabel = "Tahun $selectedYear",
            transactions = periodTransactions.filter { it.category.equals(catName, ignoreCase = true) || it.label.equals(catName, ignoreCase = true) },
            onDismiss = { selectedCategoryForSheet = null },
            onNavigateToTransactions = { onNavigateToTransactions(catName) },
            onAddTransactionForCategory = { },
            onTransactionClick = onTransactionClick
        )
    }
}
