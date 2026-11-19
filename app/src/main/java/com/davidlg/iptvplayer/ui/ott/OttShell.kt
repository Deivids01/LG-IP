package com.davidlg.iptvplayer.ui.ott

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidlg.iptvplayer.MainViewModel
import com.davidlg.iptvplayer.OttTab
import com.davidlg.iptvplayer.R
import com.davidlg.iptvplayer.Screen
import com.davidlg.iptvplayer.ui.Poppins
import com.davidlg.iptvplayer.ui.dlClickable
import com.davidlg.iptvplayer.ui.profiles.ProfileAvatar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Modo OTT (el "nuevo"): barra superior + Inicio / TV en vivo / VOD / Mi lista. */
@Composable
fun OttShell(vm: MainViewModel) {
    Column(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0B1728), Color(0xFF070D18), Color(0xFF050A12)))
        )
    ) {
        OttTopBar(vm)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (vm.ottTab) {
                OttTab.Start -> OttStart(vm)
                OttTab.Live -> OttLive(vm)
                OttTab.Movie -> OttVod(vm, "movie", "Películas")
                OttTab.Series -> OttVod(vm, "series", "Series")
                OttTab.Anime -> OttVod(vm, "anime", "Anime")
                OttTab.Fav -> OttVod(vm, "fav", "Mi lista")
            }
        }
        if (LocalConfiguration.current.screenWidthDp < 600) {
            OttPhoneBottomBar(vm)
        }
    }
}

