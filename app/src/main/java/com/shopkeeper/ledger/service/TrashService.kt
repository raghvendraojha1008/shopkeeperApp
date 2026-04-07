──
package com.shopkeeper.ledger.service

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant

object TrashService {

    private val db = FirebaseFirestore.getInstance()

    suspend fun moveToTrash(
        uid: String,
        originalCollection: String,
        originalId: String,
        data: Map<String, Any>
    ) {
        val trashData = mapOf(
            "original_collection" to originalCollection,
            "original_id" to originalId,
            "data" to data,
            "deleted_at" to Instant.now().toString(),
            "deleted_by" to uid
        )
        db.collection("users").document(uid)
            .collection("trash")
            .add(trashData)
            .await()
    }
}
kotlin// ───