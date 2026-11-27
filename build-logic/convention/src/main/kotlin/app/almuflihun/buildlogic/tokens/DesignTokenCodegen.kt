package app.almuflihun.buildlogic.tokens

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Turns `design/tokens.json` into Kotlin source for `core:designsystem` (ADR 0001). Every value the app may use comes
 * from here; nothing is typed by hand. Numeric groups become `Dp` objects, colours become one palette class per theme
 * (light and dark share names), text roles become specs that the theme binds to the bundled fonts, and motion becomes
 * durations and easings. Prose entries in the token file (rules, notes) are documentation and are not generated.
 */
object DesignTokenCodegen {

    const val PACKAGE = "app.almuflihun.core.designsystem.tokens"

    private val DIMENSION_GROUPS = listOf("space" to "Space", "radius" to "Radius", "size" to "Size", "border" to "Border")

    /** One generated file: its name and content. */
    data class GeneratedFile(val name: String, val content: String)

    fun generate(tokensJson: String): List<GeneratedFile> {
        val root = Json.parseToJsonElement(tokensJson).jsonObject
        return listOf(
            GeneratedFile("Dimensions.kt", dimensions(root)),
            GeneratedFile("Palette.kt", palette(root)),
            GeneratedFile("TextRoles.kt", textRoles(root)),
            GeneratedFile("Motion.kt", motion(root)),
        )
    }

    private fun header(imports: List<String>) = buildString {
        appendLine("// Generated from design/tokens.json by almuflihun.tokens. Do not edit: change the token file.")
        appendLine("@file:Suppress(\"MagicNumber\", \"ktlint\")")
        appendLine()
        appendLine("package $PACKAGE")
        appendLine()
        imports.forEach { appendLine("import $it") }
        if (imports.isNotEmpty()) appendLine()
    }

    private fun dimensions(root: JsonObject) = buildString {
        append(header(listOf("androidx.compose.ui.unit.Dp", "androidx.compose.ui.unit.dp")))
        DIMENSION_GROUPS.forEach { (key, objectName) ->
            val group = root.getValue(key).jsonObject
            appendLine("/** `$key` tokens. */")
            appendLine("object $objectName {")
            group.forEach { (name, value) ->
                val number = (value as JsonPrimitive).double
                appendLine("    val ${identifier(name)}: Dp = ${number.literal()}.dp")
            }
            appendLine("}")
            appendLine()
        }
    }.trimEnd() + "\n"

    private fun palette(root: JsonObject) = buildString {
        append(header(listOf("androidx.compose.runtime.Immutable", "androidx.compose.ui.graphics.Color")))
        val themes = root.getValue("color").jsonObject
        val light = themes.getValue("light").jsonObject
        val dark = themes.getValue("dark").jsonObject
        require(light.keys == dark.keys) { "light and dark colour groups differ: ${light.keys} vs ${dark.keys}" }
        light.forEach { (group, lightValues) ->
            val darkValues = dark.getValue(group).jsonObject
            require(lightValues.jsonObject.keys == darkValues.keys) { "colour group '$group' differs between themes" }
            val className = group.replaceFirstChar { it.uppercase() } + "Colors"
            appendLine("/** `color.<theme>.$group`. */")
            appendLine("@Immutable")
            appendLine("class $className(")
            lightValues.jsonObject.keys.forEach { appendLine("    val ${identifier(it)}: Color,") }
            appendLine(")")
            appendLine()
        }
        appendLine("/** Every colour role of one theme. Light and dark have the same names (DESIGN §4, theme rules). */")
        appendLine("@Immutable")
        appendLine("class AppPalette(")
        light.keys.forEach { appendLine("    val ${identifier(it)}: ${it.replaceFirstChar { c -> c.uppercase() }}Colors,") }
        appendLine(")")
        appendLine()
        listOf("Light" to light, "Dark" to dark).forEach { (name, theme) ->
            appendLine("/** `color.${name.lowercase()}`. */")
            appendLine("val ${name}Palette: AppPalette = AppPalette(")
            theme.forEach { (group, values) ->
                val className = group.replaceFirstChar { it.uppercase() } + "Colors"
                appendLine("    ${identifier(group)} = $className(")
                values.jsonObject.forEach { (role, hex) -> appendLine("        ${identifier(role)} = ${color(hex)},") }
                appendLine("    ),")
            }
            appendLine(")")
            appendLine()
        }
    }.trimEnd() + "\n"

