package com.example.shared

object ConfigHandler {
    lateinit var API_ENDPOINT: String
    lateinit var ENVIRONMENT: String

    fun initialize(apiEndpoint: String, environment: String) {
        API_ENDPOINT = apiEndpoint
        ENVIRONMENT = environment
    }
}
