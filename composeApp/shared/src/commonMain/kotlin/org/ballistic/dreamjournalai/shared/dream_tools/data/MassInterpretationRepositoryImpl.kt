package org.ballistic.dreamjournalai.shared.dream_tools.data


import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.ballistic.dreamjournalai.shared.core.Constants
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.dream_tools.domain.MassInterpretationRepository
import org.ballistic.dreamjournalai.shared.dream_tools.domain.model.MassInterpretation

private val readLogger = Logger.withTag("DJAI/Reads/MassInterpretations")

class MassInterpretationRepositoryImpl(
    private val db: FirebaseFirestore
) : MassInterpretationRepository {
    // 1) In GitLive, retrieve the current user from `Firebase.auth`
    private val currentUser get() = Firebase.auth.currentUser

    // 2) Build the collection reference: "USERS/{uid}/my_mass_interpretations"
    private fun getCollectionReferenceForMassInterpretations() = currentUser?.uid?.let { uid ->
        db.collection(Constants.USERS).document(uid).collection("my_mass_interpretations")
    }

    // 3) Add (or update) a MassInterpretation in Firestore
    override suspend fun addInterpretation(massInterpretation: MassInterpretation): Resource<Unit> {
        return try {
            val collectionRef = getCollectionReferenceForMassInterpretations()
                ?: return Resource.Error("User is not logged in or collection is null")

            // If massInterpretation.id is empty => create a new doc ref; otherwise use existing doc ref
            val docRef = if (massInterpretation.id.isNullOrEmpty()) {
                // doc() with no arg auto-generates an ID
                collectionRef.document
            } else {
                collectionRef.document(massInterpretation.id)
            }

            // Ensure the local object’s "id" matches docRef.id
            val updatedInterpretation = massInterpretation.copy(id = docRef.id)

            // In GitLive, set(...) is already a suspend function
            readLogger.d { "Log.d(\"DJAI/Reads/MassInterpretations\"){ addInterpretation(id=${updatedInterpretation.id}) – write operation (no read) }" }
            docRef.set(updatedInterpretation)

            Resource.Success(Unit)

        } catch (e: FirebaseFirestoreException) {
            Resource.Error(e.message ?: "Unknown error occurred")
        } catch (e: Exception) {
            // If you want to catch all exceptions
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getInterpretations(): Flow<List<MassInterpretation>> {
        val collectionRef = getCollectionReferenceForMassInterpretations()
            ?: return flowOf(emptyList())

        var emissions = 0
        return collectionRef.snapshots()
            .onStart { readLogger.d { "Log.d(\"DJAI/Reads/MassInterpretations\"){ Subscribing to interpretations snapshots for uid=${currentUser?.uid} }" } }
            .onEach { qs ->
                emissions += 1
                readLogger.d { "Log.d(\"DJAI/Reads/MassInterpretations\"){ Snapshot #$emissions received, documents=${qs.documents.size} }" }
            }
            .onCompletion { cause ->
                if (cause == null) {
                    readLogger.d { "Log.d(\"DJAI/Reads/MassInterpretations\"){ Unsubscribed after $emissions emissions }" }
                } else {
                    readLogger.d { "Log.d(\"DJAI/Reads/MassInterpretations\"){ Completed with error: ${cause.message} after $emissions emissions }" }
                }
            }
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    try {
                        // Use direct deserialization to MassInterpretation since Map<String, Any> is not serializable
                        val massInterpretation = document.data<MassInterpretation>()
                        // Ensure the ID is set from the document ID
                        massInterpretation.copy(id = document.id)
                    } catch (e: Exception) {
                        readLogger.e { "Error parsing document ${document.id}: ${e.message}" }
                        null
                    }
                }.sortedByDescending { it.date } // Sort by date descending (latest first)
            }
    }

    override suspend fun removeInterpretation(massInterpretation: MassInterpretation): Resource<Unit> {
        return try {
            val collectionReference = getCollectionReferenceForMassInterpretations()
            if (massInterpretation.id.isNullOrEmpty()) {
                return Resource.Error("Cannot remove interpretation without an ID")
            }
            readLogger.d { "Log.d(\"DJAI/Reads/MassInterpretations\"){ removeInterpretation(id=${massInterpretation.id}) – write operation (no read) }" }
            collectionReference?.document(massInterpretation.id)?.delete()
            Resource.Success()
        } catch (e: FirebaseFirestoreException) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }
}
