──
package com.shopkeeper.ledger.service

import com.google.firebase.firestore.FirebaseFirestore
import com.shopkeeper.ledger.model.AuditLog
import kotlinx.coroutines.tasks.await
import java.time.Instant

object AuditService {

    private val db = FirebaseFirestore.getInstance()

    suspend fun log(
        uid: String,
        action: String,
        collection: String,
        documentId: String,
        diff: Map<String, Any>? = null
    ) {
        val entry = mapOf(
            "action" to action,
            "collection" to collection,
            "document_id" to documentId,
            "changed_by" to uid,
            "diff" to (diff ?: emptyMap<String, Any>()),
            "created_at" to Instant.now().toString()
        )
        db.collection("users").document(uid)
            .collection("audit_logs")
            .add(entry)
            .await()
    }
}
kotlin// ───