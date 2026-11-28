package app.qurancommunity.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Every destination in the app (docs/specs/screens.md, design/SCREENS.md). Keys carry only primitives (ids, verse keys
 * as "surah:ayah", word locations as "surah:ayah:word"), so they survive process death and work as deep links.
 * Every key must be registered in [appNavSerializersModule] (a Konsist test checks it).
 */
sealed interface AppNavKey : NavKey

// Home, moon, settings, onboarding
@Serializable data object HomeKey : AppNavKey

@Serializable data object MoonKey : AppNavKey

@Serializable data object SettingsKey : AppNavKey

@Serializable data object CityPickerKey : AppNavKey

@Serializable data object WhatWeCollectKey : AppNavKey

@Serializable data object FeedbackKey : AppNavKey

@Serializable data object OnboardingKey : AppNavKey

// الذكر, the Mushaf and study
@Serializable data object QuranHubKey : AppNavKey

/** The Mushaf at [page], or at [verseKey] (highlighted on arrival, DESIGN §11 "verse landing"). */
@Serializable data class MushafKey(val page: Int? = null, val verseKey: String? = null) : AppNavKey

@Serializable data object MushafIndexKey : AppNavKey

@Serializable data class MushafSearchKey(val query: String? = null) : AppNavKey

@Serializable data class AyahStudyKey(val verseKey: String) : AppNavKey

@Serializable data class WordStudyKey(val location: String) : AppNavKey

@Serializable data class SurahOverviewKey(val surah: Int) : AppNavKey

/** The Hizb screen at [hizb], or at the user's own position when null. */
@Serializable data class HizbKey(val hizb: Int? = null) : AppNavKey

// الأبواب
@Serializable data object DoorsKey : AppNavKey

@Serializable data class DoorKey(val doorId: String, val entryId: String? = null) : AppNavKey

@Serializable data class DoorAllVersesKey(val doorId: String) : AppNavKey

@Serializable data class HikmaPassageKey(val passageId: String) : AppNavKey

// الضياء
@Serializable data object DiyaaKey : AppNavKey

@Serializable data class StationKey(val number: Int) : AppNavKey

// Collections
@Serializable data object CollectionsKey : AppNavKey

@Serializable data class CollectionKey(val collectionId: String) : AppNavKey

// Live and videos
@Serializable data object LiveKey : AppNavKey

@Serializable data class VideoPlayerKey(val videoId: String) : AppNavKey

// Community and profiles
@Serializable data object CommunityKey : AppNavKey

@Serializable data class PostKey(val postId: String, val replyId: String? = null) : AppNavKey

@Serializable
data class ComposeKey(
    val postType: String,
    val verseKey: String? = null,
    val replyTo: String? = null,
) : AppNavKey

@Serializable data object VersePickerKey : AppNavKey

@Serializable data class ProfileKey(val userId: String) : AppNavKey

@Serializable data object MyProfileKey : AppNavKey

// Admin
@Serializable data object AdminKey : AppNavKey

@Serializable data object AdminReviewKey : AppNavKey

@Serializable data class AdminUserKey(val userId: String? = null) : AppNavKey

@Serializable data object AdminRolesKey : AppNavKey

@Serializable data object AdminLiveKey : AppNavKey

@Serializable data object AdminAnnounceKey : AppNavKey

@Serializable data object AdminFeedbackKey : AppNavKey
