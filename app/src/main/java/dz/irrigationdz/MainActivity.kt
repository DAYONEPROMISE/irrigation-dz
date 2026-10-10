package dz.irrigationdz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import dz.irrigationdz.data.Agronomy
import dz.irrigationdz.data.ParcelEntity
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = (application as IrrigationApp).repository
        setContent { IrrigationDzApp(repo) }
    }
}

// ------------------------------------------------------------------
// Labels (ids -> localized strings)
// ------------------------------------------------------------------

@Composable
fun cropLabel(id: String): String = stringResource(
    when (id) {
        "wheat" -> R.string.crop_wheat
        "potato" -> R.string.crop_potato
        "tomato" -> R.string.crop_tomato
        "onion" -> R.string.crop_onion
        "maize" -> R.string.crop_maize
        "melon" -> R.string.crop_melon
        "pepper" -> R.string.crop_pepper
        "palm" -> R.string.crop_palm
        "citrus" -> R.string.crop_citrus
        else -> R.string.crop_olive
    }
)

@Composable
fun soilLabel(id: String): String = stringResource(
    when (id) {
        "sandy" -> R.string.soil_sandy
        "sandy_loam" -> R.string.soil_sandy_loam
        "loam" -> R.string.soil_loam
        else -> R.string.soil_clay
    }
)

@Composable
fun methodLabel(id: String): String = stringResource(
    when (id) {
        "drip" -> R.string.method_drip
        "sprinkler" -> R.string.method_sprinkler
        else -> R.string.method_surface
    }
)

@Composable
fun zoneLabel(id: String): String = stringResource(
    when (id) {
        "littoral" -> R.string.zone_littoral
        "hauts_plateaux" -> R.string.zone_hauts_plateaux
        "steppe" -> R.string.zone_steppe
        else -> R.string.zone_sahara
    }
)

@Composable
fun stageLabel(stage: Agronomy.GrowthStage): String = stringResource(
    when (stage) {
        Agronomy.GrowthStage.INITIAL -> R.string.stage_initial
        Agronomy.GrowthStage.DEVELOPMENT -> R.string.stage_development
        Agronomy.GrowthStage.MID -> R.string.stage_mid
        Agronomy.GrowthStage.LATE -> R.string.stage_late
    }
)

// ------------------------------------------------------------------
// App shell
// ------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationDzApp(repo: dz.irrigationdz.data.ParcelRepository) {
    var tab by remember { mutableStateOf(0) }
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        TextButton(onClick = {
                            val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                            val next = if (current.startsWith("ar")) "fr" else "ar"
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(next)
                            )
                        }) { Text(stringResource(R.string.lang)) }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        label = { Text(stringResource(R.string.tab_dose)) },
                        icon = {}
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        label = { Text(stringResource(R.string.tab_parcels)) },
                        icon = {}
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        label = { Text(stringResource(R.string.tab_plan)) },
                        icon = {}
                    )
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    0 -> DoseScreen()
                    1 -> ParcelsScreen(repo)
                    else -> PlanScreen(repo)
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// Shared input state + dropdown
// ------------------------------------------------------------------

class FieldInputs {
    var cropId by mutableStateOf("potato")
    var soilId by mutableStateOf("loam")
    var methodId by mutableStateOf("drip")
    var zoneId by mutableStateOf("hauts_plateaux")
    var stage by mutableStateOf(Agronomy.GrowthStage.MID)
    var areaHa by mutableStateOf("1")
    var flowLs by mutableStateOf("5")
    var customEto by mutableStateOf("")
    var rainMm by mutableStateOf("0")
}

@Composable
fun rememberFields() = remember { FieldInputs() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDown(
    label: String,
    options: List<T>,
    selected: T,
    labelOf: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = labelOf(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(labelOf(opt)) },
                    onClick = { onSelect(opt); expanded = false }
                )
            }
        }
    }
}

@Composable
fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> if (v.matches(Regex("[0-9.,]*"))) onValueChange(v.replace(',', '.')) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

fun parseDouble(v: String, fallback: Double = 0.0): Double = v.toDoubleOrNull() ?: fallback

@Composable
fun InputForm(f: FieldInputs) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DropDown(stringResource(R.string.crop), Agronomy.CROPS.map { it.id }, f.cropId, { cropLabel(it) }, { f.cropId = it })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                DropDown(stringResource(R.string.soil), Agronomy.SOILS.map { it.id }, f.soilId, { soilLabel(it) }, { f.soilId = it })
            }
            Column(Modifier.weight(1f)) {
                DropDown(stringResource(R.string.method), Agronomy.METHODS.map { it.id }, f.methodId, { methodLabel(it) }, { f.methodId = it })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                DropDown(stringResource(R.string.zone), Agronomy.ETO_ZONES.map { it.id }, f.zoneId, { zoneLabel(it) }, { f.zoneId = it })
            }
            Column(Modifier.weight(1f)) {
                DropDown(
                    stringResource(R.string.stage),
                    Agronomy.GrowthStage.entries, f.stage, { stageLabel(it) }, { f.stage = it }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) { NumberField(stringResource(R.string.area_ha), f.areaHa) { f.areaHa = it } }
            Column(Modifier.weight(1f)) { NumberField(stringResource(R.string.flow_ls), f.flowLs) { f.flowLs = it } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) { NumberField(stringResource(R.string.eto_custom), f.customEto) { f.customEto = it } }
            Column(Modifier.weight(1f)) { NumberField(stringResource(R.string.rain_mm), f.rainMm) { f.rainMm = it } }
        }
    }
}

