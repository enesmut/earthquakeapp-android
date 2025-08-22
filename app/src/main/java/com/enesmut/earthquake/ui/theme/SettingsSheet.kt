package com.enesmut.earthquake.ui.theme


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enesmut.earthquake.data.Province
import kotlin.math.roundToInt

// Kritik aksan rengi
private val Danger = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onClose: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val ui by vm.state.collectAsState()
    val provinces by vm.provinces.collectAsState()

    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Ayarlar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))
        Text("İl", style = MaterialTheme.typography.titleMedium)

        ProvinceDropdown(
            selectedName = ui.province,
            provinces = provinces,
            onSelect = { city -> vm.setProvince(city.text) }
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("Deprem Bildirimi", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Seçtiğin eşik aralığındaki depremleri bildir",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = ui.notificationsEnabled,
                onCheckedChange = vm::setNotificationsEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Danger,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        if (ui.notificationsEnabled) {
            Spacer(Modifier.height(12.dp))
            Text("Bildirim Eşiği (Mw)", style = MaterialTheme.typography.titleMedium)

            MagnitudeRangeSlider(
                value = ui.notifMin..ui.notifMax,
                onChange = { r -> vm.setNotifRange(r.first, r.last) },
                enabled = true
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = Danger,
                contentColor = Color.White
            )
        ) { Text("Kapat") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinceDropdown(
    selectedName: String,
    provinces: List<Province>,
    onSelect: (Province) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = remember(selectedName, provinces) {
        provinces.firstOrNull { it.text.equals(selectedName, true) }?.text ?: selectedName
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text("İl seç") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            provinces.forEach { city ->
                DropdownMenuItem(
                    text = { Text("${city.text} (${city.keyNo})") },
                    onClick = { onSelect(city); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun MagnitudeRangeSlider(
    value: IntRange,
    onChange: (IntRange) -> Unit,
    enabled: Boolean
) {
    var tmp by remember(value) { mutableStateOf(value.first.toFloat()..value.last.toFloat()) }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Seçilen aralık: ${tmp.start.toInt()}–${tmp.endInclusive.toInt()} Mw",
            style = MaterialTheme.typography.bodyMedium
        )

        RangeSlider(
            value = tmp,
            onValueChange = { tmp = it },
            valueRange = 1f..9f,
            steps = 7,
            enabled = enabled,
            colors = SliderDefaults.colors(
                activeTrackColor = Danger,
                inactiveTrackColor = Danger.copy(alpha = 0.25f),
                thumbColor = Danger
            ),
            onValueChangeFinished = {
                val s = tmp.start.roundToInt().coerceIn(1, 9)
                val e = tmp.endInclusive.roundToInt().coerceIn(s, 9)
                tmp = s.toFloat()..e.toFloat()
                onChange(s..e)
            }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val s = tmp.start.toInt()
            val e = tmp.endInclusive.toInt()
            (1..9).forEach { n ->
                Text(
                    text = n.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (n in s..e) Danger else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}