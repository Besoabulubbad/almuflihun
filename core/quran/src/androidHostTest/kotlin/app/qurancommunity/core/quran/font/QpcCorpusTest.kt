package app.qurancommunity.core.quran.font

import app.qurancommunity.core.model.MushafPage
import app.qurancommunity.core.quran.PlatformTest
import app.qurancommunity.core.quran.QuranDatabaseProvider
import app.qurancommunity.core.quran.newTestStorage
import app.qurancommunity.core.testing.runAppTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The real QPC V2 page fonts, when a folder of them is given with QPC_WOFF_DIR (they are never in the repository,
 * ADR 0013): every page decodes with every checksum right and maps every glyph code its words use. Skipped otherwise.
 */
class QpcCorpusTest : PlatformTest() {

    @Test
    fun every_real_page_font_passes_the_store_checks() = runAppTest {
        val dir = System.getenv("QPC_WOFF_DIR")?.takeIf { it.isNotBlank() }?.let(::File) ?: return@runAppTest
        val queries = QuranDatabaseProvider(newTestStorage()).get().quranQueries
        val failures = (1..MushafPage.COUNT).mapNotNull { page ->
            val glyphs = queries.pageGlyphs(page.toLong()).executeAsList()
                .flatMapTo(HashSet()) { glyph -> glyph.map { it.code } }
            runCatching { PageFontStore.verify(File(dir, "p$page.woff").readBytes(), glyphs) }
                .exceptionOrNull()?.let { "page $page: ${it.message}" }
        }
        assertEquals(emptyList(), failures)
        println("QPC corpus: ${MushafPage.COUNT} pages verified")
    }
}