fun computeFromFields(f: FieldInputs): Agronomy.DoseResult? {
    val area = parseDouble(f.areaHa)
    val flow = parseDouble(f.flowLs)
    if (area <= 0 || flow <= 0) return null
    val eto = f.customEto.toDoubleOrNull() ?: Agronomy.etoByZoneId(f.zoneId).etoMmPerDay
    return Agronomy.computeDose(
        crop = Agronomy.cropById(f.cropId),
        soil = Agronomy.soilById(f.soilId),
        method = Agronomy.methodById(f.methodId),
        etoMmPerDay = eto,
        stage = f.stage,
        areaHa = area,
        flowLs = flow,
        effectiveRainMm = parseDouble(f.rainMm)
    )
}

// ------------------------------------------------------------------
// Screens
// ------------------------------------------------------------------

@Composable
fun DoseScreen() {
    val f = rememberFields()
    val dose = computeFromFields(f)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        InputForm(f)
        Spacer(Modifier.height(12.dp))
        if (dose != null) DoseCard(dose) else Text("—")
    }
}

fun fmt(v: Double, digits: Int = 1): String =
    String.format(Locale.US, "%.${digits}f", v)

@Composable
fun DoseCard(d: Agronomy.DoseResult) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${stringResource(R.string.result_etc)}: ${fmt(d.etCropMmPerDay)} ${stringResource(R.string.mm_day)} (Kc=${fmt(d.kc, 2)})")
            Text("${stringResource(R.string.result_interval)}: ${d.intervalDays} ${stringResource(R.string.days_unit)}")
            Text("${stringResource(R.string.result_net)}: ${fmt(d.netMm)} mm")
            Text("${stringResource(R.string.result_gross)}: ${fmt(d.grossMm)} mm")
            Text("${stringResource(R.string.result_volume)}: ${fmt(d.volumeM3)} m³")
            Text(
                "${stringResource(R.string.result_duration)}: " +
                    "${d.durationHours.roundToInt()} h ${((d.durationHours % 1) * 60).roundToInt()} min"
            )
            Text("${stringResource(R.string.result_season)}: ${fmt(d.seasonNetM3PerHa, 0)} m³/ha")
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.disclaimer), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ParcelsScreen(repo: dz.irrigationdz.data.ParcelRepository) {
    val parcels by repo.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showEditor by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<ParcelEntity?>(null) }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { showEditor = true }) { Icon(Icons.Default.Add, null) }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (parcels.isEmpty()) {
                Text(stringResource(R.string.no_parcels))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(parcels, key = { it.id }) { p ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${cropLabel(p.cropId)} · ${fmt(p.areaHa)} ha · " +
                                            "${fmt(repo.doseFor(p).volumeM3)} m³ / " +
                                            "${repo.doseFor(p).intervalDays} ${stringResource(R.string.days_unit)}"
                                    )
                                }
                                IconButton(onClick = { toDelete = p }) {
                                    Icon(Icons.Default.Delete, stringResource(R.string.delete))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        ParcelEditor(
            onDismiss = { showEditor = false },
            onSave = { parcel ->
                scope.launch { repo.save(parcel); showEditor = false }
            }
        )
    }
    toDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            confirmButton = {
                TextButton(onClick = { scope.launch { repo.remove(p); toDelete = null } }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
            text = { Text(stringResource(R.string.confirm_delete)) }
        )
    }
}

@Composable
fun ParcelEditor(onDismiss: () -> Unit, onSave: (ParcelEntity) -> Unit) {
    val f = rememberFields()
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && computeFromFields(f) != null,
                onClick = {
                    onSave(
                        ParcelEntity(
                            name = name.trim(),
                            cropId = f.cropId,
                            soilId = f.soilId,
                            methodId = f.methodId,
                            etoZoneId = f.zoneId,
                            customEto = f.customEto.toDoubleOrNull(),
                            areaHa = parseDouble(f.areaHa),
                            flowLs = parseDouble(f.flowLs),
                            stageOrdinal = f.stage.ordinal
                        )
                    )
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        title = { Text(stringResource(R.string.add_parcel)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.parcel_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                InputForm(f)
            }
        }
    )
}

@Composable
fun PlanScreen(repo: dz.irrigationdz.data.ParcelRepository) {
    val parcels by repo.observeAll().collectAsState(initial = emptyList())
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        if (parcels.isEmpty()) {
            Text(stringResource(R.string.no_parcels))
        } else {
            parcels.forEach { p ->
                val dose = repo.doseFor(p)
                Card(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${p.name} — ${cropLabel(p.cropId)}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${stringResource(R.string.next_waterings)}: " +
                                "${stringResource(R.string.result_volume)} ${fmt(dose.volumeM3)} m³ / " +
                                "${dose.intervalDays} ${stringResource(R.string.days_unit)}"
                        )
                        Agronomy.planEvents(dose).forEach { ev ->
                            val when_ = if (ev.dayOffset == 0) stringResource(R.string.today)
                            else stringResource(R.string.in_x_days, ev.dayOffset)
                            Text("• $when_: ${fmt(ev.volumeM3)} m³ (${fmt(ev.durationHours)} h)")
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.disclaimer), style = MaterialTheme.typography.bodySmall)
        }
    }
}