@Composable
private fun OttPhoneBottomBar(vm: MainViewModel) {
    Row(
        Modifier.fillMaxWidth().height(66.dp).background(Color(0xF207101D)).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val phoneTabs = listOf(OttTab.Start, OttTab.Live, OttTab.Movie, OttTab.Series, OttTab.Fav)
        for (tab in phoneTabs) {
            val def = TABS.first { it.tab == tab }
            val selected = vm.ottTab == tab
            Column(
                Modifier.weight(1f).fillMaxHeight().dlClickable(RoundedCornerShape(10.dp), Color.White, 1.dp, 1f) { vm.ottTab = tab },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SvgIcon(def.ico, 20.dp, if (selected) OttGold else OttText.copy(alpha = 0.68f), 1.8f)
                Text(def.label, color = if (selected) OttGold else OttText.copy(alpha = 0.72f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, fontFamily = Poppins, maxLines = 1)
            }
        }
    }
}

private data class TabDef(val tab: OttTab, val label: String, val ico: List<String>)

private val TABS = listOf(
    TabDef(OttTab.Start, "Inicio", Ico.home),
    TabDef(OttTab.Movie, "Películas", Ico.movie),
    TabDef(OttTab.Series, "Series", Ico.series),
    TabDef(OttTab.Anime, "Anime", Ico.anime),
    TabDef(OttTab.Live, "TV en vivo", Ico.live),
    TabDef(OttTab.Fav, "Mi lista", Ico.fav),
)

@Composable
private fun OttTopBar(vm: MainViewModel) {
    val profile by vm.activeProfile.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val filter by vm.sourceFilter.collectAsState()
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(1000) } }

    val compact = LocalConfiguration.current.screenWidthDp < 900
    if (compact) {
        Column(Modifier.fillMaxWidth().background(Color(0xEB0B1728)).padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.logo_davidlg), "DAVIDLG IPTV PLAYER", Modifier.width(118.dp).height(40.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(38.dp).dlClickable(RoundedCornerShape(12.dp), Color.White, 2.dp, 1f) { vm.showProfileSwitcher() }) { ProfileAvatar(profile.av, 38.dp) }
                Box(Modifier.padding(start = 8.dp).size(40.dp).dlClickable(RoundedCornerShape(50), Color.White, 2.dp, 1f) { vm.screen = Screen.Settings }
                    .background(Color(0x1FFFFFFF), RoundedCornerShape(50)), contentAlignment = Alignment.Center) { SvgIcon(Ico.gear, 22.dp, OttText, 1.8f) }
            }
            if (LocalConfiguration.current.screenWidthDp >= 600) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    for (t in TABS) {
                        val selected = vm.ottTab == t.tab
                        Row(Modifier.height(38.dp).dlClickable(RoundedCornerShape(40), Color.White, 2.dp, 1f) { vm.ottTab = t.tab }
                            .background(if (selected) Color(0x35168BFF) else Color.Transparent, RoundedCornerShape(40))
                            .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            SvgIcon(t.ico, 16.dp, if (selected) Color.White else OttText.copy(alpha = 0.75f), 1.8f)
                            Text(t.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = Poppins, maxLines = 1)
                        }
                    }
                }
            }
        }
    } else {
    Row(
        Modifier.fillMaxWidth().height(92.dp)
            .background(Color(0xEB0B1728))
            .padding(start = 40.dp, end = 34.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.logo_davidlg), "DAVIDLG IPTV PLAYER", Modifier.size(210.dp, 76.dp), contentScale = ContentScale.Fit)
        Spacer(Modifier.width(69.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            for (t in TABS) {
                val sel = vm.ottTab == t.tab
                var foc by remember { mutableStateOf(false) }
                Row(
                    Modifier.height(55.dp)
                        .dlClickable(RoundedCornerShape(50), Color.White, 2.dp, 1f, { foc = it }) { vm.ottTab = t.tab }
                        .background(if (sel) Color(0x24168BFF) else Color.Transparent)
                        .then(if (sel) Modifier.border(1.dp, Color(0x73168BFF), RoundedCornerShape(50)) else Modifier)
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    SvgIcon(t.ico, 24.dp, if (sel) Color.White else OttText.copy(alpha = 0.78f), 1.8f)
                    Text(
                        t.label, color = if (sel) Color.White else OttText.copy(alpha = 0.78f),
                        fontSize = 19.sp, fontWeight = FontWeight.SemiBold, fontFamily = Poppins, maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        // Reloj
        Column(Modifier.width(237.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                SimpleDateFormat("EEEE · d MMMM", ES).format(now).uppercase(), color = OttGoldDim,
                fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, letterSpacing = 2.6.sp, maxLines = 1,
            )
            Text(
                SimpleDateFormat("hh:mm:ss a", Locale.US).format(now).uppercase(), color = Color.White,
                fontSize = 34.sp, fontWeight = FontWeight.Black, fontFamily = Poppins, letterSpacing = 1.4.sp, maxLines = 1,
            )
        }
        Spacer(Modifier.width(14.dp))
        // Botón FUENTE
        val srcName = when {
            playlists.isEmpty() -> "Sin listas"
            filter == "all" -> "Todas las listas · ${playlists.size}"
            else -> playlists.firstOrNull { it.id == filter }?.name ?: "Todas las listas"
        }
        Row(
            Modifier.width(257.dp).height(55.dp)
                .dlClickable(RoundedCornerShape(50), Color.White, 2.dp, 1f) {
                    val ids = listOf("all") + playlists.map { it.id }
                    vm.setSourceFilter(ids[(ids.indexOf(filter).coerceAtLeast(0) + 1) % ids.size])
                }
                .background(Brush.linearGradient(listOf(Color(0x38168BFF), Color(0x0AFFFFFF))))
                .border(1.dp, Color(0x80168BFF), RoundedCornerShape(50))
                .padding(start = 22.dp, end = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("FUENTE", color = OttGoldDim, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = Poppins, letterSpacing = 2.6.sp)
                Text(srcName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins, maxLines = 1, letterSpacing = 1.sp)
            }
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(50)).background(Color(0x4D168BFF)), contentAlignment = Alignment.Center) {
                Text("↻", color = Color.White, fontSize = 20.sp)
            }
        }
        Spacer(Modifier.width(14.dp))
        // Perfil
        Box(
            Modifier.size(60.dp).dlClickable(RoundedCornerShape(18.dp), Color.White, 3.dp, 1.06f) { vm.showProfileSwitcher() }
                .border(2.dp, Color(0x38FFFFFF), RoundedCornerShape(18.dp))
        ) { ProfileAvatar(profile.av, 60.dp) }
        Spacer(Modifier.width(12.dp))
        // Ajustes
        Box(
            Modifier.size(60.dp).dlClickable(RoundedCornerShape(50), Color.White, 3.dp, 1.06f) { vm.screen = Screen.Settings }
                .background(Brush.linearGradient(listOf(Color(0x1FFFFFFF), Color(0x08FFFFFF))))
                .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) { SvgIcon(Ico.gear, 28.dp, OttText, 1.8f) }
    }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x1F168BFF)))
}
