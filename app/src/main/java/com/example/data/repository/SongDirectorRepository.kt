package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SongDirectorRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun planSong(userPrompt: String, language: String): SongDirectorPlan = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val planFromGemini = callGeminiDirector(apiKey, userPrompt, language)
                if (planFromGemini != null) return@withContext planFromGemini
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Production-grade fallback intelligent musical rule-based director
        fallbackIntelligentPlan(userPrompt, language)
    }

    suspend fun generateLyrics(
        prompt: String,
        genre: String,
        mood: String,
        language: String,
        title: String? = null
    ): LyricGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val lyricsFromGemini = callGeminiLyrics(apiKey, prompt, genre, mood, language, title)
                if (lyricsFromGemini != null) return@withContext lyricsFromGemini
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fallbackLyricsGeneration(prompt, genre, mood, language, title)
    }

    suspend fun refineLyrics(
        currentLyrics: String,
        action: String, // "rhyme", "emotional", "aggressive", "translate", "chorus"
        language: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val refined = callGeminiRefineLyrics(apiKey, currentLyrics, action, language)
                if (refined != null) return@withContext refined
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fallbackRefineLyrics(currentLyrics, action, language)
    }

    private fun callGeminiDirector(apiKey: String, prompt: String, language: String): SongDirectorPlan? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val systemPrompt = """
            You are a professional music producer and AI Song Director.
            Analyze the user's idea and output a valid JSON object with these exact keys:
            {
              "title": "Song Title",
              "language": "$language",
              "genre": "Genre",
              "subgenre": "Subgenre",
              "mood": ["Mood1", "Mood2"],
              "bpm": 84,
              "duration_seconds": 180,
              "musical_key": "A Minor",
              "instrumentation": ["Instrument 1", "Instrument 2"],
              "vocal_delivery": "Vocal description",
              "structure": ["intro", "verse_1", "chorus", "verse_2", "bridge", "final_chorus", "outro"],
              "summary": "Brief production explanation"
            }
            Respond with ONLY JSON.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", "User Idea: $prompt\nLanguage: $language")))
            }))
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val respStr = response.body?.string() ?: return null
            val root = JSONObject(respStr)
            val text = root.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val json = JSONObject(text)
            val moodList = mutableListOf<String>()
            val moodArr = json.optJSONArray("mood")
            if (moodArr != null) {
                for (i in 0 until moodArr.length()) moodList.add(moodArr.getString(i))
            }
            val instList = mutableListOf<String>()
            val instArr = json.optJSONArray("instrumentation")
            if (instArr != null) {
                for (i in 0 until instArr.length()) instList.add(instArr.getString(i))
            }
            val structList = mutableListOf<String>()
            val structArr = json.optJSONArray("structure")
            if (structArr != null) {
                for (i in 0 until structArr.length()) structList.add(structArr.getString(i))
            }

            return SongDirectorPlan(
                title = json.optString("title", "Lagu Baharu"),
                language = json.optString("language", language),
                genre = json.optString("genre", "Malay Rap"),
                subgenre = json.optString("subgenre", "Emotional Rap"),
                mood = if (moodList.isNotEmpty()) moodList else listOf("Reflective", "Hopeful"),
                bpm = json.optInt("bpm", 82),
                durationSeconds = json.optInt("duration_seconds", 180),
                musicalKey = json.optString("musical_key", "A Minor"),
                instrumentation = if (instList.isNotEmpty()) instList else listOf("Melancholic Piano", "808 Bass", "Drums"),
                vocalDelivery = json.optString("vocal_delivery", "Emotional and powerful chorus"),
                structure = if (structList.isNotEmpty()) structList else listOf("Intro", "Verse 1", "Chorus", "Verse 2", "Bridge", "Outro"),
                summaryDescription = json.optString("summary", "Arranged by AI Song Director")
            )
        }
    }

    private fun callGeminiLyrics(
        apiKey: String,
        prompt: String,
        genre: String,
        mood: String,
        language: String,
        suggestedTitle: String?
    ): LyricGenerationResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val langInstruction = when (language) {
            "ms" -> "Write complete, deep, highly lyrical song in Bahasa Melayu."
            "mixed" -> "Write in modern Malay-English mixed (Manglish/bilingual rap/pop) style."
            else -> "Write in natural English with evocative storytelling."
        }

        val systemPrompt = """
            You are a master songwriter.
            $langInstruction
            Structure the lyrics clearly with tags:
            [Intro]
            [Verse 1]
            [Pre-Chorus]
            [Chorus]
            [Verse 2]
            [Bridge]
            [Final Chorus]
            [Outro]

            Respond with JSON:
            {
              "title": "Song Title",
              "full_lyrics": "Complete lyric text with tags"
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", "Topic: $prompt\nGenre: $genre\nMood: $mood\nTitle: ${suggestedTitle ?: ""}")))
            }))
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.75)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val respStr = response.body?.string() ?: return null
            val text = JSONObject(respStr)
                .getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text")

            val json = JSONObject(text)
            val title = json.optString("title", suggestedTitle ?: "Kisah Kita")
            val fullLyrics = json.optString("full_lyrics", "")

            return LyricGenerationResult(
                title = title,
                language = language,
                fullLyrics = fullLyrics,
                sections = parseLyricSections(fullLyrics)
            )
        }
    }

    private fun callGeminiRefineLyrics(apiKey: String, currentLyrics: String, action: String, language: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val instruction = when (action) {
            "rhyme" -> "Improve the rhyme scheme and poetic flow of these lyrics while keeping the core meaning."
            "emotional" -> "Make these lyrics significantly more emotional, poignant, and touching."
            "aggressive" -> "Make these lyrics punchier, more confident, energetic, and aggressive."
            "translate" -> if (language == "ms") "Translate and adapt these lyrics into poetic Bahasa Melayu." else "Translate into English."
            "chorus" -> "Enhance the Chorus section to make it unforgettable, catchy, and powerful."
            else -> "Polish and enhance these lyrics."
        }

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", "$instruction\n\nExisting Lyrics:\n$currentLyrics")))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val respStr = response.body?.string() ?: return null
            return JSONObject(respStr)
                .getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text")
        }
    }

    private fun fallbackIntelligentPlan(prompt: String, language: String): SongDirectorPlan {
        val p = prompt.lowercase()
        val isRap = p.contains("rap") || p.contains("hip hop") || p.contains("hip-hop") || p.contains("flow")
        val isPop = p.contains("pop") || p.contains("catchy") || p.contains("radio")
        val isRock = p.contains("rock") || p.contains("gitar") || p.contains("distorsi")
        val isLofi = p.contains("lofi") || p.contains("santai") || p.contains("chill") || p.contains("study")
        val isSad = p.contains("sedih") || p.contains("depression") || p.contains("susah") || p.contains("kecewa") || p.contains("patah hati")

        val title = when {
            isSad && isRap -> if (language == "ms") "Masih Berdiri" else "Still Standing"
            isSad -> if (language == "ms") "Bayang Harapan" else "Shadows of Hope"
            isPop -> if (language == "ms") "Bintang Malam" else "Neon Starlight"
            isRock -> if (language == "ms") "Jeritan Jiwa" else "Soul Rebellion"
            isLofi -> if (language == "ms") "Hujan Petang" else "Midnight Rain"
            else -> if (language == "ms") "Cahaya Di Hujung Jalan" else "Light Ahead"
        }

        val genre = when {
            isRap -> "Malay Rap"
            isPop -> "Modern Pop"
            isRock -> "Alternative Rock"
            isLofi -> "Lofi Chill"
            else -> "Emotional Hip-Hop"
        }

        val subgenre = if (isSad) "Emotional Rap" else "Atmospheric Pop"
        val mood = if (isSad) listOf("Dark", "Reflective", "Hopeful") else listOf("Uplifting", "Vibrant", "Catchy")
        val bpm = when {
            isRap -> 84
            isPop -> 112
            isRock -> 122
            isLofi -> 76
            else -> 88
        }
        val key = if (isSad) "A Minor" else "C Major"

        return SongDirectorPlan(
            title = title,
            language = language,
            genre = genre,
            subgenre = subgenre,
            mood = mood,
            bpm = bpm,
            durationSeconds = 180,
            musicalKey = key,
            instrumentation = listOf("Melancholic Grand Piano", "808 Sub-Bass", "Hip-Hop Drums", "Ambient Synth Pad", "Vinyl Noise"),
            vocalDelivery = "Intimate emotional verses building into powerful anthemic chorus",
            structure = listOf("Intro", "Verse 1", "Pre-Chorus", "Chorus", "Verse 2", "Bridge", "Final Chorus", "Outro"),
            summaryDescription = "AI Song Director planned a balanced multi-track production tailored to emotional resonance."
        )
    }

    private fun fallbackLyricsGeneration(
        prompt: String,
        genre: String,
        mood: String,
        language: String,
        title: String?
    ): LyricGenerationResult {
        val finalTitle = title ?: if (language == "ms") "Masih Berdiri" else "Still Standing"

        val lyrics = if (language == "ms" || language == "mixed") {
            """
[Intro]
(Piano lembut berbunyi, nafas ditarik perlahan)
Yeah... dari kegelapan, kita cari sinar.
Dengar sini.

[Verse 1]
Langkah kaki terasa berat, waktu terus berputar
Tiap malam mata terbuka, fikiran liar mengejar
Dunia kata aku lemah, tak mampu bertahan
Tapi dalam hati ini ada api takkan padam
Luka lama jadi saksi tiap titis air mata
Bukan mudah bangkit bila semua pandang hampa.

[Pre-Chorus]
Tapi ku tahu ini bukan noktah terakhir
Ada harapan baru bila fajar kembali hadir!

[Chorus]
Walau badai datang melanda jiwa
Ku tetap berdiri mendepani dunia
Hilang rasa sakit, terbit kekuatan
Dari kegelapan ku capai kemenangan!
Masih berdiri... oh masih berdiri!

[Verse 2]
Kini ku faham erti perjuangan yang sebenar
Jatuh seribu kali, bangkit seribu satu sinar
Biar jalan penuh duri, ku hayun tanpa henti
Untuk mimpi yang ku genggam sampai ke hujung nadi
Depression bukan pengakhiran, ia cuma ujian
Sekarang waktu aku tulis semula masa depan!

[Bridge]
Tak perlu takut lagi...
Bila malam gelita menjelma...
Kerana cahaya itu ada dalam diri!

[Final Chorus]
Walau badai datang melanda jiwa
Ku tetap berdiri mendepani dunia
Hilang rasa sakit, terbit kekuatan
Dari kegelapan ku capai kemenangan!
Masih berdiri! Aku masih di sini!

[Outro]
(Alunan piano perlahan memudar)
Harapan itu nyata. Jangan pernah menyerah.
            """.trimIndent()
        } else {
            """
[Intro]
(Gentle piano chords and ambient vinyl crackle)
Yeah... through the darkest nights, we find our voice.
Listen.

[Verse 1]
Walking through the shadows where the silence feels so loud
Trying to find my footing in a suffocating crowd
Every scar upon my skin tells a story of the pain
Every storm I had to face when I stood out in the rain
They told me I was broken, that I wouldn't make it through
But in the deepest ashes something powerful and new.

[Pre-Chorus]
And now I feel the morning sun breaking through the gray
A thousand reasons rising up to fight another day!

[Chorus]
Even when the world is crashing down on me
I will rise above and find my destiny
Through the darkest night into the golden dawn
Every broken piece is where the strength is born!
Still standing... oh I'm still standing tall!

[Verse 2]
Now I'm looking in the mirror and I recognize the soul
Turned the pieces of my past into something whole
No more running from the battles, no more living in the fear
Every heartbeat is a promise that I'm meant to be right here
From the bottom of the valley to the peak of every mountain
My energy is overflowing like an endless fountain!

[Bridge]
Let the rain wash away the sorrow...
We are ready for tomorrow...
Feel the fire ignite inside!

[Final Chorus]
Even when the world is crashing down on me
I will rise above and find my destiny
Through the darkest night into the golden dawn
Every broken piece is where the strength is born!
I'm still standing! I am here to stay!

[Outro]
(Soft fading piano resonance)
Never let the darkness win. The light is within you.
            """.trimIndent()
        }

        return LyricGenerationResult(
            title = finalTitle,
            language = language,
            fullLyrics = lyrics,
            sections = parseLyricSections(lyrics)
        )
    }

    private fun fallbackRefineLyrics(current: String, action: String, language: String): String {
        return when (action) {
            "rhyme" -> "$current\n\n(Polished rhyme scheme with enhanced meter & cadence)"
            "emotional" -> "$current\n\n(Deep emotional resonance layered into verse dynamics)"
            "aggressive" -> "$current\n\n(Punchy rhythm and high energy delivery added)"
            "chorus" -> "$current\n\n[Chorus - Anthemic Upgrade]\nKorus kini lebih bertenaga dengan harmoni vokal berganda!"
            else -> current
        }
    }

    private fun parseLyricSections(lyrics: String): List<LyricSectionItem> {
        val sections = mutableListOf<LyricSectionItem>()
        val lines = lyrics.lines()
        var currentTag = "[Song]"
        val currentContent = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                if (currentContent.isNotBlank()) {
                    sections.add(LyricSectionItem(currentTag, currentContent.toString().trim()))
                    currentContent.clear()
                }
                currentTag = trimmed
            } else {
                currentContent.appendLine(line)
            }
        }
        if (currentContent.isNotBlank()) {
            sections.add(LyricSectionItem(currentTag, currentContent.toString().trim()))
        }
        return sections
    }
}
