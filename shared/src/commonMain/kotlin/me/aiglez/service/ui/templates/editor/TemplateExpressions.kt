package me.aiglez.service.ui.templates.editor

import me.aiglez.service.domain.models.FieldType
import me.aiglez.service.domain.models.DataRecord
import me.aiglez.service.domain.models.DataSchema
import me.aiglez.service.domain.models.SchemaField
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

data class TemplateExpressionContext(
    val data: Map<String, Any?> = emptyMap(),
    val roots: Map<String, Any?> = mapOf("data" to data),
)

fun renderTemplateText(
    value: String,
    context: TemplateExpressionContext = TemplateExpressionContext(),
): String {
    return TemplateExpressionPattern.replace(value) { match ->
        val expression = match.groupValues[1].trim()
        TemplateExpressionEvaluator(context).evaluate(expression)
            ?.let(::stringifyExpressionValue)
            ?: match.value
    }
}

fun renderLegacyPlaceholder(
    value: String,
    context: TemplateExpressionContext,
): String {
    val slug = value.removePrefix("[DataRecord:").removeSuffix("]")
    return context.data[slug]?.let(::stringifyExpressionValue) ?: value
}

fun sampleExpressionContext(fields: List<SchemaField>): TemplateExpressionContext {
    return TemplateExpressionContext(
        data = fields.filter { it.slug.isNotBlank() }.associate { field -> field.slug to sampleFieldValue(field) },
    )
}

fun sampleSchemaExpressionContext(
    schemas: List<DataSchema>,
    primarySchema: DataSchema? = schemas.firstOrNull(),
): TemplateExpressionContext {
    val primaryData = primarySchema?.let(::sampleSchemaData).orEmpty()
    return expressionContextWithRoots(
        data = primaryData,
        schemaRoots = schemas.associateSchemaRoots { schema -> sampleSchemaData(schema) },
    )
}

fun recordExpressionContext(
    schema: DataSchema,
    record: DataRecord,
): TemplateExpressionContext {
    val data = schemaRecordData(schema, record)
    return expressionContextWithRoots(
        data = data,
        schemaRoots = schemaExpressionAliases(schema).associateWith { data },
    )
}

fun recordExpressionContext(
    schemas: List<DataSchema>,
    recordsBySchemaId: Map<String, DataRecord>,
    primarySchema: DataSchema? = schemas.firstOrNull(),
): TemplateExpressionContext {
    val primaryData = primarySchema?.let { schema ->
        recordsBySchemaId[schema.id]?.let { record -> schemaRecordData(schema, record) }
    }.orEmpty()
    return expressionContextWithRoots(
        data = primaryData,
        schemaRoots = schemas.associateSchemaRoots { schema ->
            recordsBySchemaId[schema.id]?.let { record -> schemaRecordData(schema, record) }.orEmpty()
        },
    )
}

fun referencedExpressionRoots(value: String): Set<String> {
    return TemplateExpressionPattern.findAll(value)
        .flatMap { match -> ExpressionRootPattern.findAll(match.groupValues[1]).map { it.groupValues[1] } }
        .filter { it.isNotBlank() }
        .toSet()
}

fun expressionIdentifier(value: String): String {
    val compact = value.filter { it.isLetterOrDigit() || it == '_' }
    return when {
        compact.isBlank() -> ""
        compact.first().isDigit() -> "_$compact"
        else -> compact
    }
}

fun schemaExpressionAliases(schema: DataSchema): Set<String> {
    return expressionAliases(schema.name) + expressionAliases(schema.id)
}

fun fieldExpressionAliases(field: SchemaField): Set<String> {
    return expressionAliases(field.name) + expressionAliases(field.slug) + expressionAliases(field.id)
}

private val TemplateExpressionPattern = Regex("""\{\{\s*(.*?)\s*}}""")
private val ExpressionRootPattern = Regex("""\b([\p{L}_][\p{L}\p{N}_]*)\s*\.""")

