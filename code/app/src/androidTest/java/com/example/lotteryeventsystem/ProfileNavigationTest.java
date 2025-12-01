package com.example.lotteryeventsystem;

import android.Manifest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test navigation to and from personal info page
 *
 * @author Ethan Kinch
 */
@RunWith(AndroidJUnit4.class)
public class ProfileNavigationTest {

    /**
     * stops android permissions from impeding tests
     */
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    /**
     * navigates to and from personal info page from profile page
     */
    @Test
    public void testNavigationProfileToPersonalInfo() {
        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // navigate to profile
        Espresso.onView(ViewMatchers.withId(R.id.profileFragment)).perform(ViewActions.click());

        // navigate to personal info
        Espresso.onView(ViewMatchers.withId(R.id.personal_info)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.personal_info)).perform(ViewActions.click());

        // check for a unique UI element
        Espresso.onView(ViewMatchers.withId(R.id.email)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // navigate back to profile
        Espresso.onView(ViewMatchers.withId(R.id.back_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.back_button)).perform(ViewActions.click());

        // check for a unique UI element
        Espresso.onView(ViewMatchers.withId(R.id.personal_info)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }


    /**
     * navigates to and from event history page from profile page
     */
    @Test
    public void testNavigationProfileToEventHistory() {
        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // navigate to profile
        Espresso.onView(ViewMatchers.withId(R.id.profileFragment)).perform(ViewActions.click());

        // navigate to event history
        Espresso.onView(ViewMatchers.withId(R.id.event_history)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.event_history)).perform(ViewActions.click());

        // check for a unique UI element
        Espresso.onView(ViewMatchers.withId(R.id.event_history_text)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // navigate back to profile
        Espresso.onView(ViewMatchers.withId(R.id.back_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.back_button)).perform(ViewActions.click());

        // check for a unique UI element
        Espresso.onView(ViewMatchers.withId(R.id.event_history)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }


    /**
     * navigates to and from created events page from profile page
     */
    @Test
    public void testNavigationProfileToCreatedEvents() {
        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // navigate to profile
        Espresso.onView(ViewMatchers.withId(R.id.profileFragment)).perform(ViewActions.click());

        // navigate to created events
        Espresso.onView(ViewMatchers.withId(R.id.my_created_events)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.my_created_events)).perform(ViewActions.click());

        // check for a unique UI element
        Espresso.onView(ViewMatchers.withId(R.id.create_event_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // navigate back to profile
        Espresso.onView(ViewMatchers.withId(R.id.back_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.back_button)).perform(ViewActions.click());

        // check for a unique UI element
        Espresso.onView(ViewMatchers.withId(R.id.my_created_events)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
