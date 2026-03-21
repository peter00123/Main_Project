package com.atezhare.utils

// SupabaseClient.kt
// Singleton Supabase client used for Auth operations across the app.
// Used by: LoginViewModel, SignUpViewModel
// Replace the two placeholder strings with your actual values from
// Supabase → Settings → API

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        //project ref
        supabaseUrl = "https://nnxychgfgfzbjfrbltht.supabase.co",
        //anon tocken
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5ueHljaGdmZ2Z6YmpmcmJsdGh0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE5MzAwMTIsImV4cCI6MjA4NzUwNjAxMn0.4T0SGwGpHIFDYpHT9fuENBrZtJh-nNOEVGG0FPxch-c"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
