package videoTrade.screonPlayer.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import videoTrade.screonPlayer.app.androidApi.ContextObj

// --------------------------------------
// 1) Модель и парсер M3U (из вашего кода)
// --------------------------------------

data class IptvChannel(
    val name: String,
    val url: String,
    val tvgId: String? = null,
    val group: String? = null,
    val logo: String? = null
)

fun parseM3U(text: String): List<IptvChannel> {
    val lines = text.lines()
    val res = mutableListOf<IptvChannel>()
    var pendingName = ""
    var tvgId: String? = null
    var group: String? = null
    var logo: String? = null

    val extinfRegex = Regex("""#EXTINF:-?\d+\s*(.*),\s*(.*)""")
    fun parseAttrs(attrs: String): Map<String, String> =
        Regex("""(\w+?)=\"(.*?)\"""").findAll(attrs)
            .associate { it.groupValues[1] to it.groupValues[2] }

    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        if (line.startsWith("#EXTINF", true)) {
            val match = extinfRegex.find(line)
            if (match != null) {
                val attrs = parseAttrs(match.groupValues[1])
                pendingName = match.groupValues[2]
                tvgId = attrs["tvg-id"]
                group = attrs["group-title"]
                logo = attrs["tvg-logo"]
            }
        } else if (line.isNotBlank() && !line.startsWith("#")) {
            res += IptvChannel(
                name = pendingName.ifBlank { line },
                url = line,
                tvgId = tvgId,
                group = group,
                logo = logo
            )
            pendingName = ""
            tvgId = null; group = null; logo = null
        }
        i++
    }
    return res
}

// --------------------------------------
// 2) VLC player-обёртка (исправлена очистка, headers и lifecycle)
// --------------------------------------

class VlcPlayer(
    context: Context,
    private val videoLayout: VLCVideoLayout    // <-- было SurfaceView
) {
    private val libVlc = LibVLC(context, arrayListOf(
        "--network-caching=1500",
        "--rtsp-tcp",
        "--no-drop-late-frames",
        "--no-skip-frames"
    ))
    private val mediaPlayer = MediaPlayer(libVlc)

    fun attach() {
        mediaPlayer.attachViews(videoLayout, null, false, false) // <-- теперь VLCVideoLayout
    }

    fun play(url: String, headers: Map<String, String> = emptyMap()) {
        val media = Media(libVlc, Uri.parse(url))
        headers["User-Agent"]?.let { media.addOption(":http-user-agent=$it") }
        headers["Referer"]?.let { media.addOption(":http-referrer=$it") }
        mediaPlayer.media = media
        media.release()
        mediaPlayer.play()
    }

    fun pause() = mediaPlayer.pause()
    fun stop() = mediaPlayer.stop()

    fun release() {
        runCatching { mediaPlayer.stop() }
        mediaPlayer.detachViews()
        mediaPlayer.release()
        libVlc.release()
    }
}

@Composable
fun VlcPlayerView(
    url: String,
    headers: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val context = ContextObj.getContext()

    var layout by remember { mutableStateOf<VLCVideoLayout?>(null) }
    val player = remember(layout) { layout?.let { VlcPlayer(context, it).apply { attach() } } }

    // автоплей при смене url/headers
    LaunchedEffect(player, url, headers) {
        player?.play(url, headers)
    }

    // пауза/освобождение по жизненному циклу
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE,
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> player?.pause()
                androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> player?.release()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player?.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            VLCVideoLayout(ctx).also { layout = it }   // <-- не SurfaceView
        },
        modifier = modifier
    )
}
// --------------------------------------
// 3) Multicast helper (для udp://@ )
// --------------------------------------

class MulticastHelper(private val context: Context) {
    private var lock: WifiManager.MulticastLock? = null
    fun acquire() {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        lock = wifi.createMulticastLock("iptv-multicast").apply {
            setReferenceCounted(true)
            acquire()
        }
    }

    fun release() {
        lock?.let { if (it.isHeld) it.release() }
        lock = null
    }
}

// --------------------------------------
// 4) ViewModel-like слой: загрузка плейлиста и выбор канала
// --------------------------------------

data class IptvViewState(
    val channels: List<IptvChannel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selected: IptvChannel? = null,
    val query: String = "",
    val group: String = ""
)

class IptvController(private val client: OkHttpClient = OkHttpClient()) {
    private val _state = MutableStateFlow(IptvViewState())
    val state: StateFlow<IptvViewState> = _state

    fun setChannels(list: List<IptvChannel>) {
        _state.value = IptvViewState(channels = list)
    }

