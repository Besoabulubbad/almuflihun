package com.abulubad.almoflihon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform