package com.workforcex.shared_employer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