    private fun textRoles(root: JsonObject) = buildString {
        append(
            header(
                listOf(
                    "androidx.compose.runtime.Immutable",
                    "androidx.compose.ui.text.TextStyle",
                    "androidx.compose.ui.text.font.FontFamily",
                    "androidx.compose.ui.text.font.FontWeight",
                    "androidx.compose.ui.unit.sp",
                ),
            ),
        )
        appendLine("/** Which bundled family a role uses: the UI font or the Quran font (DESIGN §3). */")
        appendLine("enum class FontRole { Ui, Quran }")
        appendLine()
        appendLine("/** One text role: font, size in sp, weight, and line height as a multiple of the size. */")
        appendLine("@Immutable")
        appendLine("class TextRoleSpec(val font: FontRole, val sizeSp: Float, val weight: Int, val lineHeight: Float)")
        appendLine()
        appendLine("/** The only text styles in the app (DESIGN §3). The theme binds them to fonts. */")
        appendLine("object TextRoles {")
        root.getValue("text").jsonObject.forEach { (name, spec) ->
            val s = spec.jsonObject
            val font = when (val f = s.getValue("font").jsonPrimitive.content) {
                "ui" -> "FontRole.Ui"
                "quran" -> "FontRole.Quran"
                else -> error("text.$name has unknown font '$f'")
            }
            val weight = s.getValue("weight").jsonPrimitive.int
            require(weight in setOf(400, 600, 700)) { "text.$name weight $weight is not 400, 600 or 700 (DESIGN §3)" }
            appendLine(
                "    val ${identifier(name)}: TextRoleSpec = TextRoleSpec($font, " +
                    "${s.getValue("size").jsonPrimitive.double.literal()}f, $weight, " +
                    "${s.getValue("lineHeight").jsonPrimitive.double.literal()}f)",
            )
        }
        appendLine("}")
        appendLine()
        val roles = root.getValue("text").jsonObject
        appendLine("/** Every text role as a ready style, bound to the bundled fonts by the theme (DESIGN §3). */")
        appendLine("@Immutable")
        appendLine("class AppTextStyles(")
        roles.keys.forEach { appendLine("    val ${identifier(it)}: TextStyle,") }
        appendLine(")")
        appendLine()
        appendLine("/** Builds [AppTextStyles] from the role specs and the two font families. */")
        appendLine("fun appTextStyles(ui: FontFamily, quran: FontFamily): AppTextStyles = AppTextStyles(")
        roles.keys.forEach { appendLine("    ${identifier(it)} = TextRoles.${identifier(it)}.style(ui, quran),") }
        appendLine(")")
        appendLine()
        appendLine("private fun TextRoleSpec.style(ui: FontFamily, quran: FontFamily): TextStyle = TextStyle(")
        appendLine("    fontFamily = if (font == FontRole.Quran) quran else ui,")
        appendLine("    fontSize = sizeSp.sp,")
        appendLine("    fontWeight = FontWeight(weight),")
        appendLine("    lineHeight = (sizeSp * lineHeight).sp,")
        appendLine(")")
    }

    private fun motion(root: JsonObject) = buildString {
        append(header(listOf("androidx.compose.animation.core.CubicBezierEasing", "androidx.compose.animation.core.Easing")))
        val motion = root.getValue("motion").jsonObject
        appendLine("/** `motion.duration`, in milliseconds (DESIGN §11). */")
        appendLine("object Durations {")
        motion.getValue("duration").jsonObject.forEach { (name, ms) ->
            appendLine("    const val ${identifier(name)}: Int = ${ms.jsonPrimitive.int}")
        }
        appendLine("}")
        appendLine()
        appendLine("/** `motion.easing`, cubic Bézier control points (DESIGN §11). */")
        appendLine("object Easings {")
        motion.getValue("easing").jsonObject.forEach { (name, points) ->
            val p = (points as JsonArray).map { it.jsonPrimitive.double.literal() + "f" }
            require(p.size == 4) { "motion.easing.$name needs 4 control points" }
            appendLine("    val ${identifier(name)}: Easing = CubicBezierEasing(${p.joinToString(", ")})")
        }
        appendLine("}")
        appendLine()
        val press = motion.getValue("press").jsonObject
        val focus = motion.getValue("focus").jsonObject
        appendLine("/** Numeric press and focus values (DESIGN §11). */")
        appendLine("object Interaction {")
        appendLine("    const val cardPressScale: Float = ${press.getValue("cardScale").jsonPrimitive.double.literal()}f")
        appendLine("    const val focusRingDp: Float = ${focus.getValue("ring").jsonPrimitive.double.literal()}f")
        appendLine("    const val focusOffsetDp: Float = ${focus.getValue("offset").jsonPrimitive.double.literal()}f")
        appendLine("}")
    }

    private fun color(value: JsonElement): String {
        val hex = value.jsonPrimitive.content
        require(Regex("#[0-9A-Fa-f]{6}").matches(hex)) { "colour '$hex' is not #RRGGBB" }
        return "Color(0xFF${hex.drop(1).uppercase()})"
    }

    /** Token names are already camelCase; guard against anything that is not a valid Kotlin identifier. */
    private fun identifier(name: String): String {
        require(Regex("[a-zA-Z][a-zA-Z0-9]*").matches(name)) { "token name '$name' is not a valid identifier" }
        return name
    }

    private fun Double.literal(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()
}
