package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.dialogs.CustomDateRange
import com.example.ui.dialogs.DateRangePickerDialog
import com.example.ui.dialogs.MonthYearPickerDialog
import com.example.ui.dialogs.PdfExportSuccessDialog
import com.example.ui.screens.summary.SummaryKalenderView
import com.example.ui.screens.summary.SummaryLaporanView
import com.example.ui.screens.summary.SummaryStatistikView
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueBright
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.getTopHeaderGradient
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CategorySlice
import com.example.util.ChartDataHelper
import com.example.util.ChartPeriod
import com.example.util.CurrencyHelper
import com.example.util.PdfExportResult
import com.example.util.PdfReportHelper
import com.example.util.TransactionMathHelper

/**
 * Menu Ringkasan Keuangan (Financial Summary Dashboard):
 * 1. Header & Navigasi Atas:
 *    - Pemilih Periode Cepat (Chip horizontal bulan: Agu 2026, Sep 2026, Okt 2026, Nov 2026 aktif + tombol kalender)
 *    - Tab Tampilan (Toggle View): [ Statistik ], [ Laporan ], [ Kalender ]
 *    - Mode Sekilas (At-a-glance): Pilihan rentang waktu (Bulanan, Tahunan, Kustom) dengan tombol filter kalender dan ekspor PDF
 * 2. Tab "Statistik" (Tampilan Utama): 11 komponen analisis mendalam, arus kas, aktivitas harian, Sankey, tren, dan kesehatan keuangan
 * 3. Tab "Laporan": Ringkasan tahunan, rincian bulanan, donut chart kategori, pengeluaran per label, periode ini vs lalu
 * 4. Tab "Kalender": Kalender bulanan interaktif penuh dengan badge nominal pengeluaran/pemasukan di setiap tanggal
 */
