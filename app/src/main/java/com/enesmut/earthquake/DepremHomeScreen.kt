package com.enesmut.earthquake

import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enesmut.earthquake.domain.Earthquake
import com.enesmut.earthquake.ui.theme.SettingsSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---- Renkler (görsele yakın tonlar)
private val BlueSelected = Color(0xFF548FF1)    // zaman filtresi seçili
private val ChipBg = Color(0xFFF5F0F0)          // zaman filtresi seçili değil
private val DividerGray = Color(0x33212121)

private val MagGreen = Color(0xFFA7E6B5)
private val MagYellow = Color(0xFFF5E28A)
private val MagOrange = Color(0xFFF7B24A)
private val MagRed = Color(0xFFEF5858)
private val ddd = Color(0xFF3D3B3B)


private val DangerContainer = Color(0xFF5A1212) // koyu zemin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepremHomeScreen(
    vm: HomeViewModel = viewModel()
) {
    // İlk açılışta ve filtre değiştikçe veri çek
    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(vm.timeIndex, vm.magSelection) { vm.load() }

    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Deprem", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { vm.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            Text(
                text = "Zaman Filtresi",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            TimeSegmented(
                items = listOf("24 saat", "48 saat", "72 saat", "96 saat"),
                selectedIndex = vm.timeIndex,
                onSelect = vm::selectTime
            )

            Text(
                text = "son ${vm.timeLabel}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 8.dp),
                color = Color(0x99000000)
            )

            TabsListMap(
                selectedIndex = vm.tabIndex,
                onSelect = vm::selectTab
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (vm.tabIndex == 0) {
                    when {
                        vm.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        vm.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Hata: ${vm.error}")
                        }
                        vm.quakes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Seçili aralıkta Türkiye için deprem bulunamadı.\nZaman penceresini genişletmeyi deneyin.",
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> EarthquakeList(vm.quakes)
                    }
                } else {
                    EarthquakeMap(quakes = vm.quakes)
                }
            }

            Text(
                text = "Büyüklük (Mw)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            MagnitudeChips(
                items = listOf("≤ 2", "2–<4", "4–<6", "≥ 6", "sıfırla"),
                colors = listOf(MagGreen, MagYellow, MagOrange, MagRed, ddd),
                selected = vm.magSelection,
                onToggle = vm::toggleMagnitude,
                onReset = vm::clearMagnitudes
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // <<< EKLENEN KISIM: Ayarlar sheet'i >>>
    if (showSettings)    ModalBottomSheet(onDismissRequest = { showSettings = false },
              sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
              SettingsSheet(onClose = { showSettings = false })
    }
}

@Composable
private fun EarthquakeList(items: List<Earthquake>) {
    val fmt = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { q ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Magnitüd rozeti
                    Surface(
                        color = when {
                            (q.magnitude ?: 0.0) >= 6.0 -> MagRed
                            (q.magnitude ?: 0.0) >= 4.0 -> MagOrange
                            (q.magnitude ?: 0.0) >= 2.0 -> MagYellow
                            else -> MagGreen
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = q.magnitude?.let { String.format(Locale.US, "%.1f", it) } ?: "-",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(q.place ?: "Konum bilinmiyor", fontWeight = FontWeight.SemiBold)
                        val t = q.timeMillis?.let { fmt.format(Date(it)) } ?: "-"
                        Text(t, fontSize = 12.sp, color = Color(0x99000000))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSegmented(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                BorderStroke(1.dp, Color(0x11000000)),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { i, label ->
            val selected = i == selectedIndex
            val bg = if (selected) BlueSelected else ChipBg
            val textColor = if (selected) Color.White else Color(0xDD111111)

            TextButton(
                onClick = { onSelect(i) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = bg,
                    contentColor = textColor
                )
            ) {
                Text(label, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TabsListMap(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            Box(Modifier.fillMaxSize()) {
                val pos = tabPositions[selectedIndex]
                Box(
                    Modifier
                        .tabIndicatorOffset(pos)
                        .height(2.dp)
                        .fillMaxWidth(0.48f)                  // görseldeki kısa çizgi
                        .align(Alignment.BottomStart)
                        .padding(start = if (selectedIndex == 0) 24.dp else 0.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                )
            }
        },
        divider = { Divider(color = DividerGray, thickness = 1.dp) }
    ) {
        Tab(
            selected = selectedIndex == 0,
            onClick = { onSelect(0) },
            text = { Text("Liste", fontSize = 28.sp, fontWeight = FontWeight.SemiBold) }
        )
        Tab(
            selected = selectedIndex == 1,
            onClick = { onSelect(1) },
            text = { Text("Harita", fontSize = 28.sp, fontWeight = FontWeight.SemiBold) }
        )
    }
}

@Composable
private fun MagnitudeChips(
    items: List<String>,
    colors: List<Color>,
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEachIndexed { i, label ->
            val isSel = i in selected
            val isSıfırla = label == "sıfırla"
            val stroke = if (isSel) BorderStroke(2.dp, Color.Black.copy(alpha = 0.25f)) else null
            val bg = colors[i]
            val alpha = if (isSel) 1f else 0.4f

            Surface(
                onClick = {
                    if (isSıfırla) {
                        onReset()           // hepsi => sıfırla
                    } else {
                        onToggle(i)         // diğerleri normal
                    } },
                shape = RoundedCornerShape(16.dp),
                color = bg.copy(alpha = alpha),   // seçili değilse soluk
                border = if (isSel) BorderStroke(2.dp, Color.Black.copy(alpha = 0.4f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSel) Color.Black else Color.DarkGray
                    )
                }
            }
        }
    }
}