    fun select(channel: IptvChannel?) {
        _state.value = _state.value.copy(selected = channel)
    }
    fun search(text: String) {
        _state.value = _state.value.copy(query = text)
    }
    fun setGroup(group: String) {
        _state.value = _state.value.copy(group = group)
    }

    fun loadPlaylist(url: String) { /* как было */ }
}

// --------------------------------------
// 5) Экран: ввод URL, список каналов, плеер
// --------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IptvScreen(
    defaultPlaylistUrl: String = "",
    defaultHeaders: Map<String, String> = emptyMap()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val controller = remember { IptvController() }
    val state by controller.state.collectAsStateWithLifecycle()

    var playlistUrl by remember { mutableStateOf(defaultPlaylistUrl) }

    // Разрешение на multicast (только если нужно)
    val permission = Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
    val hasPermission =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    val permLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        if (defaultPlaylistUrl.isNotBlank()) controller.loadPlaylist(defaultPlaylistUrl)
        if (!hasPermission) permLauncher.launch(permission)
    }

    val multicast = remember { MulticastHelper(context) }

    // Включаем multicast lock, если выбранный канал udp://@
    DisposableEffect(state.selected?.url) {
        val isUdp = state.selected?.url?.startsWith("udp://@") == true
        if (isUdp) multicast.acquire() else multicast.release()
        onDispose { multicast.release() }
    }

    LaunchedEffect(Unit) {
        // Если хочешь всегда стартовать с встроенного списка:
        controller.setChannels(predefinedChannels)

        // Либо, если передали URL, можно поверх подгрузить M3U:
        if (defaultPlaylistUrl.isNotBlank()) controller.loadPlaylist(defaultPlaylistUrl)

        if (!hasPermission) permLauncher.launch(permission)
    }


    Column(Modifier
        .fillMaxSize()
        .padding(12.dp)) {




        Spacer(Modifier.height(8.dp))

        // Список каналов
        val filtered = remember(state.channels, state.query, state.group) {
            state.channels.filter { ch ->
                (state.query.isBlank() || ch.name.contains(state.query, ignoreCase = true)) &&
                        (state.group.isBlank() || ch.group.equals(state.group, true))
            }
        }


        Spacer(Modifier.height(8.dp))

        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        state.error?.let { Text("Ошибка: ${'$'}it", color = MaterialTheme.colorScheme.error) }

        Row(Modifier.fillMaxSize()) {
            // Список
            LazyColumn(Modifier.weight(1f)) {
                items(filtered) { ch ->
                    ChannelRow(channel = ch, selected = (ch == state.selected)) {
                        controller.select(ch)
                    }
                    Divider()
                }
            }

            Spacer(Modifier.width(8.dp))

            // Плеер (правее). Можно поменять на нижнюю панель на телефонах
            Box(Modifier
                .weight(1f)
                .fillMaxHeight()) {
                val current = state.selected
                if (current != null) {
                    VlcPlayerView(
                        url = current.url,
                        headers = defaultHeaders,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Выберите канал")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(channel: IptvChannel, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                channel.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = if (selected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
            )
            val meta = listOfNotNull(channel.group, channel.tvgId).joinToString(" · ")
            if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick = onClick) { Text(if (selected) "Идёт" else "Играть") }
    }
}

// --------------------------------------
// 6) Как вызвать экран из Activity/Screen
// --------------------------------------

/*
@Composable
fun App() {
    MaterialTheme { IptvScreen(
        defaultPlaylistUrl = "https://example.com/playlist.m3u",
        defaultHeaders = mapOf(
            // если провайдер требует:
            // "User-Agent" to "MyApp/1.0",
            // "Referer" to "https://example.com/",
        )
    ) }
}
*/


// Предустановленные каналы (HLS)
val predefinedChannels = listOf(
    IptvChannel(
        name = "Первый канал",
        url = "http://ottrc.crd-s.net/154/0faf669a8b1a94c71c96/live.m3u8",
        group = "Общероссийские (SD)",
        logo = "https://mvvv.eu/b/templates/ottch/pervyj.png"
    ),
    IptvChannel(
        name = "СТС International",
        url = "http://ottrc.crd-s.net/654/0faf669a8b1a94c71c96/live.m3u8",
        group = "Общероссийские (SD)",
        logo = "https://mvvv.eu/b/templates/ottch/sts-int.png"
    ),
    IptvChannel(
        name = "СТС",
        url = "http://ottrc.crd-s.net/161/0faf669a8b1a94c71c96/live.m3u8",
        group = "Общероссийские (SD)",
        logo = "https://mvvv.eu/b/templates/ottch/sts.png"
    )
)
