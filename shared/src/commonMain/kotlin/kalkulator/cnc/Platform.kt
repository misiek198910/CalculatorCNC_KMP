package kalkulator.cnc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform