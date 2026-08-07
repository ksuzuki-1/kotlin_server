package com.ksuzuki

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.*

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
}
