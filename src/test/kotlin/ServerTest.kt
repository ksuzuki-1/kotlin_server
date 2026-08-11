package com.ksuzuki

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

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

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid priority: urgent", response.bodyAsText())
    }

    @Test
    fun newTasksCanBeAdded() = testApplication {
        configure()

        val response1 = client.post("/tasks") {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.FormUrlEncoded.toString()
            )
            setBody(
                listOf(
                    "name" to "swimming",
                    "description" to "Go to the beach",
                    "priority" to "Low"
                ).formUrlEncode()
            )
        }
        assertEquals(HttpStatusCode.NoContent, response1.status)

        val response2 = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, response2.status)
        val body = response2.bodyAsText()

        assertContains(body, "swimming")
        assertContains(body, "Go to the beach")
    }
}
