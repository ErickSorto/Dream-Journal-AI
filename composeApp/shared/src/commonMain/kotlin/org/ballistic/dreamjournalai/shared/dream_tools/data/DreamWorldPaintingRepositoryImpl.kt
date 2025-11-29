package org.ballistic.dreamjournalai.shared.dream_tools.data

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.storage
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.ballistic.dreamjournalai.shared.core.Constants
import org.ballistic.dreamjournalai.shared.core.Resource
import org.ballistic.dreamjournalai.shared.core.util.decodeUrlPart
import org.ballistic.dreamjournalai.shared.core.util.downloadImageBytes
import org.ballistic.dreamjournalai.shared.core.util.toGitLiveData
import org.ballistic.dreamjournalai.shared.dream_tools.domain.DreamWorldPaintingRepository
import org.ballistic.dreamjournalai.shared.dream_tools.domain.model.DreamWorldPainting
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime

private val logger = Logger.withTag("DreamWorldPaintingRepo")

class DreamWorldPaintingRepositoryImpl(
    private val db: FirebaseFirestore
) : DreamWorldPaintingRepository {

    private val currentUser get() = Firebase.auth.currentUser

    private fun getCollectionReference() = currentUser?.uid?.let { uid ->
        db.collection(Constants.USERS).document(uid).collection("dream_world_paintings")
    }

    override fun getPaintings(): Flow<List<DreamWorldPainting>> {
        val collectionRef = getCollectionReference() ?: return flowOf(emptyList())

        return collectionRef.snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    try {
                        val painting = document.data<DreamWorldPainting>()
                        painting.copy(id = document.id)
                    } catch (e: Exception) {
                        logger.e(e) { "Error parsing painting document: ${document.id}" }
                        null
                    }
                }
            }
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
    override suspend fun savePainting(painting: DreamWorldPainting): Resource<Unit> {
        return try {
            val collectionRef = getCollectionReference()
                ?: return Resource.Error("User not logged in")

            val docRef = if (painting.id.isBlank()) {
                collectionRef.document
            } else {
                collectionRef.document(painting.id)
            }

            val uid = currentUser?.uid ?: ""
            val paintingId = docRef.id

            suspend fun uploadImageIfNeeded(p: DreamWorldPainting): DreamWorldPainting {
                if (p.imageUrl.isBlank()) return p
                val alreadyStorageUrl = p.imageUrl.startsWith("https://firebasestorage.googleapis.com/")
                if (alreadyStorageUrl) return p

                return withContext(Dispatchers.IO) {
                    val targetPath = "$uid/dream_world_paintings/$paintingId.jpg"
                    val storageRef = Firebase.storage.reference(location = targetPath)

                    val isDataUrl = p.imageUrl.startsWith("data:")
                    val imageBytes = if (isDataUrl) {
                        val commaIdx = p.imageUrl.indexOf(',')
                        val b64 = if (commaIdx >= 0) p.imageUrl.substring(commaIdx + 1) else ""
                        if (b64.isBlank()) ByteArray(0) else Base64.decode(b64)
                    } else {
                        downloadImageBytes(p.imageUrl)
                    }

                    val gitLiveData: Data = imageBytes.toGitLiveData()
                    storageRef.putData(gitLiveData)
                    val downloadUrl = storageRef.getDownloadUrl().toString()
                    val sep = if (downloadUrl.contains('?')) '&' else '?'
                    val version = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    val finalUrl = "$downloadUrl${sep}v=$version"

                    p.copy(imageUrl = finalUrl)
                }
            }

            val paintingToSave = uploadImageIfNeeded(painting.copy(id = paintingId, userId = uid))
            docRef.set(paintingToSave)
            Resource.Success(Unit)
        } catch (e: Exception) {
            logger.e(e) { "Error saving painting" }
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun deletePainting(id: String): Resource<Unit> {
        return try {
            val collectionRef = getCollectionReference()
                ?: return Resource.Error("User not logged in")
            
            val docRef = collectionRef.document(id)
            val snapshot = docRef.get()
            if (snapshot.exists) {
                val painting = snapshot.data<DreamWorldPainting>()
                val imageUrl = painting.imageUrl
                
                if (imageUrl.isNotBlank() && imageUrl.contains("firebasestorage")) {
                    try {
                        val ktorUrl = Url(imageUrl)
                        val pathPortion = ktorUrl.encodedPath.substringAfter("/o/").substringBefore("?")
                        val decodedPath = decodeUrlPart(pathPortion)
                        Firebase.storage.reference(decodedPath).delete()
                        logger.d { "Successfully deleted image from storage at path: $decodedPath" }
                    } catch (e: Exception) {
                        logger.w(e) { "Failed to delete image from storage, but proceeding with Firestore deletion." }
                    }
                }
            }

            docRef.delete()
            logger.d { "Successfully deleted painting document from Firestore with id: $id" }
            Resource.Success(Unit)
        } catch (e: Exception) {
            logger.e(e) { "Error deleting painting" }
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