@Composable
fun SummaryScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTransactions: (category: String?) -> Unit = {},
    onNavigateToAccount: (walletName: String) -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {},
    onNavigateToPlanner: () -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onAddTransaction: (category: String?) -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonthIndex.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val monthOptions by viewModel.monthOptions.collectAsState()
    val selectedWallet by viewModel.selectedWalletFilter.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val summaryTab by viewModel.summaryTab.collectAsState()
    val summaryPeriod by viewModel.summaryPeriod.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    val assets by viewModel.assets.collectAsState()

    var showWalletDropdown by remember { mutableStateOf(false) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var customRange by remember { mutableStateOf<CustomDateRange?>(CustomDateRange(1, selectedMonth, selectedYear, 28, selectedMonth, selectedYear)) }

    // Consistent wallet-filtered transaction stream
    val walletFilteredTransactions = remember(allTransactions, selectedWallet) {
        if (selectedWallet == "Semua") allTransactions
        else allTransactions.filter { it.accountName.equals(selectedWallet, ignoreCase = true) }
    }

    // Filter transactions based on selected period (Bulanan, Tahunan, Kustom) and wallet
    val periodTransactions = remember(walletFilteredTransactions, selectedMonth, selectedYear, summaryPeriod, customRange) {
        when (summaryPeriod) {
            0 -> walletFilteredTransactions.filter { it.monthIndex == selectedMonth && it.year == selectedYear }
            1 -> walletFilteredTransactions.filter { it.year == selectedYear }
            2 -> {
                if (customRange != null) {
                    val r = customRange!!
                    walletFilteredTransactions.filter { tx ->
                        val isAfterStart = tx.year > r.startYear || (tx.year == r.startYear && (tx.monthIndex > r.startMonth || (tx.monthIndex == r.startMonth && tx.dayOfMonth >= r.startDay)))
                        val isBeforeEnd = tx.year < r.endYear || (tx.year == r.endYear && (tx.monthIndex < r.endMonth || (tx.monthIndex == r.endMonth && tx.dayOfMonth <= r.endDay)))
                        isAfterStart && isBeforeEnd
                    }
                } else {
                    walletFilteredTransactions.filter { it.monthIndex == selectedMonth && it.year == selectedYear }
                }
            }
            else -> walletFilteredTransactions.filter { it.monthIndex == selectedMonth && it.year == selectedYear }
        }
    }

    val (computedIncomeTotal, incomeBreakdown) = remember(periodTransactions) {
        TransactionMathHelper.buildCategoryBreakdown(periodTransactions, TransactionType.INCOME)
    }

    val (computedExpenseTotal, expenseBreakdown) = remember(periodTransactions) {
        TransactionMathHelper.buildCategoryBreakdown(periodTransactions, TransactionType.EXPENSE)
    }

    val currentIncome = computedIncomeTotal
    val currentExpense = computedExpenseTotal
    val netCashFlow = currentIncome - currentExpense

    val chartData = remember(periodTransactions, selectedMonth, selectedYear) {
        ChartDataHelper.getExpenseTrend(periodTransactions, ChartPeriod.ONE_MONTH, selectedMonth, selectedYear)
    }

    val cumulativeData = remember(allTransactions, selectedMonth, selectedYear, selectedWallet) {
        ChartDataHelper.getCumulativeComparison(allTransactions, selectedMonth, selectedYear, selectedWallet)
    }

    val selectedMonthLabel = "${monthOptions.find { it.first == selectedMonth }?.second ?: "Nov"} $selectedYear"
    val savingsRate = if (currentIncome > 0) (((currentIncome - currentExpense) / currentIncome) * 100).toInt() else 0
    var pdfExportResult by remember { mutableStateOf<PdfExportResult?>(null) }

    val handleExportPdf: () -> Unit = {
        val result = PdfReportHelper.generateAndSavePdf(
            context = context,
            periodLabel = if (summaryPeriod == 2 && customRange != null) customRange!!.label else if (summaryPeriod == 1) "$selectedYear" else selectedMonthLabel,
            walletName = selectedWallet,
            incomeTotal = currentIncome,
            expenseTotal = currentExpense,
            netCashFlow = netCashFlow,
            savingsRate = savingsRate,
            expenseBreakdown = expenseBreakdown,
            incomeBreakdown = incomeBreakdown,
            transactions = periodTransactions
        )
        if (result != null) {
            pdfExportResult = result
            Toast.makeText(context, "Laporan PDF berhasil disimpan!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Gagal membuat laporan PDF", Toast.LENGTH_SHORT).show()
        }
    }

    var csvExportResult by remember { mutableStateOf<com.example.util.CsvExportResult?>(null) }

    val handleExportCsv: () -> Unit = {
        val activePeriodLabel = if (summaryPeriod == 2 && customRange != null) customRange!!.label else if (summaryPeriod == 1) "Tahun $selectedYear" else selectedMonthLabel
        val result = com.example.util.CsvExportHelper.generateAndSaveCsv(
            context = context,
            periodLabel = activePeriodLabel,
            transactions = periodTransactions
        )
        if (result != null) {
            csvExportResult = result
            Toast.makeText(context, "Data Excel/CSV berhasil diekspor!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Gagal membuat file CSV", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(getTopHeaderGradient(AccentBlue))
    ) {
        // ==================== 1. HEADER & NAVIGASI ATAS ====================
        // Top Bar: Ringkasan Title, Quick PDF & Excel Buttons, Wallet Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ringkasan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // PDF Export Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clickable { handleExportPdf() }
                        .padding(end = 6.dp)
                        .testTag("btn_export_pdf_header")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Simpan Laporan PDF",
                            tint = AccentBlueBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlueBright
                        )
                    }
                }

                // Excel/CSV Export Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clickable { handleExportCsv() }
                        .padding(end = 8.dp)
                        .testTag("btn_export_csv_header")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Ekspor Spreadsheet Excel/CSV",
                            tint = IncomeGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Excel",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }

                // Wallet Filter Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.clickable { showWalletDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedWallet,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showWalletDropdown,
                        onDismissRequest = { showWalletDropdown = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua", color = TextWhite) },
                            onClick = {
                                viewModel.selectWalletFilter("Semua")
                                showWalletDropdown = false
                            }
                        )
                        wallets.forEach { wallet ->
                            DropdownMenuItem(
                                text = { Text(wallet.name, color = TextWhite) },
                                onClick = {
                                    viewModel.selectWalletFilter(wallet.name)
                                    showWalletDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Pemilih Periode Cepat: Tombol Kalender Fixed + Horizontal Chip Row (Agu, Sep, Okt, Nov aktif)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Kalender Fixed
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .clickable { showMonthYearPicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pilih Bulan dan Tahun",
                        tint = AccentBlueBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Scrollable Month Chips
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                monthOptions.forEach { (index, label) ->
                    val isSelected = selectedMonth == index
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) AccentBlue else DarkSurfaceElevated,
                        modifier = Modifier.clickable { viewModel.selectMonth(index) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Tab Tampilan (Toggle View): [ Statistik ], [ Laporan ], dan [ Kalender ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Triple(0, "Statistik", Icons.Default.BarChart),
                Triple(1, "Laporan", Icons.Default.Description),
                Triple(2, "Kalender", Icons.Default.CalendarMonth)
            )

            tabs.forEach { (index, title, icon) ->
                val isSelected = summaryTab == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentBlue else DarkSurfaceElevated,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setSummaryTab(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        // Mode Sekilas (At-a-glance): Bulanan, Tahunan, Kustom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sekilas",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )

            Row(
                modifier = Modifier
                    .background(DarkSurfaceElevated, RoundedCornerShape(14.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val periodOptions = listOf(
                    Triple(0, "Bulanan", Icons.Default.CalendarMonth),
                    Triple(1, "Tahunan", Icons.Default.BarChart),
                    Triple(2, "Kustom", Icons.Default.FilterList)
                )

                periodOptions.forEach { (index, title, icon) ->
                    val isSelected = summaryPeriod == index
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AccentBlue else Color.Transparent,
                        modifier = Modifier.clickable {
                            viewModel.setSummaryPeriod(index)
                            if (index == 2) {
                                showCustomDateDialog = true
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Custom Date Range Active Chip (e.g. 2 Nov - 5 Nov 2026)
        if (summaryPeriod == 2 && customRange != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showCustomDateDialog = true }
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = customRange!!.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setSummaryPeriod(0) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Reset", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ==================== TAB CONTENT ====================
        when (summaryTab) {
            0 -> {
                // TAB STATISTIK (Tampilan Utama: 11 Komponen Lengkap)
                SummaryStatistikView(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    monthName = monthOptions.find { it.first == selectedMonth }?.second ?: "Nov",
                    allTransactions = allTransactions,
                    periodTransactions = periodTransactions,
                    recurringTransactions = recurringTransactions,
                    wallets = wallets,
                    currentIncome = currentIncome,
                    currentExpense = currentExpense,
                    chartData = chartData,
                    cumulativeData = cumulativeData,
                    expenseSlices = expenseBreakdown,
                    incomeSlices = incomeBreakdown,
                    selectedWallet = selectedWallet,
                    onExportPdf = handleExportPdf,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToAccount = onNavigateToAccount,
                    onNavigateToAssets = onNavigateToAssets,
                    onNavigateToDebts = onNavigateToDebts,
                    onNavigateToPlanner = onNavigateToPlanner,
                    onNavigateToBudgets = onNavigateToBudgets,
                    onAddTransaction = onAddTransaction,
                    onTransactionClick = onTransactionClick
                )
            }
            1 -> {
                // TAB LAPORAN (Ringkasan Tahunan, Rincian Bulanan Tabel, Donut Kategori & Label)
                SummaryLaporanView(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    monthOptions = monthOptions,
                    allTransactions = allTransactions,
                    periodTransactions = periodTransactions,
                    wallets = wallets,
                    assets = assets,
                    currentIncome = currentIncome,
                    currentExpense = currentExpense,
                    expenseSlices = expenseBreakdown,
                    incomeSlices = incomeBreakdown,
                    selectedWallet = selectedWallet,
                    onExportPdf = handleExportPdf,
                    onNavigateToTransactions = onNavigateToTransactions,
                    onNavigateToAccount = onNavigateToAccount,
                    onNavigateToAssets = onNavigateToAssets,
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onSelectYear = { viewModel.selectYear(it) },
                    onTransactionClick = onTransactionClick
                )
            }
            else -> {
                // TAB KALENDER (Kalender Bulanan Interaktif S S R K J S M dengan Badges Nominal)
                SummaryKalenderView(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    monthName = monthOptions.find { it.first == selectedMonth }?.second ?: "Nov",
                    transactions = periodTransactions,
                    onTransactionClick = onTransactionClick,
                    onAddTransactionForDate = { onAddTransaction(null) },
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onSelectMonthYear = { m, y -> viewModel.selectMonthAndYear(m, y) },
                    onNavigateToTransactions = { onNavigateToTransactions(null) }
                )
            }
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showMonthYearPicker = false },
            onSelectMonthYear = { m, y ->
                viewModel.selectMonthAndYear(m, y)
            }
        )
    }

    if (showCustomDateDialog) {
        DateRangePickerDialog(
            initialRange = customRange,
            onDismiss = { showCustomDateDialog = false },
            onConfirm = { range ->
                customRange = range
                viewModel.setSummaryPeriod(2)
                showCustomDateDialog = false
            }
        )
    }

    val currentActivePeriodLabel = if (summaryPeriod == 2 && customRange != null) customRange!!.label else if (summaryPeriod == 1) "Tahun $selectedYear" else selectedMonthLabel
    pdfExportResult?.let { result ->
        PdfExportSuccessDialog(
            exportResult = result,
            periodLabel = currentActivePeriodLabel,
            onDismiss = { pdfExportResult = null }
        )
    }

    csvExportResult?.let { result ->
        com.example.ui.dialogs.CsvExportSuccessDialog(
            exportResult = result,
            periodLabel = currentActivePeriodLabel,
            onDismiss = { csvExportResult = null }
        )
    }
}
