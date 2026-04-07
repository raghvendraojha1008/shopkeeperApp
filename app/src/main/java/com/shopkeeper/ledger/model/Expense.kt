──
package com.shopkeeper.ledger.model

data class Expense(
    val id: String? = null,
    val date: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val paid_by: String? = null,
    val payment_mode: String? = null,
    val notes: String? = null,
    val created_at: String = ""
)
kotlin// ───