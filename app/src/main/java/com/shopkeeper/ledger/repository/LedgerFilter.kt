──
package com.shopkeeper.ledger.repository

data class LedgerFilter(
    val type: String? = null,
    val partyName: String? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null
)
kotlin// ───