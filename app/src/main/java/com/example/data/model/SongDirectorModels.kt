package com.example.data.model

data class SongDirectorPlan(
    val title: String,
    val language: String,
    val genre: String,
    val subgenre: String,
    val mood: List<String>,
    val bpm: Int,
    val durationSeconds: Int,
    val musicalKey: String = "A Minor",
    val instrumentation: List<String>,
    val vocalDelivery: String,
    val structure: List<String>,
    val summaryDescription: String = ""
)

data class LyricSectionItem(
    val sectionName: String, // e.g. "[Intro]", "[Verse 1]", "[Chorus]", "[Verse 2]", "[Bridge]", "[Final Chorus]", "[Outro]"
    val lines: String
)

data class LyricGenerationResult(
    val title: String,
    val language: String,
    val fullLyrics: String,
    val sections: List<LyricSectionItem>
)

data class GenreTemplate(
    val id: String,
    val title: String,
    val titleMs: String,
    val category: String,
    val defaultBpm: Int,
    val defaultKey: String,
    val defaultMood: String,
    val promptEn: String,
    val promptMs: String,
    val instruments: List<String>
)

object StudioGenreCatalog {
    val presets = listOf(
        GenreTemplate(
            id = "malay_rap",
            title = "Malay Emotional Rap",
            titleMs = "Rap Emosi Melayu",
            category = "Hip Hop",
            defaultBpm = 84,
            defaultKey = "A Minor",
            defaultMood = "Reflective & Hopeful",
            promptEn = "Emotional Malay rap song about overcoming depression, struggles in life, and finding inner strength and hope.",
            promptMs = "Lagu rap Melayu tentang depression, hidup susah tetapi akhirnya ada harapan dan kekuatan.",
            instruments = listOf("Melancholic Piano", "808 Sub-Bass", "Boom Bap Drums", "Vinyl Crackle", "Ambient Synth Pad")
        ),
        GenreTemplate(
            id = "malay_pop",
            title = "Modern Malay Pop",
            titleMs = "Pop Moden Melayu",
            category = "Pop",
            defaultBpm = 110,
            defaultKey = "C Major",
            defaultMood = "Catchy & Upbeat",
            promptEn = "Catchy modern Malay pop song with melodic hooks, acoustic guitar strums and energetic vocal chorus.",
            promptMs = "Lagu pop Melayu moden dengan melodi catchy, gitar akustik dan korus yang bertenaga.",
            instruments = listOf("Acoustic Guitar", "Electric Bass", "Punchy Pop Drums", "Brass Stabs", "Synth Pluck")
        ),
        GenreTemplate(
            id = "emotional_ballad",
            title = "Cinematic Ballad",
            titleMs = "Balada Sinematik",
            category = "Ballad",
            defaultBpm = 72,
            defaultKey = "D Minor",
            defaultMood = "Deep & Tearful",
            promptEn = "Deeply moving acoustic piano ballad with orchestral string quartet and heartfelt vocal dynamics.",
            promptMs = "Balada piano emosi yang menyentuh hati dengan gesekan biola orkestra dan vokal syahdu.",
            instruments = listOf("Grand Piano", "Orchestral Strings", "Cello Solo", "Warm Bass", "Subtle Shaker")
        ),
        GenreTemplate(
            id = "lofi_chill",
            title = "Lofi Study Beats",
            titleMs = "Lofi Santai",
            category = "Chill",
            defaultBpm = 76,
            defaultKey = "F Major 7",
            defaultMood = "Relaxing & Nostalgic",
            promptEn = "Chill lofi hip-hop beat with Rhodes electric piano, tape saturation, rainy atmosphere, and laid-back groove.",
            promptMs = "Lofi santai dengan piano elektrik Rhodes, bunyi hujan dan rentak chill.",
            instruments = listOf("Rhodes Piano", "Tape Saturation", "Rain Ambience", "Laidback Drums", "Soft Bass")
        ),
        GenreTemplate(
            id = "synthwave_edm",
            title = "Cyber Synthwave / EDM",
            titleMs = "Synthwave / EDM Siber",
            category = "Electronic",
            defaultBpm = 126,
            defaultKey = "G Minor",
            defaultMood = "Futuristic & Driving",
            promptEn = "High-energy cyberpunk synthwave with analog synthesizer arpeggios, gated reverb drums and retro basslines.",
            promptMs = "Muzik elektronik synthwave bertenaga tinggi dengan arpeggio synth dan rentak padu.",
            instruments = listOf("Analog Synth Leads", "Arpeggiator", "Gated Reverb Drums", "Saw Bass", "Laser FX")
        ),
        GenreTemplate(
            id = "rock_anthem",
            title = "Alternative Rock Anthem",
            titleMs = "Rock Alternatif",
            category = "Rock",
            defaultBpm = 120,
            defaultKey = "E Minor",
            defaultMood = "Passionate & Raw",
            promptEn = "Powerful alternative rock anthem with distorted electric guitars, driving live drum kit, and gritty vocal delivery.",
            promptMs = "Lagu rock alternatif bertenaga dengan gitar elektrik distorsi dan drum live mantap.",
            instruments = listOf("Distorted Electric Guitar", "Overdriven Bass", "Rock Drums", "Acoustic Backing")
        )
    )
}
