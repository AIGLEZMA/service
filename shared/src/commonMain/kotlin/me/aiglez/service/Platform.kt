package me.aiglez.service

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform