package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import com.example.ui.components.FixedYellow
import com.example.ui.components.FixedYellowDark
import com.example.ui.components.FixedYellowLight
import com.example.ui.components.PengaturanTwoToneNavIcon
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import com.example.data.model.TransactionType
import com.example.util.CsvExportHelper
import com.example.util.PdfReportHelper
import com.example.util.TransactionMathHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.ui.dialogs.ManageCategoriesDialog
import com.example.ui.dialogs.ManageLabelsDialog
import com.example.ui.dialogs.RecurringTransactionDialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AvailableThemePalettes
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.getTopHeaderGradient
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onBackupRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    var sliderPosition by remember(settings.textSizePercent) { mutableFloatStateOf(settings.textSizePercent.toFloat()) }

    // Dialog state handlers
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeColorDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showExchangeRateDialog by remember { mutableStateOf(false) }
    var showMonthlyStartDayDialog by remember { mutableStateOf(false) }
    var showSmartNotificationsDialog by remember { mutableStateOf(false) }
    var showManageLabelsDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }
    var showNavigationOrderDialog by remember { mutableStateOf(false) }
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showNumberFormatDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showDailyReminderTimeDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showStartDayOfWeekDialog by remember { mutableStateOf(false) }
    var showBudgetWarningDialog by remember { mutableStateOf(false) }
    var showSavingsTargetDialog by remember { mutableStateOf(false) }

    val allTransactions by viewModel.allTransactions.collectAsState()
    val (computedIncomeTotal, incomeBreakdown) = remember(allTransactions) {
        TransactionMathHelper.buildCategoryBreakdown(allTransactions, TransactionType.INCOME)
    }
    val (computedExpenseTotal, expenseBreakdown) = remember(allTransactions) {
        TransactionMathHelper.buildCategoryBreakdown(allTransactions, TransactionType.EXPENSE)
    }

    val handleExportPdf: () -> Unit = {
        val result = PdfReportHelper.generateAndSavePdf(
            context = context,
            periodLabel = "Rekap Money Tracker Lengkap",
            walletName = "Semua Akun",
            incomeTotal = computedIncomeTotal,
            expenseTotal = computedExpenseTotal,
            netCashFlow = computedIncomeTotal - computedExpenseTotal,
            savingsRate = if (computedIncomeTotal > 0) (((computedIncomeTotal - computedExpenseTotal) / computedIncomeTotal) * 100).toInt() else 0,
            expenseBreakdown = expenseBreakdown,
            incomeBreakdown = incomeBreakdown,
            transactions = allTransactions
        )
        if (result != null) {
            Toast.makeText(context, "Laporan PDF berhasil disimpan!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Gagal membuat laporan PDF", Toast.LENGTH_SHORT).show()
        }
    }

    val handleExportCsv: () -> Unit = {
        val result = CsvExportHelper.generateAndSaveCsv(
            context = context,
            periodLabel = "Semua Data Money Tracker",
            transactions = allTransactions
        )
        if (result != null) {
            Toast.makeText(context, "Data Excel/CSV berhasil disimpan!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Gagal mengekspor file CSV", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(getTopHeaderGradient(AccentBlue))
    ) {
        // Header Toolbar with status bar padding for edge-to-edge
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurfaceElevated,
            border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PengaturanTwoToneNavIcon(
                    themeColor = AccentBlue,
                    isSelected = true,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Pengaturan",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Tampilan & Antarmuka
            item {
                Text(
                    text = "Tampilan & Antarmuka",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemClickable(
                            icon = Icons.Default.Language,
                            title = "Bahasa",
                            subtitle = "Ubah bahasa aplikasi",
                            badgeValue = settings.language,
                            onClick = { showLanguageDialog = true }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.DarkMode,
                            title = "Mode Gelap",
                            subtitle = "Beralih antara tema terang dan gelap",
                            checked = settings.isDarkMode,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(isDarkMode = it)) }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Palette,
                            title = "Warna Tema",
                            subtitle = "Sesuaikan warna aksen utama aplikasi",
                            badgeValue = settings.themeColorName,
                            onClick = { showThemeColorDialog = true }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.Vibration,
                            title = "Getaran & Haptic Feedback",
                            subtitle = "Sensasi getar responsif saat menekan tombol & keypad",
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedbackEnabled = it)) }
                        )

                        // Slider Ukuran Teks
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SettingsTwoToneIconBadge(
                                        icon = Icons.Default.FormatSize,
                                        tint = AccentBlue
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Ukuran Teks", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                        Text("Perbesar atau perkecil teks di seluruh aplikasi", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Text("${sliderPosition.toInt()}%", fontSize = 12.sp, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = sliderPosition,
                                onValueChange = {
                                    sliderPosition = it
                                    viewModel.updateSettings(settings.copy(textSizePercent = it.toInt()))
                                },
                                onValueChangeFinished = {
                                    viewModel.updateSettings(settings.copy(textSizePercent = sliderPosition.toInt()))
                                    Toast.makeText(context, "Ukuran teks diatur ke ${sliderPosition.toInt()}%", Toast.LENGTH_SHORT).show()
                                },
                                valueRange = 80f..140f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentBlue,
                                    activeTrackColor = AccentBlue,
                                    inactiveTrackColor = DarkSurfaceElevated
                                )
                            )
                        }
                    }
                }
            }

            // Section 2: Preferensi Finansial
            item {
                Text(
                    text = "Preferensi Finansial",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemClickable(
                            icon = Icons.Default.CurrencyExchange,
                            title = "Mata Uang Utama",
                            subtitle = "Tetapkan mata uang saldo dan transaksi",
                            badgeValue = settings.primaryCurrency,
                            onClick = { showCurrencyDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Repeat,
                            title = "Kurs Mata Uang",
                            subtitle = "Tetapkan kurs konversi untuk pasangan mata uang",
                            onClick = { showExchangeRateDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Pin,
                            title = "Format Angka",
                            subtitle = "Pemisah ribuan dan tampilan nominal",
                            badgeValue = settings.numberFormat,
                            onClick = { showNumberFormatDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.DateRange,
                            title = "Format Tanggal",
                            subtitle = "Tampilan tanggal pada transaksi dan laporan",
                            badgeValue = settings.dateFormat,
                            onClick = { showDateFormatDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Event,
                            title = "Hari Pertama Mingguan",
                            subtitle = "Awal perhitungan grafik dan tren mingguan",
                            badgeValue = settings.startDayOfWeek,
                            onClick = { showStartDayOfWeekDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Event,
                            title = "Tanggal Mulai Bulanan / Gajian",
                            subtitle = "Sesuaikan siklus buku bulanan dengan tanggal gajian",
                            badgeValue = "Hari ${settings.monthlyStartDay}",
                            onClick = { showMonthlyStartDayDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Savings,
                            title = "Target Rasio Menabung",
                            subtitle = "Target persentase tabungan dari pemasukan bulanan",
                            badgeValue = "${settings.targetSavingsPercent}%",
                            onClick = { showSavingsTargetDialog = true }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Akun Tunai Saja",
                            subtitle = "Kecualikan kartu kredit dari laporan arus kas",
                            checked = settings.cashAccountsOnly,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(cashAccountsOnly = it)) }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.ShowChart,
                            title = "Tampilkan Uang Terencana di Saldo",
                            subtitle = "Menambahkan proyeksi 30 hari ke depan pada nilai bersih",
                            checked = settings.showPlannedInBalance,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showPlannedInBalance = it)) }
                        )
                    }
                }
            }

            // Section 3: Notifikasi & Peringatan
            item {
                Text(
                    text = "Notifikasi & Peringatan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemSwitch(
                            icon = Icons.Default.Alarm,
                            title = "Pengingat Catat Harian",
                            subtitle = if (settings.dailyReminderEnabled) "Aktif setiap pukul ${settings.dailyReminderTime}" else "Nonaktif",
                            checked = settings.dailyReminderEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(dailyReminderEnabled = it)) }
                        )

                        if (settings.dailyReminderEnabled) {
                            SettingsItemClickable(
                                icon = Icons.Default.Schedule,
                                title = "Waktu Pengingat Harian",
                                subtitle = "Jam evaluasi dan pencatatan transaksi setiap hari",
                                badgeValue = settings.dailyReminderTime,
                                onClick = { showDailyReminderTimeDialog = true }
                            )
                        }

                        SettingsItemSwitch(
                            icon = Icons.Default.Warning,
                            title = "Peringatan Anggaran",
                            subtitle = if (settings.budgetAlert) "Pemberitahuan saat anggaran mencapai ${settings.budgetWarningPercent}%" else "Nonaktif",
                            checked = settings.budgetAlert,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(budgetAlert = it)) }
                        )

                        if (settings.budgetAlert) {
                            SettingsItemClickable(
                                icon = Icons.Default.Warning,
                                title = "Ambang Batas Peringatan",
                                subtitle = "Persentase kuota anggaran untuk memicu peringatan",
                                badgeValue = "${settings.budgetWarningPercent}%",
                                onClick = { showBudgetWarningDialog = true }
                            )
                        }

                        SettingsItemSwitch(
                            icon = Icons.Default.Notifications,
                            title = "Pengingat Tagihan & Tempo",
                            subtitle = if (settings.billReminder) "Aktif" else "Nonaktif",
                            checked = settings.billReminder,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(billReminder = it)) }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.Savings,
                            title = "Pengingat Menabung Rutin",
                            subtitle = if (settings.saveReminder) "Aktif" else "Nonaktif",
                            checked = settings.saveReminder,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(saveReminder = it)) }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.AutoAwesome,
                            title = "Smart Financial Insights",
                            subtitle = "Wawasan pengeluaran pintar & anomali berkala",
                            badgeValue = "Aktif",
                            onClick = { showSmartNotificationsDialog = true }
                        )
                    }
                }
            }

            // Section 4: Data & Kustomisasi
            item {
                Text(
                    text = "Data & Kustomisasi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemClickable(
                            icon = Icons.Default.Category,
                            title = "Kategori Transaksi",
                            subtitle = "Kelola kategori pengeluaran dan pemasukan",
                            onClick = { showManageCategoriesDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Label,
                            title = "Label Transaksi",
                            subtitle = "Atur tag/label transaksi khusus",
                            onClick = { showManageLabelsDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Repeat,
                            title = "Transaksi Berulang",
                            subtitle = "Atur dan kelola pengeluaran/pemasukan rutin",
                            onClick = { showRecurringDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.ViewAgenda,
                            title = "Urutan Menu Utama",
                            subtitle = "Kustomisasi urutan tab navigasi bawah",
                            onClick = { showNavigationOrderDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.PictureAsPdf,
                            title = "Ekspor Laporan PDF",
                            subtitle = "Simpan ringkasan keuangan lengkap ke dokumen PDF",
                            onClick = handleExportPdf
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Description,
                            title = "Ekspor Data Excel / CSV",
                            subtitle = "Unduh seluruh riwayat transaksi ke format CSV",
                            onClick = handleExportCsv
                        )
                    }
                }
            }

            // Section 5: Keamanan & Privasi
            item {
                Text(
                    text = "Keamanan & Privasi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemSwitch(
                            icon = Icons.Default.Lock,
                            title = "Kode Sandi / PIN Aplikasi",
                            subtitle = if (settings.passcodeEnabled) "PIN aktif (${settings.appPasscode.ifEmpty { "1234" }}) · Mengunci saat dibuka" else "Amankan aplikasi dengan 4 digit PIN",
                            checked = settings.passcodeEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPasscodeDialog = true
                                } else {
                                    viewModel.updateSettings(settings.copy(passcodeEnabled = false))
                                    Toast.makeText(context, "Kode sandi dinonaktifkan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.VisibilityOff,
                            title = "Mode Privasi (Sembunyikan Saldo)",
                            subtitle = "Sensor angka nominal saldo dengan '••••••' secara default",
                            checked = settings.defaultHideBalance,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(defaultHideBalance = it)) }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.DeleteForever,
                            title = "Konfirmasi Hapus Transaksi",
                            subtitle = "Tampilkan dialog konfirmasi saat menghapus entri keuangan",
                            checked = settings.confirmDeleteTransaction,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDeleteTransaction = it)) }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.CloudUpload,
                            title = "Cadangkan & Pulihkan",
                            subtitle = "Ekspor atau impor basis data lokal aplikasi (JSON)",
                            onClick = onBackupRestoreClick
                        )
                    }
                }
            }

            // Section 6: Informasi & Dukungan
            item {
                Text(
                    text = "Informasi & Dukungan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAboutAppDialog = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_logo),
                                        contentDescription = "Logo Money Tracker",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Tentang Money Tracker", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                    Text("Personal Finance Manager v2.33.0", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSurfaceElevated
                                ) {
                                    Text(
                                        text = "v2.33.0",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AccentBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = TextSubtle,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        SettingsItemClickable(
                            icon = Icons.Default.Policy,
                            title = "Kebijakan Privasi",
                            subtitle = "100% Offline-First, data aman di perangkat Anda",
                            onClick = { showPrivacyPolicyDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Feedback,
                            title = "Saran & Masukan",
                            subtitle = "Bagikan ide dan masukan untuk fitur berikutnya",
                            onClick = { showFeedbackDialog = true }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Share,
                            title = "Bagikan ke Teman",
                            subtitle = "Bagikan aplikasi Money Tracker ke kerabat Anda",
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Money Tracker - Personal Finance Manager")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Yuk catat dan kelola keuangan pribadimu dengan mudah menggunakan Money Tracker! Praktis, modern, dan tanpa ribet."
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Money Tracker"))
                            }
                        )
                    }
                }
            }

            // Section 7: Zona Bahaya / Reset Data
            item {
                Text(
                    text = "Zona Bahaya",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ExpenseRed,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    SettingsItemClickable(
                        icon = Icons.Default.RestartAlt,
                        title = "Reset Data Aplikasi",
                        subtitle = "Hapus semua riwayat transaksi, akun, aset, dan utang",
                        iconTint = ExpenseRed,
                        titleColor = ExpenseRed,
                        onClick = { showResetConfirmDialog = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Dialogs
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = settings.language,
                onDismiss = { showLanguageDialog = false },
                onSelectLanguage = { lang ->
                    viewModel.updateSettings(settings.copy(language = lang))
                    showLanguageDialog = false
                    Toast.makeText(context, "Bahasa diubah ke $lang", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showThemeColorDialog) {
            ThemeColorSelectionDialog(
                currentThemeName = settings.themeColorName,
                onDismiss = { showThemeColorDialog = false },
                onSelectColor = { name ->
                    viewModel.updateSettings(settings.copy(themeColorName = name))
                    com.example.ui.theme.AppThemeState.currentAccentColor = com.example.ui.theme.getThemeColorByName(name)
                    com.example.ui.theme.AppThemeState.currentThemeName = name
                    showThemeColorDialog = false
                    Toast.makeText(context, "Warna tema '$name' diterapkan di seluruh aplikasi!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showCurrencyDialog) {
            CurrencySelectionDialog(
                currentCurrency = settings.primaryCurrency,
                onDismiss = { showCurrencyDialog = false },
                onSelectCurrency = { curr ->
                    viewModel.updateSettings(settings.copy(primaryCurrency = curr))
                    showCurrencyDialog = false
                    Toast.makeText(context, "Mata uang utama diatur ke $curr", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showExchangeRateDialog) {
            ExchangeRateDialog(
                primaryCurrency = settings.primaryCurrency,
                onDismiss = { showExchangeRateDialog = false }
            )
        }

        if (showMonthlyStartDayDialog) {
            MonthlyStartDayDialog(
                currentDay = settings.monthlyStartDay,
                onDismiss = { showMonthlyStartDayDialog = false },
                onSelectDay = { day ->
                    viewModel.updateSettings(settings.copy(monthlyStartDay = day))
                    showMonthlyStartDayDialog = false
                    Toast.makeText(context, "Siklus bulanan dimulai tanggal $day", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showSmartNotificationsDialog) {
            SmartNotificationsDialog(
                onDismiss = { showSmartNotificationsDialog = false }
            )
        }

        if (showNavigationOrderDialog) {
            MainMenuOrderDialog(
                onDismiss = { showNavigationOrderDialog = false }
            )
        }

        if (showPasscodeDialog) {
            PasscodeSetupDialog(
                currentPasscode = settings.appPasscode,
                onDismiss = { showPasscodeDialog = false },
                onPasscodeSet = { newPin ->
                    viewModel.updateSettings(settings.copy(passcodeEnabled = true, appPasscode = newPin))
                    showPasscodeDialog = false
                    Toast.makeText(context, "Kode sandi berhasil diatur ke '$newPin'!", Toast.LENGTH_LONG).show()
                }
            )
        }

        if (showFeedbackDialog) {
            FeedbackDialog(
                onDismiss = { showFeedbackDialog = false },
                onSubmit = {
                    showFeedbackDialog = false
                    Toast.makeText(context, "Terima kasih atas saran dan masukan Anda!", Toast.LENGTH_LONG).show()
                }
            )
        }

        if (showAboutAppDialog) {
            AboutAppDialog(
                onDismiss = { showAboutAppDialog = false }
            )
        }

        if (showResetConfirmDialog) {
            ResetDataConfirmDialog(
                onDismiss = { showResetConfirmDialog = false },
                onConfirmReset = { keepDefaultWallet ->
                    viewModel.resetAllData(createDefaultWallet = keepDefaultWallet)
                    showResetConfirmDialog = false
                    Toast.makeText(context, "Semua data transaksi dan inputan berhasil direset.", Toast.LENGTH_LONG).show()
                }
            )
        }

        if (showManageLabelsDialog) {
            ManageLabelsDialog(
                viewModel = viewModel,
                onDismiss = { showManageLabelsDialog = false }
            )
        }

        if (showManageCategoriesDialog) {
            ManageCategoriesDialog(
                viewModel = viewModel,
                onDismiss = { showManageCategoriesDialog = false }
            )
        }

        if (showRecurringDialog) {
            RecurringTransactionDialog(
                viewModel = viewModel,
                onDismiss = { showRecurringDialog = false }
            )
        }

        if (showNumberFormatDialog) {
            NumberFormatDialog(
                currentFormat = settings.numberFormat,
                onDismiss = { showNumberFormatDialog = false },
                onSelectFormat = { fmt ->
                    viewModel.updateSettings(settings.copy(numberFormat = fmt))
                    showNumberFormatDialog = false
                    Toast.makeText(context, "Format angka diubah ke $fmt", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showDateFormatDialog) {
            DateFormatDialog(
                currentFormat = settings.dateFormat,
                onDismiss = { showDateFormatDialog = false },
                onSelectFormat = { fmt ->
                    viewModel.updateSettings(settings.copy(dateFormat = fmt))
                    showDateFormatDialog = false
                    Toast.makeText(context, "Format tanggal diubah ke $fmt", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showDailyReminderTimeDialog) {
            DailyReminderTimeDialog(
                currentTime = settings.dailyReminderTime,
                onDismiss = { showDailyReminderTimeDialog = false },
                onSelectTime = { time ->
                    viewModel.updateSettings(settings.copy(dailyReminderTime = time))
                    showDailyReminderTimeDialog = false
                    Toast.makeText(context, "Waktu pengingat disimpan: $time", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showStartDayOfWeekDialog) {
            StartDayOfWeekDialog(
                currentDay = settings.startDayOfWeek,
                onDismiss = { showStartDayOfWeekDialog = false },
                onSelectDay = { day ->
                    viewModel.updateSettings(settings.copy(startDayOfWeek = day))
                    showStartDayOfWeekDialog = false
                    Toast.makeText(context, "Hari pertama dalam seminggu diatur ke $day", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showBudgetWarningDialog) {
            BudgetWarningThresholdDialog(
                currentThreshold = settings.budgetWarningPercent,
                onDismiss = { showBudgetWarningDialog = false },
                onSelectThreshold = { pct ->
                    viewModel.updateSettings(settings.copy(budgetWarningPercent = pct))
                    showBudgetWarningDialog = false
                    Toast.makeText(context, "Ambang batas peringatan anggaran diatur ke $pct%", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showSavingsTargetDialog) {
            SavingsTargetDialog(
                currentTarget = settings.targetSavingsPercent,
                onDismiss = { showSavingsTargetDialog = false },
                onSelectTarget = { pct ->
                    viewModel.updateSettings(settings.copy(targetSavingsPercent = pct))
                    showSavingsTargetDialog = false
                    Toast.makeText(context, "Target rasio menabung diatur ke $pct%", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showPrivacyPolicyDialog) {
            PrivacyPolicyDialog(
                onDismiss = { showPrivacyPolicyDialog = false }
            )
        }
    }
}

@Composable
fun SettingsTwoToneIconBadge(
    icon: ImageVector,
    tint: Color = AccentBlue,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(34.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(9.dp),
            color = tint.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Distinct vibrant yellow accent badge in top-right corner matching toolbar two-tone styling!
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(8.dp)
        ) {
            val r = size.minDimension / 2f
            drawCircle(
                color = FixedYellowDark,
                radius = r
            )
            drawCircle(
                color = FixedYellow,
                radius = r * 0.8f
            )
            drawCircle(
                color = FixedYellowLight,
                radius = r * 0.35f,
                center = Offset(size.width * 0.35f, size.height * 0.35f)
            )
        }
    }
}

@Composable
fun SettingsItemClickable(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeValue: String? = null,
    iconTint: Color = AccentBlue,
    titleColor: Color = TextWhite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            SettingsTwoToneIconBadge(
                icon = icon,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                Text(subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badgeValue != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceElevated
                ) {
                    Text(
                        text = badgeValue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSubtle,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    iconTint: Color = AccentBlue,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            SettingsTwoToneIconBadge(
                icon = icon,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                Text(subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentBlue,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}

// -------------------------------------------------------------
// Dialogs for Settings Features
// -------------------------------------------------------------

@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onDismiss: () -> Unit,
    onSelectLanguage: (String) -> Unit
) {
    val languages = listOf("Indonesia", "English (US)", "Bahasa Jawa", "Bahasa Sunda")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Bahasa", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                languages.forEach { lang ->
                    val isSelected = currentLanguage.equals(lang, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLanguage(lang) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectLanguage(lang) },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(lang, color = if (isSelected) AccentBlue else TextWhite, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun ThemeColorSelectionDialog(
    currentThemeName: String,
    onDismiss: () -> Unit,
    onSelectColor: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Warna Tema", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Pilih palet warna aksen aplikasi. Perubahan akan langsung terlihat di seluruh layar dan kartu total saldo:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                AvailableThemePalettes.forEach { (name, color) ->
                    val isSelected = currentThemeName.equals(name, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) color.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) color else DarkCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectColor(name) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    name,
                                    color = if (isSelected) color else TextWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terpilih",
                                    tint = color,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelectCurrency: (String) -> Unit
) {
    val currencies = listOf(
        Pair("IDR", "Rupiah Indonesia (Rp)"),
        Pair("USD", "US Dollar ($)"),
        Pair("EUR", "Euro (€)"),
        Pair("SGD", "Singapore Dollar (S$)"),
        Pair("MYR", "Malaysian Ringgit (RM)"),
        Pair("JPY", "Japanese Yen (¥)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mata Uang Utama", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                currencies.forEach { (code, desc) ->
                    val isSelected = currentCurrency.equals(code, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCurrency(code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectCurrency(code) },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(code, fontWeight = FontWeight.Bold, color = if (isSelected) AccentBlue else TextWhite, fontSize = 14.sp)
                            Text(desc, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun ExchangeRateDialog(
    primaryCurrency: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val rates = remember {
        listOf(
            Triple("USD / IDR", "Rp 15.850", "US Dollar"),
            Triple("SGD / IDR", "Rp 11.950", "Singapore Dollar"),
            Triple("EUR / IDR", "Rp 17.200", "Euro"),
            Triple("MYR / IDR", "Rp 3.580", "Malaysian Ringgit"),
            Triple("JPY / IDR", "Rp 103", "Japanese Yen")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kurs Pasangan Mata Uang", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Kurs referensi acuan untuk konversi nilai akun valuta asing ke $primaryCurrency:",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                rates.forEach { (pair, rate, name) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pair, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                                Text(name, color = TextMuted, fontSize = 11.sp)
                            }
                            Text(rate, fontWeight = FontWeight.SemiBold, color = AccentBlue, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Kurs berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Perbarui Kurs", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun MonthlyStartDayDialog(
    currentDay: Int,
    onDismiss: () -> Unit,
    onSelectDay: (Int) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(currentDay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tanggal Mulai Siklus", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Sesuaikan tanggal awal siklus bulanan Anda (misal tanggal gajian):",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (selectedDay > 1) selectedDay-- }
                    ) {
                        Text("-", fontSize = 24.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Tanggal $selectedDay",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AccentBlue,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    IconButton(
                        onClick = { if (selectedDay < 31) selectedDay++ }
                    ) {
                        Text("+", fontSize = 24.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectDay(selectedDay) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun SmartNotificationsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var weeklySummary by remember { mutableStateOf(true) }
    var spendingAlert by remember { mutableStateOf(true) }
    var monthlyReview by remember { mutableStateOf(true) }
    var savingTips by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Smart Notifications", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Notifikasi cerdas untuk membantu kestabilan finansial:", color = TextMuted, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ringkasan Mingguan", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Kilas pengeluaran setiap Minggu malam", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(checked = weeklySummary, onCheckedChange = { weeklySummary = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Peringatan Lonjakan Boros", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Peringatan jika pengeluaran harian melebihi rata-rata", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(checked = spendingAlert, onCheckedChange = { spendingAlert = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Evaluasi Akhir Bulan", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Analisis sisa anggaran dan capaian tabungan", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(checked = monthlyReview, onCheckedChange = { monthlyReview = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tips & Rekomendasi Finansial", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Inspirasi cerdas pengelolaan uang", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(checked = savingTips, onCheckedChange = { savingTips = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Toast.makeText(context, "Pengaturan notifikasi disimpan", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun MainMenuOrderDialog(onDismiss: () -> Unit) {
    val items = listOf("Dashboard", "Ringkasan", "Transaksi", "Akun", "Perencana")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menu Navigasi Utama", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Semua tab navigasi aktif di bilah navigasi bawah:", color = TextMuted, fontSize = 12.sp)
                items.forEachIndexed { idx, name ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${idx + 1}.", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(name, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Text("Tutup", color = Color.White)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun PasscodeSetupDialog(
    currentPasscode: String = "",
    onDismiss: () -> Unit,
    onPasscodeSet: (String) -> Unit
) {
    var pin by remember { mutableStateOf(currentPasscode) }
    var pinConfirm by remember { mutableStateOf(currentPasscode) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Kode Sandi PIN", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Masukkan 4 digit PIN untuk mengamankan aplikasi Money Tracker:", color = TextMuted, fontSize = 12.sp)

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("PIN (4 Digit)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pinConfirm,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinConfirm = it },
                    label = { Text("Konfirmasi PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = ExpenseRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length < 4) {
                        errorMsg = "PIN harus terdiri dari 4 digit angka"
                    } else if (pin != pinConfirm) {
                        errorMsg = "Konfirmasi PIN tidak cocok"
                    } else {
                        onPasscodeSet(pin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Simpan PIN", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saran & Masukan", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Beri penilaian pengalaman Anda menggunakan Money Tracker:", color = TextMuted, fontSize = 12.sp)

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (star <= rating) Color(0xFFFBBF24) else DarkSurfaceElevated,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Tulis ide atau saran Anda") },
                    placeholder = { Text("Contoh: Tambahkan ekspor laporan PDF...") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Kirim Masukan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.size(80.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Logo Money Tracker",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Money Tracker", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Personal Finance Manager", color = TextMuted, fontSize = 12.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Money Tracker adalah aplikasi pencatatan keuangan pribadi modern untuk memantau transaksi harian, mengelola portofolio aset, anggaran bulanan, serta perencanaan arus kas secara presisi.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, DarkCardBorder.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Versi", color = TextMuted, fontSize = 11.sp)
                            Text("2.33.0 (Build 2026.10)", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Penyimpanan", color = TextMuted, fontSize = 11.sp)
                            Text("Offline-First Storage", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Keamanan", color = TextMuted, fontSize = 11.sp)
                            Text("100% Data Lokal Perangkat", color = IncomeGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tutup", color = Color.White)
            }
        },
        containerColor = DarkSurface
    )
}

// REQUIREMENT 4: RESET DATA CONFIRMATION DIALOG
@Composable
fun ResetDataConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmReset: (keepDefaultWallet: Boolean) -> Unit
) {
    var keepDefaultWallet by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Semua Data?", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Apakah Anda yakin ingin menghapus semua inputan? Semua transaksi, akun dompet, portofolio aset, catatan utang, dan perencanaan keuangan akan dihapus secara permanen.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExpenseRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⚠️ Tindakan ini tidak dapat dibatalkan. Pastikan Anda telah mencadangkan data jika masih dibutuhkan.",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { keepDefaultWallet = !keepDefaultWallet }
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = keepDefaultWallet,
                        onCheckedChange = { keepDefaultWallet = it },
                        colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Siapkan 1 dompet awal ('Dompet Utama' Saldo Rp 0)",
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmReset(keepDefaultWallet) },
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
            ) {
                Text("Hapus Semua Data", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun NumberFormatDialog(
    currentFormat: String,
    onDismiss: () -> Unit,
    onSelectFormat: (String) -> Unit
) {
    val formats = listOf(
        Pair("1.000.000 (Titik)", "Format standar Indonesia (Contoh: Rp 2.500.000)"),
        Pair("1,000,000 (Koma)", "Format internasional (Contoh: Rp 2,500,000)"),
        Pair("1 000 000 (Spasi)", "Format pemisah spasi (Contoh: Rp 2 500 000)"),
        Pair("Ringkas (1jt / 1.5M)", "Tampilan singkatan nominal (Contoh: Rp 2.5jt)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Format Angka", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih gaya pemisah ribuan dan desimal:", color = TextMuted, fontSize = 12.sp)
                formats.forEach { (formatKey, description) ->
                    val isSelected = currentFormat == formatKey
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) AccentBlue else DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFormat(formatKey) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectFormat(formatKey) },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(formatKey, color = if (isSelected) AccentBlue else TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(description, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun DateFormatDialog(
    currentFormat: String,
    onDismiss: () -> Unit,
    onSelectFormat: (String) -> Unit
) {
    val formats = listOf(
        Pair("DD/MM/YYYY", "Contoh: 28/10/2026"),
        Pair("DD MMM YYYY", "Contoh: 28 Okt 2026"),
        Pair("YYYY-MM-DD", "Contoh: 2026-10-28 (ISO)"),
        Pair("D MMMM YYYY", "Contoh: 28 Oktober 2026")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Format Tanggal", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih format tampilan tanggal di daftar transaksi dan laporan:", color = TextMuted, fontSize = 12.sp)
                formats.forEach { (formatKey, example) ->
                    val isSelected = currentFormat.equals(formatKey, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) AccentBlue else DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFormat(formatKey) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectFormat(formatKey) },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(formatKey, color = if (isSelected) AccentBlue else TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(example, color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun DailyReminderTimeDialog(
    currentTime: String,
    onDismiss: () -> Unit,
    onSelectTime: (String) -> Unit
) {
    val presetTimes = listOf(
        Pair("07:00", "Pagi (Memulai hari)"),
        Pair("12:30", "Siang (Setelah makan siang)"),
        Pair("18:30", "Sore (Pulang kerja)"),
        Pair("20:00", "Malam (Rekomendasi evaluasi)"),
        Pair("21:30", "Malam (Sebelum tidur)")
    )
    var customTimeInput by remember { mutableStateOf(currentTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Waktu Pengingat Harian", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih waktu notifikasi harian untuk mencatat pengeluaran Anda:", color = TextMuted, fontSize = 12.sp)

                presetTimes.forEach { (timeVal, desc) ->
                    val isSelected = currentTime == timeVal
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) AccentBlue else DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTime(timeVal) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectTime(timeVal) },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(timeVal, color = if (isSelected) AccentBlue else TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(desc, color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = customTimeInput,
                    onValueChange = { if (it.length <= 5) customTimeInput = it },
                    label = { Text("Atur Jam Manual (HH:mm)") },
                    placeholder = { Text("Contoh: 19:45") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customTimeInput.isNotBlank()) onSelectTime(customTimeInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Terapkan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun StartDayOfWeekDialog(
    currentDay: String,
    onDismiss: () -> Unit,
    onSelectDay: (String) -> Unit
) {
    val options = listOf(
        Pair("Senin", "Standar Indonesia & ISO 8601 (Hari kerja awal)"),
        Pair("Minggu", "Standar Kalender Tradisional / AS")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hari Pertama Mingguan", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih hari awal pada tampilan Kalender dan Ringkasan mingguan:", color = TextMuted, fontSize = 12.sp)
                options.forEach { (day, desc) ->
                    val isSelected = currentDay.equals(day, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) AccentBlue else DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDay(day) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectDay(day) },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(day, color = if (isSelected) AccentBlue else TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(desc, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun BudgetWarningThresholdDialog(
    currentThreshold: Int,
    onDismiss: () -> Unit,
    onSelectThreshold: (Int) -> Unit
) {
    val thresholds = listOf(
        Pair(50, "Peringatan Dini (50% anggaran terpakai)"),
        Pair(70, "Peringatan Waspada (70% anggaran terpakai)"),
        Pair(80, "Standar Money Tracker (80% anggaran terpakai)"),
        Pair(90, "Peringatan Kritis (90% anggaran terpakai)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ambang Peringatan Anggaran", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Aplikasi akan memunculkan tanda waspada saat pengeluaran kategori mencapai:", color = TextMuted, fontSize = 12.sp)
                thresholds.forEach { (pct, desc) ->
                    val isSelected = currentThreshold == pct
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentBlue.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) AccentBlue else DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectThreshold(pct) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectThreshold(pct) },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$pct%", color = if (isSelected) AccentBlue else TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(desc, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun SavingsTargetDialog(
    currentTarget: Int,
    onDismiss: () -> Unit,
    onSelectTarget: (Int) -> Unit
) {
    val targets = listOf(
        Pair(10, "10% dari penghasilan (Permulaan yang baik)"),
        Pair(20, "20% dari penghasilan (Standar 50/30/20 ideal)"),
        Pair(30, "30% dari penghasilan (Penghemat agresif)"),
        Pair(50, "50% dari penghasilan (Pecinta gaya hidup FIRE)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Target Rasio Menabung", color = TextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Target rasio menabung bulanan yang dipantau pada kartu ringkasan:", color = TextMuted, fontSize = 12.sp)
                targets.forEach { (pct, desc) ->
                    val isSelected = currentTarget == pct
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) IncomeGreen.copy(alpha = 0.15f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) IncomeGreen else DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTarget(pct) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectTarget(pct) },
                                colors = RadioButtonDefaults.colors(selectedColor = IncomeGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$pct%", color = if (isSelected) IncomeGreen else TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(desc, color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = TextMuted) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Policy, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kebijakan Privasi", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IncomeGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "🔒 100% Offline-First: Seluruh data keuangan Anda disimpan secara lokal di memori internal perangkat ini.",
                        color = IncomeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text("1. Kepemilikan Data Penuh", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Money Tracker tidak memiliki server pihak ketiga yang mengumpulkan, membaca, atau menjual data transaksi, nominal saldo, riwayat akun dompet, maupun portofolio aset Anda.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Text("2. Pencadangan Mandiri", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Anda dapat mengekspor seluruh basis data ke dalam file JSON terenkripsi atau spreadsheet CSV/Excel kapan saja melalui menu Cadangkan & Pulihkan.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Text("3. Keamanan Tambahan", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    "Untuk melindungi privasi layar dari orang lain di sekitar Anda, aktifkan PIN Kode Sandi Aplikasi dan Mode Privasi Sembunyikan Saldo.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)) {
                Text("Saya Mengerti", color = Color.White)
            }
        },
        containerColor = DarkSurface
    )
}
