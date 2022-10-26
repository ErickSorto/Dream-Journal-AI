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

    suspend operator fun invoke(prompt: Prompt): String {
        return repository.getCompletion(
            prompt
        ).toCompletion().choices[0].text
    }
}
