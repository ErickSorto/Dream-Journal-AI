package org.ballistic.dreamjournalai.feature_dream.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.ballistic.dreamjournalai.core.Constants.USERS
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.domain.model.Dream
import org.ballistic.dreamjournalai.feature_dream.domain.model.InvalidDreamException
import org.ballistic.dreamjournalai.feature_dream.domain.repository.DreamRepository
import java.net.URL
import java.util.*
import javax.inject.Singleton

@Singleton
class DreamRepositoryImpl(
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore
) : DreamRepository {


    private val dreamsCollection = getCollectionReferenceForDreams()


    override fun getDreams(): Flow<List<Dream>> {
        return callbackFlow {
            val registration = getCollectionReferenceForDreams()?.addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    val dreams = querySnapshot.documents.mapNotNull { document ->
                        val data = document.data
                        if (data != null) {
                            Dream(
                                title = data["title"] as String,
                                content = data["content"] as String,
                                timestamp = data["timestamp"] as String,
                                AIResponse = data["airesponse"] as String,
                                isFavorite = data["favorite"] as Boolean,
                                isLucid = data["lucid"] as Boolean,
                                isNightmare = data["nightmare"] as Boolean,
                                isRecurring = data["recurring"] as Boolean,
                                falseAwakening = data["falseAwakening"] as Boolean? ?: false,
                                lucidityRating = (data["lucidityRating"] as Long?)?.toInt() ?: 1,
                                moodRating = (data["moodRating"] as Long?)?.toInt() ?: 1,
                                vividnessRating = (data["vividnessRating"] as Long?)?.toInt() ?: 1,
                                timeOfDay = data["timeOfDay"] as String? ?: "",
                                backgroundImage = (data["backgroundImage"] as Long?)?.toInt() ?: 0,
                                generatedImage = data["generatedImage"] as String?,
                                generatedDetails = data["generatedDetails"] as String? ?: "",
                                id = document.id
                            )
                        } else {
                            null
                        }
                    }
                    trySend(dreams).isSuccess
                }
            }
            awaitClose { registration?.remove() }
        }
    }


    override suspend fun getDream(id: String): Resource<Dream> {
        return try {
            val document = getCollectionReferenceForDreams()?.document(id)?.get()?.await()
            if (document?.exists() == true) {
                val data = document.data
                if (data != null) {
                    val dream = Dream(
                        title = data["title"] as String,
                        content = data["content"] as String,
                        timestamp = data["timestamp"] as String,
                        AIResponse = data["airesponse"] as String,
                        isFavorite = data["favorite"] as Boolean,
                        isLucid = data["lucid"] as Boolean,
                        isNightmare = data["nightmare"] as Boolean,
                        isRecurring = data["recurring"] as Boolean,
                        falseAwakening = data["falseAwakening"] as Boolean? ?: false,
                        lucidityRating = (data["lucidityRating"] as Long?)?.toInt() ?: 1,
                        moodRating = (data["moodRating"] as Long?)?.toInt() ?: 1,
                        vividnessRating = (data["vividnessRating"] as Long?)?.toInt() ?: 1,
                        timeOfDay = data["timeOfDay"] as String? ?: "",
                        backgroundImage = (data["backgroundImage"] as Long?)?.toInt() ?: 0,
                        generatedImage = data["generatedImage"] as String?,
                        generatedDetails = data["generatedDetails"] as String? ?: "",
                        id = document.id
                    )
                    Resource.Success(dream)
                } else {
                    Resource.Error("Error getting dream data")
                }
            } else {
                Resource.Error("Dream not found")
            }
        } catch (e: Exception) {
            Resource.Error("Error fetching dream")
        }
    }


    override suspend fun insertDream(dream: Dream): Resource<Unit> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: throw InvalidDreamException("Cannot insert dream, no current user found")
            var updatedDream = dream
            CoroutineScope(Dispatchers.IO).launch {
                if (updatedDream.id == null) {
                    val newDreamRef = getCollectionReferenceForDreams()?.document()
                    updatedDream = updatedDream.copy(id = newDreamRef?.id)
                }
                if (dream.generatedImage != null && !dream.generatedImage.startsWith("gs://")) {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

                    val imageUrl = URL(dream.generatedImage)
                    val imageBytes = imageUrl.readBytes()
                    val uploadTask = imageRef.putBytes(imageBytes)
                    uploadTask.await()
                    val downloadUrl = imageRef.downloadUrl.await()
                    updatedDream = updatedDream.copy(generatedImage = downloadUrl.toString())
                }
                // Save the dream to Firestore
                getCollectionReferenceForDreams()?.add(updatedDream)?.await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error inserting dream: ${e.message}")
        }
    }


    override suspend fun deleteDream(id: String): Resource<Unit> {
        return try {
            val dreamRef = db.collection("dreams").document(id)
            dreamRef.delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error deleting dream")
        }
    }

    private fun getCollectionReferenceForDreams(): CollectionReference? {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid?.let {
            db.collection(USERS).document(it).collection("my_dreams")
        }
    }
}