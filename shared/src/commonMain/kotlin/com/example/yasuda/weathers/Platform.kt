package com.example.yasuda.weathers

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform