──
package com.shopkeeper.ledger.model

data class OfflineCommand(
    val id: String = "",
    val action: String = "",
    val collection: String = "",
    val document_id: String? = null,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: String = ""
)
kotlin// ───