private fun expressionContextWithRoots(
    data: Map<String, Any?>,
    schemaRoots: Map<String, Map<String, Any?>>,
): TemplateExpressionContext {
    return TemplateExpressionContext(
        data = data,
        roots = buildMap {
            put("data", data)
            schemaRoots.forEach { (key, value) -> put(key, value) }
        },
    )
}

private class TemplateExpressionEvaluator(
    private val context: TemplateExpressionContext,
) {
    fun evaluate(expression: String): Any? {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return null

        parseStringLiteral(trimmed)?.let { return it }
        trimmed.toDoubleOrNull()?.let { return it }
        when (trimmed.lowercase()) {
            "true" -> return true
            "false" -> return false
            "null" -> return null
        }

        parseFunctionCall(trimmed)?.let { call ->
            val args = splitArguments(call.arguments).map { evaluate(it) }
            return evaluateFunction(call.name, args)
        }

        return resolveVariable(trimmed)
    }

    private fun evaluateFunction(name: String, args: List<Any?>): Any? {
        return when (name.lowercase()) {
            "default", "ifnull" -> args.firstOrNull().takeUnless(::isBlankExpressionValue) ?: args.getOrNull(1)
            "coalesce" -> args.firstOrNull { !isBlankExpressionValue(it) }
            "if" -> if (args.firstOrNull().isTruthy()) args.getOrNull(1) else args.getOrNull(2)
            "eq" -> compareValues(args.getOrNull(0), args.getOrNull(1)) == 0
            "neq" -> compareValues(args.getOrNull(0), args.getOrNull(1)) != 0
            "gt" -> compareValues(args.getOrNull(0), args.getOrNull(1)) > 0
            "gte" -> compareValues(args.getOrNull(0), args.getOrNull(1)) >= 0
            "lt" -> compareValues(args.getOrNull(0), args.getOrNull(1)) < 0
            "lte" -> compareValues(args.getOrNull(0), args.getOrNull(1)) <= 0
            "and" -> args.all { it.isTruthy() }
            "or" -> args.any { it.isTruthy() }
            "not" -> !args.firstOrNull().isTruthy()
            "upper" -> args.firstOrNull().asText().uppercase()
            "lower" -> args.firstOrNull().asText().lowercase()
            "capitalize" -> args.firstOrNull().asText().replaceFirstChar { it.uppercase() }
            "trim" -> args.firstOrNull().asText().trim()
            "truncate" -> truncate(args.firstOrNull().asText(), args.getOrNull(1).asIntOrNull() ?: 40)
            "round" -> roundNumber(args.firstOrNull().asDoubleOrNull(), args.getOrNull(1).asIntOrNull() ?: 0)
            "number" -> formatNumber(args.firstOrNull().asDoubleOrNull())
            "percent" -> formatPercent(args.firstOrNull().asDoubleOrNull())
            "currency" -> formatCurrency(args.firstOrNull().asDoubleOrNull(), args.getOrNull(1).asText().ifBlank { "USD" })
            "count" -> countValue(args.firstOrNull())
            "first" -> listValue(args.firstOrNull()).firstOrNull()
            "last" -> listValue(args.firstOrNull()).lastOrNull()
            "join" -> listValue(args.firstOrNull()).joinToString(args.getOrNull(1).asText().ifEmpty { ", " })
            else -> null
        }
    }

    private fun resolveVariable(expression: String): Any? {
        val parts = expression.split(".").filter { it.isNotBlank() }
        if (parts.isEmpty()) return null
        var current: Any? = context.roots[parts.first()]
        parts.drop(1).forEach { part ->
            current = when (val value = current) {
                is Map<*, *> -> value[part]
                else -> return null
            }
        }
        return current
    }
}

private data class FunctionCall(
    val name: String,
    val arguments: String,
)

