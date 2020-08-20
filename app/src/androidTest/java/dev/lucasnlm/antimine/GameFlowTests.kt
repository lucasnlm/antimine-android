package dev.lucasnlm.antimine

import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test

class GameFlowTests {
    @get:Rule
    var activityRule = ActivityTestRule(GameActivity::class.java)

    @Test
    fun testGoToStats() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.stats))

        onView(withText(R.string.events)).check(matches(isDisplayed()))
    }

    @Test
    fun testGoToBeginner() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.beginner))

        onView(withText("10")).check(matches(isDisplayed()))

        assert(activityRule.activity.findViewById<RecyclerView>(R.id.recyclerGrid).adapter?.itemCount == 9 * 9)
    }

    @Test
    fun testGoToIntermediate() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.intermediate))

        onView(withText("40")).check(matches(isDisplayed()))

        assert(activityRule.activity.findViewById<RecyclerView>(R.id.recyclerGrid).adapter?.itemCount == 16 * 16)
    }

    @Test
    fun testGoToExpert() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.expert))

        onView(withText("99")).check(matches(isDisplayed()))

        assert(activityRule.activity.findViewById<RecyclerView>(R.id.recyclerGrid).adapter?.itemCount == 24 * 24)
    }

    @Test
    fun testGoToCustom() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.custom))

        onView(withId(R.id.map_width)).perform(click()).perform(typeText("12"))
        onView(withId(R.id.map_height)).perform(click()).perform(typeText("8"))
        onView(withId(R.id.map_mines)).perform(click()).perform(typeText("7"))

        onView(withText(R.string.start)).perform(click())

        onView(withText("7")).check(matches(isDisplayed()))

        assert(activityRule.activity.findViewById<RecyclerView>(R.id.recyclerGrid).adapter?.itemCount == 12 * 8)
    }

    @Test
    fun testGoToControl() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.control))

        onView(withText(R.string.standard)).perform(click())

        onView(withText(R.string.ok)).perform(click())
    }

    @Test
    fun testGoToPreviousGames() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.previous_games))

        onView(withText(R.string.previous_games)).check(matches(isDisplayed()))
    }

    @Test
    fun testGoToSettings() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.settings))

        onView(withText(R.string.settings)).check(matches(isDisplayed()))
    }

    @Test
    fun testGoToAbout() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.about))

        onView(withText(R.string.app_name)).check(matches(isDisplayed()))
        onView(withText(R.string.show_licenses)).check(matches(isDisplayed()))
        onView(withText(R.string.translation)).check(matches(isDisplayed()))
        onView(withText(R.string.source_code)).check(matches(isDisplayed()))

        onView(withText(R.string.show_licenses)).perform(click())
        onView(withText(R.string.licenses)).check(matches(isDisplayed()))
        onView(withText(R.string.used_software_text)).check(matches(isDisplayed()))
        onView(isRoot()).perform(ViewActions.pressBack())

        onView(withText(R.string.translation)).perform(click())
        onView(withText(R.string.translators_text)).check(matches(isDisplayed()))
        onView(isRoot()).perform(ViewActions.pressBack())

        onView(isRoot()).perform(ViewActions.pressBack())
    }

    @Test
    fun testGoToThemes() {
        onView(withId(R.id.drawer))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.navigationView))
            .perform(NavigationViewActions.navigateTo(R.id.themes))

        onView(withText(R.string.themes)).check(matches(isDisplayed()))
    }
}
