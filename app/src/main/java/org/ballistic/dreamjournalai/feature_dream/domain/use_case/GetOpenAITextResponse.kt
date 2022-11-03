package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ballistic.dreamjournalai.core.Resource
import org.ballistic.dreamjournalai.feature_dream.data.remote.dto.toCompletion
import org.ballistic.dreamjournalai.feature_dream.domain.model.Prompt
import org.ballistic.dreamjournalai.feature_dream.domain.repository.OpenAIRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetOpenAITextResponse @Inject constructor(
    private val repository: OpenAIRepository,

    ) {

    operator fun invoke(prompt: Prompt): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading<String>())
            val response = repository.getCompletion(prompt).toCompletion().choices[0].text
            emit(Resource.Success<String>(response))
        } catch(e: HttpException) {
            emit(Resource.Error<String>(e.localizedMessage ?: "An unexpected error occured"))
        } catch(e: IOException) {
            emit(Resource.Error<String>("Couldn't reach server. Check your internet connection."))
        }

    }
}

