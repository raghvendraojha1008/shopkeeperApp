──
package com.shopkeeper.ledger.model

data class Vehicle(
    val id: String? = null,
    val vehicle_number: String = "",
    val vehicle_type: String? = null,
    val driver_name: String? = null,
    val driver_phone: String? = null,
    val notes: String? = null,
    val created_at: String = ""
)
kotlin// ───