package app.qurancommunity.core.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * What features call to move around. Features never touch the back stack directly; `app:shared` provides
 * [StackNavigator]. The app has one back stack (no bottom tabs): Home leads into the portals.
 */
interface Navigator {
    /** Pushes [key]. */
    fun goTo(key: AppNavKey)

    /** Pops the top; returns false when only Home is left (the platform then handles back). */
    fun back(): Boolean

    /** Pops until [key] is on top, or to Home if it is not in the stack. */
    fun popTo(key: AppNavKey)

    /** Pops every entry on top that [replaces] matches (never Home), then pushes [key]. */
    fun goToReplacing(key: AppNavKey, replaces: (AppNavKey) -> Boolean)

    /**
     * Replaces the whole stack, for a deep link or a notification: Home, then [path]. Back from the target then walks
     * a sensible history (docs/guides/quality-bar.md).
     */
    fun resetTo(path: List<AppNavKey>)
}

/**
 * Explicit polymorphic registration of every key. Required on iOS, where the reflection-based back-stack serializer
 * is unavailable; a unit test round-trips every key through it, and Konsist checks every key is listed.
 */
val appNavSerializersModule: SerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(HomeKey::class, HomeKey.serializer())
        subclass(MoonKey::class, MoonKey.serializer())
        subclass(SettingsKey::class, SettingsKey.serializer())
        subclass(CityPickerKey::class, CityPickerKey.serializer())
        subclass(WhatWeCollectKey::class, WhatWeCollectKey.serializer())
        subclass(FeedbackKey::class, FeedbackKey.serializer())
        subclass(OnboardingKey::class, OnboardingKey.serializer())
        subclass(QuranHubKey::class, QuranHubKey.serializer())
        subclass(MushafKey::class, MushafKey.serializer())
        subclass(MushafIndexKey::class, MushafIndexKey.serializer())
        subclass(MushafSearchKey::class, MushafSearchKey.serializer())
        subclass(AyahStudyKey::class, AyahStudyKey.serializer())
        subclass(WordStudyKey::class, WordStudyKey.serializer())
        subclass(SurahOverviewKey::class, SurahOverviewKey.serializer())
        subclass(HizbKey::class, HizbKey.serializer())
        subclass(DoorsKey::class, DoorsKey.serializer())
        subclass(DoorKey::class, DoorKey.serializer())
        subclass(DoorAllVersesKey::class, DoorAllVersesKey.serializer())
        subclass(HikmaPassageKey::class, HikmaPassageKey.serializer())
        subclass(DiyaaKey::class, DiyaaKey.serializer())
        subclass(StationKey::class, StationKey.serializer())
        subclass(CollectionsKey::class, CollectionsKey.serializer())
        subclass(CollectionKey::class, CollectionKey.serializer())
        subclass(LiveKey::class, LiveKey.serializer())
        subclass(VideoPlayerKey::class, VideoPlayerKey.serializer())
        subclass(CommunityKey::class, CommunityKey.serializer())
        subclass(PostKey::class, PostKey.serializer())
        subclass(ComposeKey::class, ComposeKey.serializer())
        subclass(VersePickerKey::class, VersePickerKey.serializer())
        subclass(ProfileKey::class, ProfileKey.serializer())
        subclass(MyProfileKey::class, MyProfileKey.serializer())
        subclass(AdminKey::class, AdminKey.serializer())
        subclass(AdminReviewKey::class, AdminReviewKey.serializer())
        subclass(AdminUserKey::class, AdminUserKey.serializer())
        subclass(AdminRolesKey::class, AdminRolesKey.serializer())
        subclass(AdminLiveKey::class, AdminLiveKey.serializer())
        subclass(AdminAnnounceKey::class, AdminAnnounceKey.serializer())
        subclass(AdminFeedbackKey::class, AdminFeedbackKey.serializer())
    }
}

/** The configuration `rememberNavBackStack` needs on every platform. */
val appSavedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = appNavSerializersModule
}

/**
 * The navigation state as plain observable data over one [stack] that starts at [HomeKey]. `app:shared` creates the
 * stack with `rememberNavBackStack(appSavedStateConfiguration, HomeKey)` so it survives process death.
 */
class StackNavigator(private val stack: NavBackStack<NavKey>) : Navigator {

    /** Changes each time the stack changes, for observers that need a signal. */
    val version = mutableStateOf(0)

    override fun goTo(key: AppNavKey) = edit { stack.add(key) }

    override fun back(): Boolean {
        if (stack.size <= 1) return false
        edit { stack.removeAt(stack.lastIndex) }
        return true
    }

    override fun popTo(key: AppNavKey) = edit {
        while (stack.size > 1 && stack.last() != key) stack.removeAt(stack.lastIndex)
    }

    override fun goToReplacing(key: AppNavKey, replaces: (AppNavKey) -> Boolean) = edit {
        while (stack.size > 1 && (stack.last() as? AppNavKey)?.let(replaces) == true) stack.removeAt(stack.lastIndex)
        stack.add(key)
    }

    override fun resetTo(path: List<AppNavKey>) = edit {
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
        if (stack.firstOrNull() != HomeKey) {
            stack.clear()
            stack.add(HomeKey)
        }
        path.filter { it != HomeKey }.forEach { stack.add(it) }
    }

    private fun edit(change: () -> Unit) {
        change()
        version.value++
    }
}
