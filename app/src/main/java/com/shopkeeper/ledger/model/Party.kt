──
package com.shopkeeper.ledger.model

data class Party(
    val id: String? = null,
    val name: String = "",
    val role: String = "",
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val state: String? = null,
    val gstin: String? = null,
    val opening_balance: Double = 0.0,
    val balance: Double = 0.0,
    val notes: String? = null,
    val created_at: String = ""
)
kotlin// ───