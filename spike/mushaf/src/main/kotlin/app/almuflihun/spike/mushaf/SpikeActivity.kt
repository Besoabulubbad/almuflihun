package app.almuflihun.spike.mushaf

import android.database.sqlite.SQLiteDatabase
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import androidx.compose.ui.text.font.Typeface as ComposeTypeface

/** One line of a printed page: its type, whether it is centred, and its words (glyph, ayah key). */
data class Line(val type: String, val centered: Boolean, val surah: Int, val words: List<Word>)
data class Word(val glyph: String, val ayahKey: String)

private const val TAG = "MushafSpike"
private val SAMPLE_PAGES = listOf(0, 1, 2, 3, 50, 51, 300, 301, 604) // 0 = Digital Khatt CFF2 check

class SpikeActivity : ComponentActivity() {

    private lateinit var layout: SQLiteDatabase
    private lateinit var words: SQLiteDatabase
    private lateinit var dkWords: SQLiteDatabase
    private var useDk by mutableStateOf(false)
    private val dkFamily by lazy { FontFamily(ComposeTypeface(Typeface.createFromAsset(assets, "fonts/dk.otf"))) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        layout = openAsset("layout.db")
        words = openAsset("words.db")
        dkWords = openAsset("dkwords.db")
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                var dark by remember { mutableStateOf(false) }
                val pager = rememberPagerState { SAMPLE_PAGES.size }
                Column(Modifier.fillMaxSize().background(if (dark) Color(0xFF141B2D) else Color(0xFFFBF7EE)).safeDrawingPadding()) {
                    Row {
                        BasicText(
                            text = if (dark) "light" else "dark",
                            modifier = Modifier.padding(12.dp).clickable { dark = !dark },
                            style = TextStyle(color = if (dark) Color.White else Color.Black),
                        )
                        BasicText(
                            text = if (useDk) "font: Digital Khatt (tap for QPC V2)" else "font: QPC V2 (tap for Digital Khatt)",
                            modifier = Modifier.padding(12.dp).clickable { useDk = !useDk },
                            style = TextStyle(color = if (dark) Color.White else Color.Black),
                        )
                    }
                    HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { index ->
                        MushafPage(SAMPLE_PAGES[index], dark, useDk)
                    }
                }
            }
        }
    }

    @Composable
    private fun MushafPage(page: Int, dark: Boolean, dk: Boolean) {
        if (page == 0) {
            val dk = remember { FontFamily(ComposeTypeface(Typeface.createFromAsset(assets, "fonts/dk.otf"))) }
            val text = remember { assets.open("sample.txt").bufferedReader().readText().trim() }
            BasicText(text, Modifier.fillMaxSize().padding(16.dp), style = TextStyle(fontFamily = dk, fontSize = 28.sp, color = if (dark) Color.White else Color.Black, textAlign = TextAlign.Justify, lineHeight = 60.sp))
            return
        }
        val (lines, family) = remember(page, dk) { loadPage(page, dk) }
        var selected by remember { mutableStateOf<String?>(null) }
        val measurer = rememberTextMeasurer()
        val ink = if (dark) Color(0xFFEDE9E1) else Color(0xFF1D1C1A)
        val start = remember(page) { System.nanoTime() }
        var reported by remember(page) { mutableStateOf(false) }
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp).drawWithContent {
                drawContent()
                if (!reported) {
                    reported = true
                    Log.i(TAG, "page $page first draw ${(System.nanoTime() - start) / 1_000_000} ms after composition")
                }
            },
        ) {
            val density = LocalDensity.current
            val width = constraints.maxWidth.toFloat()
            // Font size: the widest justified line at 100 sp, scaled so it fills the page width.
            val fontSize = remember(page, width, dk) {
                val justified = lines.filter { it.type == "ayah" && !it.centered }
                val widest = justified.ifEmpty { lines.filter { it.type == "ayah" } }.maxOfOrNull { line ->
                    measurer.measure(line.words.joinToString(if (dk) " " else "") { it.glyph }, TextStyle(fontFamily = family, fontSize = 100.sp)).size.width
                } ?: 1
                val byWidth = 100f * width / widest * (if (justified.isEmpty()) 0.75f else 0.97f)
                val byHeight = with(density) { (constraints.maxHeight / 15f / 1.9f).toSp().value }
                minOf(byWidth, byHeight).sp
            }
            val lineHeight: Dp = with(density) { (constraints.maxHeight / 15f).toDp() }
            Column(Modifier.fillMaxSize()) {
                lines.forEach { line ->
                    Box(Modifier.fillMaxWidth().height(lineHeight), contentAlignment = Alignment.Center) {
                        when (line.type) {
                            "surah_name" -> BasicText("سورة ${line.surah}", style = TextStyle(color = ink, fontSize = 18.sp))
                            "basmallah" -> BasicText("﷽", style = TextStyle(color = ink, fontSize = 30.sp, textAlign = TextAlign.Center))
                            else -> Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = if (line.centered) Arrangement.Center else Arrangement.SpaceBetween,
                            ) {
                                line.words.forEach { word ->
                                    val isSelected = word.ayahKey == selected
                                    BasicText(
                                        text = word.glyph,
                                        modifier = Modifier
                                            .background(if (isSelected) Color(0x552F4A7A) else Color.Transparent)
                                            .clickable { selected = word.ayahKey },
                                        style = TextStyle(fontFamily = family, fontSize = fontSize, color = ink),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadPage(page: Int, dk: Boolean): Pair<List<Line>, FontFamily> {
        val t0 = System.nanoTime()
        val typeface = if (dk) null else Typeface.createFromAsset(assets, "fonts/p$page.ttf")
        val t1 = System.nanoTime()
        val lines = mutableListOf<Line>()
        layout.rawQuery(
            "select line_type, is_centered, first_word_id, last_word_id, surah_number from pages where page_number = ? order by line_number",
            arrayOf(page.toString()),
        ).use { c ->
            while (c.moveToNext()) {
                val type = c.getString(0)
                val ws = if (type == "ayah") wordsBetween(if (dk) dkWords else words, c.getInt(2), c.getInt(3), dk) else emptyList()
                lines += Line(type, c.getInt(1) == 1, if (c.isNull(4)) 0 else c.getInt(4), ws)
            }
        }
        val t2 = System.nanoTime()
        Log.i(TAG, "page $page: font ${(t1 - t0) / 1_000_000} ms, layout+words ${(t2 - t1) / 1_000_000} ms")
        return lines to (if (dk) dkFamily else FontFamily(ComposeTypeface(typeface!!)))
    }

    private fun wordsBetween(db: SQLiteDatabase, first: Int, last: Int, dk: Boolean): List<Word> =
        db.rawQuery("select text, surah, ayah from words where id between ? and ? order by id", arrayOf("$first", "$last")).use { c ->
            buildList { while (c.moveToNext()) add(Word(c.getString(0), "${c.getInt(1)}:${c.getInt(2)}")) }
        }

    private fun openAsset(name: String): SQLiteDatabase {
        val file = File(filesDir, name)
        if (!file.exists()) assets.open(name).use { input -> file.outputStream().use { input.copyTo(it) } }
        return SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
    }
}