private fun parseFunctionCall(expression: String): FunctionCall? {
    val openIndex = expression.indexOf('(')
    if (openIndex <= 0 || !expression.endsWith(")")) return null
    val name = expression.substring(0, openIndex).trim()
    if (!name.all { it.isLetterOrDigit() || it == '_' }) return null
    val closeIndex = matchingCloseParenIndex(expression, openIndex)
    if (closeIndex != expression.lastIndex) return null
    return FunctionCall(name, expression.substring(openIndex + 1, closeIndex))
}

private fun matchingCloseParenIndex(value: String, openIndex: Int): Int {
    var depth = 0
    var quote: Char? = null
    var escaped = false
    for (index in openIndex until value.length) {
        val char = value[index]
        if (quote != null) {
            if (escaped) {
                escaped = false
                continue
            }
            if (char == '\\') {
                escaped = true
                continue
            }
            if (char == quote) quote = null
            continue
        }
        when (char) {
            '"', '\'' -> quote = char
            '(' -> depth += 1
            ')' -> {
                depth -= 1
                if (depth == 0) return index
            }
        }
    }
    return -1
}

private fun splitArguments(value: String): List<String> {
    if (value.isBlank()) return emptyList()
    val args = mutableListOf<String>()
    var depth = 0
    var start = 0
    var quote: Char? = null
    var escaped = false
    value.forEachIndexed { index, char ->
        if (quote != null) {
            if (escaped) {
                escaped = false
                return@forEachIndexed
            }
            if (char == '\\') {
                escaped = true
                return@forEachIndexed
            }
            if (char == quote) quote = null
            return@forEachIndexed
        }
        when (char) {
            '"', '\'' -> quote = char
            '(' -> depth += 1
            ')' -> depth -= 1
            ',' -> if (depth == 0) {
                args += value.substring(start, index).trim()
                start = index + 1
            }
        }
    }
    args += value.substring(start).trim()
    return args
}

private fun parseStringLiteral(value: String): String? {
    if (value.length < 2) return null
    val quote = value.first()
    if ((quote != '"' && quote != '\'') || value.last() != quote) return null
    return value.substring(1, value.lastIndex)
        .replace("\\$quote", quote.toString())
        .replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace("\\\\", "\\")
}

private fun compareValues(left: Any?, right: Any?): Int {
    val leftNumber = left.asDoubleOrNull()
    val rightNumber = right.asDoubleOrNull()
    if (leftNumber != null && rightNumber != null) {
        return leftNumber.compareTo(rightNumber)
    }
    return left.asText().compareTo(right.asText(), ignoreCase = true)
}

private fun Any?.isTruthy(): Boolean {
    return when (this) {
        null -> false
        is Boolean -> this
        is Number -> this.toDouble() != 0.0
        is String -> this.isNotBlank() && this.lowercase() != "false" && this != "0"
        is Collection<*> -> this.isNotEmpty()
        else -> true
    }
}

private fun isBlankExpressionValue(value: Any?): Boolean {
    return value == null || value is String && value.isBlank()
}

private fun Any?.asText(): String {
    return stringifyExpressionValue(this)
}

private fun Any?.asDoubleOrNull(): Double? {
    return when (this) {
        is Number -> toDouble()
        is String -> trim().toDoubleOrNull()
        else -> null
    }
}

private fun Any?.asIntOrNull(): Int? {
    return asDoubleOrNull()?.toInt()
}

private fun truncate(value: String, maxLength: Int): String {
    if (maxLength <= 0) return ""
    if (value.length <= maxLength) return value
    return value.take((maxLength - 1).coerceAtLeast(0)) + "..."
}

private fun roundNumber(value: Double?, precision: Int): String {
    if (value == null) return ""
    val multiplier = 10.0.pow(precision.coerceAtLeast(0))
    return formatNumber(round(value * multiplier) / multiplier)
}

private fun formatPercent(value: Double?): String {
    if (value == null) return ""
    val normalized = if (abs(value) <= 1.0) value * 100 else value
    return "${formatNumber(normalized)}%"
}

