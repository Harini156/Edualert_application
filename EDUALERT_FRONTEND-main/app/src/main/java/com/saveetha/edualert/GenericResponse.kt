package com.saveetha.edualert

/**
 * Generic response class for API calls that return simple status and message
 * Used for: OTP sending, password reset, and other simple operations
 */
data class GenericResponse(
    val status: String,
    val message: String
)