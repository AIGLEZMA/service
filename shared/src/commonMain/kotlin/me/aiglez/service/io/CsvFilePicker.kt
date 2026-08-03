package me.aiglez.service.io

data class CsvFileSelection(
    val fileName: String,
    val content: String,
)

expect suspend fun pickCsvFile(): CsvFileSelection?
