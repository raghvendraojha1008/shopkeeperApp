──
package com.shopkeeper.ledger.model

data class TrashItem(
    val id: String? = null,
    val original_collection: String = "",
    val original_id: String = "",
    val data: Map<String, Any> = emptyMap(),
    val deleted_at: String = "",
    val deleted_by: String = ""
)
kotlin// ───