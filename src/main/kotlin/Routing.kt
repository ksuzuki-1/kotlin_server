package com.ksuzuki

import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(CallLogging)

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        get("/home") {
            val text = "<h1>Ktor Server</h1>"
            call.respondText(text, ContentType.parse("text/html"))
        }
        get("/eat") {
            val param = call.request.queryParameters["food"]
            call.respondText { "Eat $param" }
        }

        staticResources("/content", "mycontent")
    }
}
