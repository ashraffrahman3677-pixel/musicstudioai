package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AIMusicApp
import com.example.data.model.*
import com.example.data.repository.GenerationUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class StudioTab {
    CREATE,
    LIBRARY,
    MY_VOICE,
    PLAYER,
    ADMIN
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AIMusicApp
    val musicRepo = app.musicRepository
    val voiceRepo = app.voiceProfileRepository
    val directorRepo = app.songDirectorRepository
    val authRepo = app.authRepository
    val generationRepo = app.generationRepository
    val player = app.playerManager
    val recorder = app.voiceRecorderHelper

    // Navigation
    private val _currentTab = MutableStateFlow(StudioTab.CREATE)
    val currentTab: StateFlow<StudioTab> = _currentTab.asStateFlow()

    // Composer State
    private val _composerMode = MutableStateFlow("SIMPLE") // "SIMPLE" or "ADVANCED"
    val composerMode: StateFlow<String> = _composerMode.asStateFlow()

    private val _promptText = MutableStateFlow("Buat lagu rap Melayu tentang depression, hidup susah tetapi akhirnya ada harapan.")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _songTitle = MutableStateFlow("Masih Berdiri")
    val songTitle: StateFlow<String> = _songTitle.asStateFlow()

    private val _lyricsText = MutableStateFlow("")
    val lyricsText: StateFlow<String> = _lyricsText.asStateFlow()

    private val _songLanguage = MutableStateFlow("ms") // "ms", "en", "mixed"
    val songLanguage: StateFlow<String> = _songLanguage.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Malay Rap")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _selectedSubgenre = MutableStateFlow("Emotional Rap")
    val selectedSubgenre: StateFlow<String> = _selectedSubgenre.asStateFlow()

    private val _selectedMood = MutableStateFlow("Reflective & Hopeful")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    private val _selectedBpm = MutableStateFlow(84)
    val selectedBpm: StateFlow<Int> = _selectedBpm.asStateFlow()

    private val _selectedKey = MutableStateFlow("A Minor")
    val selectedKey: StateFlow<String> = _selectedKey.asStateFlow()

    private val _selectedDuration = MutableStateFlow(180)
    val selectedDuration: StateFlow<Int> = _selectedDuration.asStateFlow()

    private val _vocalType = MutableStateFlow("AI_VOCAL") // "AI_VOCAL", "USER_VOICE", "INSTRUMENTAL"
    val vocalType: StateFlow<String> = _vocalType.asStateFlow()

    private val _selectedVoiceProfile = MutableStateFlow<VoiceProfileEntity?>(null)
    val selectedVoiceProfile: StateFlow<VoiceProfileEntity?> = _selectedVoiceProfile.asStateFlow()

    private val _isPlanning = MutableStateFlow(false)
    val isPlanning: StateFlow<Boolean> = _isPlanning.asStateFlow()

    private val _isWritingLyrics = MutableStateFlow(false)
    val isWritingLyrics: StateFlow<Boolean> = _isWritingLyrics.asStateFlow()

    private val _plannedDirectorOutput = MutableStateFlow<SongDirectorPlan?>(null)
    val plannedDirectorOutput: StateFlow<SongDirectorPlan?> = _plannedDirectorOutput.asStateFlow()

    // Library & Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _libraryFilter = MutableStateFlow("ALL") // "ALL", "FAVORITES", "MY_VOICE", "INSTRUMENTAL"
    val libraryFilter: StateFlow<String> = _libraryFilter.asStateFlow()

    val songsList: StateFlow<List<SongEntity>> = combine(
        musicRepo.allSongs,
        _searchQuery,
        _libraryFilter
    ) { all, query, filter ->
        all.filter { song ->
            val matchQuery = query.isBlank() ||
                    song.title.contains(query, ignoreCase = true) ||
                    song.genre.contains(query, ignoreCase = true) ||
                    song.lyrics.contains(query, ignoreCase = true)

            val matchFilter = when (filter) {
                "FAVORITES" -> song.isFavorite
                "MY_VOICE" -> song.vocalType == "USER_VOICE"
                "INSTRUMENTAL" -> song.isInstrumental || song.vocalType == "INSTRUMENTAL"
                else -> true
            }
            matchQuery && matchFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceProfiles: StateFlow<List<VoiceProfileEntity>> = voiceRepo.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generationState = generationRepo.generationState

    // Voice Enrollment State
    private val _enrollmentStep = MutableStateFlow(1) // 1: Info & Script, 2: Record, 3: Review & Consent, 4: Complete
    val enrollmentStep: StateFlow<Int> = _enrollmentStep.asStateFlow()

    private val _enrollVoiceName = MutableStateFlow("Suara Saya Utama")
    val enrollVoiceName: StateFlow<String> = _enrollVoiceName.asStateFlow()

    private val _enrollChallengePhrase = MutableStateFlow("Biru tujuh sungai empat puluh sembilan")
    val enrollChallengePhrase: StateFlow<String> = _enrollChallengePhrase.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordedSampleFile = MutableStateFlow<File?>(null)
    val recordedSampleFile: StateFlow<File?> = _recordedSampleFile.asStateFlow()

    private val _consentChecked = MutableStateFlow(false)
    val consentChecked: StateFlow<Boolean> = _consentChecked.asStateFlow()

    init {
        // Automatically select first available voice profile if user switches to My Voice
        viewModelScope.launch {
            voiceProfiles.collect { profiles ->
                if (_selectedVoiceProfile.value == null && profiles.isNotEmpty()) {
                    _selectedVoiceProfile.value = profiles.first()
                }
            }
        }
    }

    fun setTab(tab: StudioTab) {
        _currentTab.value = tab
    }

    fun setComposerMode(mode: String) {
        _composerMode.value = mode
    }

    fun setPromptText(text: String) {
        _promptText.value = text
    }

    fun setSongTitle(title: String) {
        _songTitle.value = title
    }

    fun setLyricsText(lyrics: String) {
        _lyricsText.value = lyrics
    }

    fun setSongLanguage(lang: String) {
        _songLanguage.value = lang
    }

    fun setVocalType(type: String) {
        _vocalType.value = type
    }

    fun selectVoiceProfile(profile: VoiceProfileEntity) {
        _selectedVoiceProfile.value = profile
        _vocalType.value = "USER_VOICE"
    }

    fun applyGenrePreset(preset: GenreTemplate) {
        _selectedGenre.value = preset.title
        _selectedMood.value = preset.defaultMood
        _selectedBpm.value = preset.defaultBpm
        _selectedKey.value = preset.defaultKey
        _promptText.value = if (_songLanguage.value == "ms") preset.promptMs else preset.promptEn
    }

    fun runSongDirector() {
        viewModelScope.launch {
            _isPlanning.value = true
            try {
                val plan = directorRepo.planSong(_promptText.value, _songLanguage.value)
                _plannedDirectorOutput.value = plan
                _songTitle.value = plan.title
                _selectedGenre.value = plan.genre
                _selectedSubgenre.value = plan.subgenre
                _selectedMood.value = plan.mood.joinToString(", ")
                _selectedBpm.value = plan.bpm
                _selectedKey.value = plan.musicalKey
                _selectedDuration.value = plan.durationSeconds

                // Also generate initial lyrics
                _isWritingLyrics.value = true
                val lyricsRes = directorRepo.generateLyrics(
                    prompt = _promptText.value,
                    genre = plan.genre,
                    mood = plan.mood.firstOrNull() ?: "Emotional",
                    language = _songLanguage.value,
                    title = plan.title
                )
                _lyricsText.value = lyricsRes.fullLyrics
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPlanning.value = false
                _isWritingLyrics.value = false
            }
        }
    }

    fun generateLyricsOnly() {
        viewModelScope.launch {
            _isWritingLyrics.value = true
            try {
                val lyricsRes = directorRepo.generateLyrics(
                    prompt = _promptText.value,
                    genre = _selectedGenre.value,
                    mood = _selectedMood.value,
                    language = _songLanguage.value,
                    title = _songTitle.value
                )
                _lyricsText.value = lyricsRes.fullLyrics
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isWritingLyrics.value = false
            }
        }
    }

    fun refineLyrics(action: String) {
        viewModelScope.launch {
            _isWritingLyrics.value = true
            try {
                val refined = directorRepo.refineLyrics(
                    currentLyrics = _lyricsText.value,
                    action = action,
                    language = _songLanguage.value
                )
                _lyricsText.value = refined
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isWritingLyrics.value = false
            }
        }
    }

    fun startGeneratingSong() {
        viewModelScope.launch {
            val lyricsToUse = if (_lyricsText.value.isNotBlank()) {
                _lyricsText.value
            } else {
                directorRepo.generateLyrics(
                    prompt = _promptText.value,
                    genre = _selectedGenre.value,
                    mood = _selectedMood.value,
                    language = _songLanguage.value,
                    title = _songTitle.value
                ).fullLyrics
            }

            val completed = generationRepo.startSongGeneration(
                title = _songTitle.value.ifBlank { "Untitled Master" },
                prompt = _promptText.value,
                lyrics = lyricsToUse,
                language = _songLanguage.value,
                genre = _selectedGenre.value,
                subgenre = _selectedSubgenre.value,
                mood = _selectedMood.value,
                bpm = _selectedBpm.value,
                musicalKey = _selectedKey.value,
                durationSeconds = _selectedDuration.value,
                vocalType = _vocalType.value,
                voiceProfile = if (_vocalType.value == "USER_VOICE") _selectedVoiceProfile.value else null
            )

            if (completed != null) {
                player.playSong(completed)
                _currentTab.value = StudioTab.PLAYER
            }
        }
    }

    // Voice Enrollment
    fun prepareNewVoiceEnrollment() {
        _enrollmentStep.value = 1
        _enrollVoiceName.value = "Suara Saya #${(voiceProfiles.value.size + 1)}"
        _enrollChallengePhrase.value = voiceRepo.getRandomChallengePhrase(authRepo.selectedLanguage.value)
        _recordedSampleFile.value = null
        _consentChecked.value = false
    }

    fun startVoiceRecording() {
        val tempFile = File(app.cacheDir, "sample_voice_${System.currentTimeMillis()}.wav")
        _isRecordingVoice.value = true
        recorder.startRecording(tempFile) { success, file ->
            _isRecordingVoice.value = false
            if (success && file != null) {
                _recordedSampleFile.value = file
                _enrollmentStep.value = 3 // Review & Consent step
            }
        }
    }

    fun stopVoiceRecording() {
        recorder.stopRecording()
        _isRecordingVoice.value = false
    }

    fun setConsentChecked(checked: Boolean) {
        _consentChecked.value = checked
    }

    fun submitVoiceEnrollment() {
        val file = _recordedSampleFile.value ?: return
        if (!_consentChecked.value) return

        viewModelScope.launch {
            val consentText = "I confirm that this is my own voice, and I grant permission to AI Music Studio to create and process this voice profile for authorized music generation on my account."
            val created = voiceRepo.enrollVoiceProfile(
                name = _enrollVoiceName.value,
                language = authRepo.selectedLanguage.value,
                recordedSampleFile = file,
                challengePhrase = _enrollChallengePhrase.value,
                consentAgreementText = consentText
            )
            _selectedVoiceProfile.value = created
            _vocalType.value = "USER_VOICE"
            _enrollmentStep.value = 4
        }
    }

    fun deleteVoiceProfile(id: String) {
        viewModelScope.launch {
            voiceRepo.deleteVoiceProfile(id)
            if (_selectedVoiceProfile.value?.id == id) {
                _selectedVoiceProfile.value = null
                _vocalType.value = "AI_VOCAL"
            }
        }
    }

    // Library Actions
    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setLibraryFilter(filter: String) {
        _libraryFilter.value = filter
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            musicRepo.toggleFavorite(song.id, !song.isFavorite)
        }
    }

    fun renameSong(song: SongEntity, newTitle: String) {
        viewModelScope.launch {
            musicRepo.renameSong(song.id, newTitle)
        }
    }

    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            if (player.currentSong.value?.id == song.id) {
                player.stop()
            }
            musicRepo.deleteSong(song.id)
        }
    }

    fun downloadSong(song: SongEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val target = musicRepo.exportSongToDownloads(song)
            if (target != null) {
                onResult(true, "Tersimpan di Downloads: ${target.name}")
            } else {
                onResult(false, "Gagal memuat turun lagu.")
            }
        }
    }
}
