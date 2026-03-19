package com.example.practice.archives.wifi


import fi.iki.elonen.NanoHTTPD

class FileServer(
    port: Int,
    private val sessionToken: String
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {

        val token = session.headers["token"]
        if (token != sessionToken) {
            return newFixedLengthResponse(
                Response.Status.UNAUTHORIZED,
                MIME_PLAINTEXT,
                "Invalid session"
            )
        }

        if (session.method == Method.POST) {
            val files = HashMap<String, String>()
            session.parseBody(files)

            val tempFilePath = files["file"]
            return newFixedLengthResponse("File received")
        }

        return newFixedLengthResponse("Receiver active")
    }
}