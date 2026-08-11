package com.ksuzuki

import com.ksuzuki.model.task.Priority
import com.ksuzuki.model.task.Task
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ServerTest {

    @Test
    fun `test root endpoint`() = testApplication {
        configure()

        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }

    @Test
    fun `test home endpoint`() = testApplication {
        configure()

        val response = client.get("/home")
        assertEquals("html", response.contentType()?.contentSubtype)
        assertContains(response.bodyAsText(), "Ktor Server")
    }

    @Test
    fun `test eat endpoint`() = testApplication {
        configure()

        val response = client.get("/eat?food=eel")
        assertEquals("Eat eel", response.bodyAsText())
    }

    @Test
    fun `test content endpoint`() = testApplication {
        configure()

        val response = client.get("/content/sample.html")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("html", response.contentType()?.contentSubtype)
        assertContains(response.bodyAsText(), "This page is built with:")
    }

    @Test
    fun `test missing content returns 404`() = testApplication {
        configure()

        val response = client.get("/content/not-found.html")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `test tasks with invalid priority returns 400`() = testApplication {
        configure()

        val response = client.get("/tasks?priority=urgent")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("Invalid priority: urgent", response.bodyAsText())
    }

    @Test
    fun `tasks can be requested as JSON`() = testApplication {
        configure()

        val response = client.get("/tasks") {
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ContentType.Application.Json, response.contentType())
    }

    @Test
    fun tasksCanBeFoundByPriority() = testApplication {
        configure()
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.get("/tasks?priority=medium")
        val result = response.body<List<Task>>().firstOrNull()

        assertEquals(HttpStatusCode.OK, response.status)
        assertNotNull(result)
        assertEquals(Priority.MEDIUM, result.priority)
    }

    @Test
    fun newTasksCanBeAdded() = testApplication {
        configure()
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val task = Task("swimming", "Go to the beach", Priority.LOW)
        val response1 = client.post("/tasks") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )

            setBody(task)
        }
        assertEquals(HttpStatusCode.Created, response1.status)

        val response2 = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response2.status)

        val taskNames = response2
            .body<List<Task>>()
            .map { it.name }

        assertContains(taskNames, "swimming")
    }
}
