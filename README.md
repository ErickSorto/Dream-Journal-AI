# Dream-Journal-AI
App allows users to save, edit, and delete dreams and gives them the ability to generate an AI response that interprets their dream. It also stores various details for a dream and beautifully displays all dreams on screen.

---

### Tools and skills used:

- Clean Architecture using MVVM
  - Data Layer
    - Retrofit Interface
  - Domain Layer
    - Models
  - Presentation Layer
    - ViewModel
- Jetpack Navigation Component
- Jetpack Compose
- Tablayouts
- REST APIs
  - Retrofit 2
    - Uses Open AI API to fetch AI responses for dreams
    - @GET and @POST queries 
- Dependency Injection (Dagger Hilt)
- LiveData
  - Livedata Observers
  - Kotlin Flow
- Kotlin coroutines (for synchronous executions)
- Lazy Columns (RecyclerView for Jetpack Compose)
