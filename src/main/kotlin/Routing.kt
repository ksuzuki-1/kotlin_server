package com.ksuzuki

import com.ksuzuki.model.task.Priority
import com.ksuzuki.model.task.Task
import com.ksuzuki.model.task.TaskRepository
import com.ksuzuki.model.task.tasksAsTable
import io.ktor.http.ContentType
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

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

        route("/tasks") {
            get {
                val priorityParam = call.request.queryParameters["priority"]
                val tasks: List<Task> = if (priorityParam != null) {
                    val priority = runCatching {
                        Priority.valueOf(priorityParam.uppercase())
                    }.getOrElse {
                        call.respondText(
                            text = "Invalid priority: $priorityParam",
                            status = HttpStatusCode.BadRequest
                        )
                        return@get
                    }
                    TaskRepository.tasksByPriority(priority)
                } else {
                    TaskRepository.allTasks()
                }
                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = tasks.tasksAsTable()
                )
            }

            post {
                val formContent = call.receiveParameters()
                val params = Triple(
                    formContent["name"] ?: "",
                    formContent["description"] ?: "",
                    formContent["priority"] ?: ""
                )

                if (params.toList().any { it.isEmpty() }) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                try {
                    val priority = Priority.valueOf(params.third.uppercase())
                    TaskRepository.addTask(
                        Task(
                            params.first,
                            params.second,
                            priority
                        )
                    )

                    call.respond(HttpStatusCode.NoContent)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (e: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }


        staticResources("/task-ui", "task-ui")

        staticResources("/content", "mycontent")
    }
}
