package uz.meduza.anime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
