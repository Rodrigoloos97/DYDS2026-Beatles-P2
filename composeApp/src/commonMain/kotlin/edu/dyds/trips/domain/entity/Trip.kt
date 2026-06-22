package edu.dyds.trips.domain.entity

import java.util.UUID

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val countryCode: String,
    val countryName: String,
    val startDate: String,
    val endDate: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

