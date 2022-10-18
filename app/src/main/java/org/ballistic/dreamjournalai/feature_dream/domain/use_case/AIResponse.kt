package org.ballistic.dreamjournalai.feature_dream.domain.use_case

import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONArray
import org.json.JSONObject

class AIResponse() {


    private lateinit var volleyRequestQueue: RequestQueue
    val demoConversation: String =
        "The following is a conversation between the AI and a user. The AI acts as a dream interpreter trying to find meaning in the dream and explains it to the user, if the user begins to take the topic away from dreams, the ai cleverly brings the topic of dreams back\n" +
                "User: Hello!!\n" +
                "AI: Hello, do you have a dream you want to talk about? \n" +
                "User: I had a dream that I gave birth to a pikachu.\n" +

                "\n\n###\n\n" // add more examples with this as pad token

    private fun requestResponse(text: String, engine: String) {

        val apiKey = org.ballistic.dreamjournalai.BuildConfig.API_KEY
        //sends request to OpenAI
        val modelType = "OpenAI"
        val json = JSONObject()

        json.put("prompt", demoConversation + text)
        json.put("temperature", 0.7)
        json.put("max_tokens", 256)
        json.put("presence_penalty", 0)
        json.put("frequency_penalty", 0)


        val url = "https://api.openai.com/v1/engines/$engine/completions"


        val req = object : JsonObjectRequest(
            Request.Method.POST, url, json,
            Response.Listener { response ->
                saveResponse(response)
            },
            Response.ErrorListener {
                //   error -> conveyError(error)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()

                val api_key = apiKey
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "Bearer $api_key"
                return headers
            }
        }
        volleyRequestQueue.add(req)
    }

    //save response to room database
    private fun saveResponse(response: JSONObject) {
        getTextFromResponseOpenAI(response)
        //add response to room database
    }


    //turn jsonObject to string
    fun getTextFromResponseOpenAI(json: JSONObject): String {
        val arr: JSONArray = json.getJSONArray("choices")
        val res = arr[0] as JSONObject
        var text = res.getString("text")
        Log.d("AI convo", res.toString())

        text = text.trim()
        return text
    }

}