private fun formatCurrency(value: Double?, code: String): String {
    if (value == null) return ""
    val symbol = when (code.uppercase()) {
        "USD" -> "$"
        "EUR" -> "EUR "
        "MAD" -> "MAD "
        "GBP" -> "GBP "
        else -> "${code.uppercase()} "
    }
    return symbol + formatFixed(value, 2)
}

private fun formatNumber(value: Double?): String {
    if (value == null) return ""
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        value.toString().trimEnd('0').trimEnd('.')
    }
}

private fun formatFixed(value: Double, precision: Int): String {
    val multiplier = 10.0.pow(precision)
    val rounded = round(value * multiplier) / multiplier
    val raw = rounded.toString()
    val decimals = raw.substringAfter('.', "")
    return if (decimals.length >= precision) {
        raw
    } else {
        raw + "0".repeat(precision - decimals.length)
    }
}

private fun countValue(value: Any?): Int {
    return when (value) {
        is Collection<*> -> value.size
        is String -> listValue(value).size
        null -> 0
        else -> 1
    }
}

private fun listValue(value: Any?): List<String> {
    return when (value) {
        is Collection<*> -> value.map { it.asText() }
        is String -> value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        null -> emptyList()
        else -> listOf(value.asText())
    }
}

private fun stringifyExpressionValue(value: Any?): String {
    return when (value) {
        null -> ""
        is Double -> formatNumber(value)
        is Float -> formatNumber(value.toDouble())
        is Collection<*> -> value.joinToString(", ") { stringifyExpressionValue(it) }
        else -> value.toString()
    }
}

private fun sampleFieldValue(field: SchemaField): Any? {
    return when (field.type) {
        FieldType.TEXT -> field.name.ifBlank { field.slug }.replaceFirstChar { it.uppercase() }
        FieldType.NUMBER -> 42
        FieldType.DOUBLE -> 199.99
        FieldType.REFERENCE -> field.name.ifBlank { "Reference" }
        FieldType.LIST -> listOf("Alpha", "Beta", "Gamma")
    }
}

private fun sampleSchemaData(schema: DataSchema): Map<String, Any?> {
    return schema.fields.associateFieldValues { field -> sampleFieldValue(field) }
}

private fun schemaRecordData(schema: DataSchema, record: DataRecord): Map<String, Any?> {
    return schema.fields.associateFieldValues { field ->
        recordFieldValue(field, record.values[field.slug] ?: record.values[field.id].orEmpty())
    }
}

private fun List<DataSchema>.associateSchemaRoots(valueForSchema: (DataSchema) -> Map<String, Any?>): Map<String, Map<String, Any?>> {
    return buildMap {
        this@associateSchemaRoots.forEach { schema ->
            val value = valueForSchema(schema)
            schemaExpressionAliases(schema).forEach { alias ->
                if (alias.isNotBlank()) put(alias, value)
            }
        }
    }
}

private fun List<SchemaField>.associateFieldValues(valueForField: (SchemaField) -> Any?): Map<String, Any?> {
    return buildMap {
        this@associateFieldValues.forEach { field ->
            val value = valueForField(field)
            fieldExpressionAliases(field).forEach { alias ->
                if (alias.isNotBlank()) put(alias, value)
            }
        }
    }
}

private fun expressionAliases(value: String): Set<String> {
    val compact = expressionIdentifier(value)
    return setOf(value, compact)
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()
}

private fun recordFieldValue(field: SchemaField, rawValue: String): Any? {
    return when (field.type) {
        FieldType.NUMBER -> rawValue.toLongOrNull() ?: rawValue
        FieldType.DOUBLE -> rawValue.toDoubleOrNull() ?: rawValue
        FieldType.LIST -> rawValue
            .lines()
            .flatMap { line -> line.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
        FieldType.TEXT,
        FieldType.REFERENCE -> rawValue
    }
}
