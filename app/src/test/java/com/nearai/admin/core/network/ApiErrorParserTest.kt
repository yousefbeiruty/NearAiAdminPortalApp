package com.nearai.admin.core.network

import com.nearai.admin.core.common.AppError
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiErrorParserTest {

    // --- 422 Pydantic Validation Error Tests ---

    @Test
    fun `parse 422 with single validation error extracts field message`() {
        val body = """{"detail":[{"loc":["body","email"],"msg":"field required","type":"missing"}]}"""
        val exception = createHttpException(422, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertEquals(422, (error as AppError.ServerError).statusCode)
        assertTrue(error.message.contains("field required"))
        assertTrue(error.message.contains("body.email"))
    }

    @Test
    fun `parse 422 with multiple validation errors joins messages`() {
        val body = """{
            "detail":[
                {"loc":["body","email"],"msg":"field required","type":"missing"},
                {"loc":["body","password"],"msg":"field required","type":"missing"}
            ]
        }"""
        val exception = createHttpException(422, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertTrue(error.message.contains("email"))
        assertTrue(error.message.contains("password"))
    }

    @Test
    fun `parse 422 with string detail returns the string`() {
        val body = """{"detail":"Custom validation error message"}"""
        val exception = createHttpException(422, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertEquals("Custom validation error message", error.message)
    }

    @Test
    fun `parse 422 with empty detail array returns generic message`() {
        val body = """{"detail":[]}"""
        val exception = createHttpException(422, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertEquals("Validation failed. Please check your input.", error.message)
    }

    @Test
    fun `parse 422 with invalid JSON returns generic message`() {
        val body = """not valid json"""
        val exception = createHttpException(422, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertEquals("Validation failed. Please check your input.", error.message)
    }

    @Test
    fun `parse 422 with nested loc path extracts full path`() {
        val body = """{"detail":[{"loc":["body","credentials","email"],"msg":"value is not a valid email","type":"value_error"}]}"""
        val exception = createHttpException(422, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertTrue(error.message.contains("body.credentials.email"))
    }

    // --- Other Status Code Tests ---

    @Test
    fun `parse 401 returns UnauthorizedError`() {
        val exception = createHttpException(401, """{"detail":"Not authenticated"}""")

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.UnauthorizedError)
    }

    @Test
    fun `parse 403 returns AuthenticationError`() {
        val exception = createHttpException(403, """{"detail":"Forbidden"}""")

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.AuthenticationError)
    }

    @Test
    fun `parse 404 returns NotFoundError`() {
        val exception = createHttpException(404, """{"detail":"Not found"}""")

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.NotFoundError)
    }

    @Test
    fun `parse 500 extracts detail string`() {
        val body = """{"detail":"Internal server error occurred"}"""
        val exception = createHttpException(500, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertEquals(500, (error as AppError.ServerError).statusCode)
        assertEquals("Internal server error occurred", error.message)
    }

    @Test
    fun `parse 500 with no detail returns generic message`() {
        val exception = createHttpException(500, """{}""")

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertEquals("Server error", error.message)
    }

    @Test
    fun `parse 400 extracts detail string`() {
        val body = """{"detail":"Bad request body"}"""
        val exception = createHttpException(400, body)

        val error = ApiErrorParser.parse(exception)

        assertTrue(error is AppError.ServerError)
        assertEquals(400, (error as AppError.ServerError).statusCode)
        assertEquals("Bad request body", error.message)
    }

    private fun createHttpException(code: Int, body: String): HttpException {
        val responseBody = ResponseBody.create(null, body)
        val response = Response.error<String>(code, responseBody)
        return HttpException(response)
    }
}
