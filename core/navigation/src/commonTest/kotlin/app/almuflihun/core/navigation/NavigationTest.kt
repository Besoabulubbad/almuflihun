package app.almuflihun.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationTest {

    private val samples: List<AppNavKey> = listOf(
        HomeKey, MoonKey, SettingsKey, CityPickerKey, WhatWeCollectKey, FeedbackKey, OnboardingKey,
        QuranHubKey, MushafKey(), MushafKey(page = 44), MushafKey(verseKey = "16:90"), MushafIndexKey,
        MushafSearchKey("صبر"), AyahStudyKey("2:219"), WordStudyKey("2:219:15"), SurahOverviewKey(2), HizbKey(),
        HizbKey(2), DoorsKey, DoorKey("infaq", "infaq-01"), DoorAllVersesKey("infaq"), HikmaPassageKey("isra"),
        DiyaaKey, StationKey(4), CollectionsKey, CollectionKey("c1"), LiveKey, VideoPlayerKey("v1"), CommunityKey,
        PostKey("p1", "r1"), ComposeKey(postType = "question", verseKey = "2:219"), VersePickerKey, ProfileKey("u1"),
        MyProfileKey, AdminKey, AdminReviewKey, AdminUserKey("u1"), AdminRolesKey, AdminLiveKey, AdminAnnounceKey,
        AdminFeedbackKey,
    )

    private val json = Json { serializersModule = appNavSerializersModule }
    private val serializer = PolymorphicSerializer(NavKey::class)

    @Test
    fun `every key round-trips through the registered serializers`() {
        samples.forEach { key ->
            val encoded = json.encodeToString(serializer, key)
            assertEquals(key, json.decodeFromString(serializer, encoded), encoded)
        }
    }

    @Test
    fun `back pops until only home is left`() {
        val nav = StackNavigator(NavBackStack(HomeKey))
        nav.goTo(QuranHubKey)
        nav.goTo(MushafKey(page = 2))
        assertTrue(nav.back())
        assertTrue(nav.back())
        assertFalse(nav.back())
    }

    @Test
    fun `popTo returns to a key or to home`() {
        val stack = NavBackStack<NavKey>(HomeKey)
        val nav = StackNavigator(stack)
        listOf(QuranHubKey, DoorsKey, DoorKey("infaq")).forEach(nav::goTo)
        nav.popTo(QuranHubKey)
        assertEquals(listOf<NavKey>(HomeKey, QuranHubKey), stack.toList())
        nav.popTo(SettingsKey)
        assertEquals(listOf<NavKey>(HomeKey), stack.toList())
    }

    @Test
    fun `goToReplacing hands a flow over to its result`() {
        val stack = NavBackStack<NavKey>(HomeKey)
        val nav = StackNavigator(stack)
        listOf(CommunityKey, VersePickerKey, ComposeKey(postType = "verse")).forEach(nav::goTo)
        nav.goToReplacing(PostKey("p1")) { it is ComposeKey || it is VersePickerKey }
        assertEquals(listOf<NavKey>(HomeKey, CommunityKey, PostKey("p1")), stack.toList())
    }

    @Test
    fun `resetTo builds a deep-link stack on top of home`() {
        val stack = NavBackStack<NavKey>(HomeKey)
        val nav = StackNavigator(stack)
        listOf(MoonKey, SettingsKey).forEach(nav::goTo)
        nav.resetTo(listOf(CommunityKey, PostKey("p1", "r1")))
        assertEquals(listOf<NavKey>(HomeKey, CommunityKey, PostKey("p1", "r1")), stack.toList())
    }
}
