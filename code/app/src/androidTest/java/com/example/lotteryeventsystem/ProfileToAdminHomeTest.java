package com.example.lotteryeventsystem;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileToAdminHomeTest {

    @Test
    public void testNavigationProfileToAdminHome() {
        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.profileFragment)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText("Login as Admin")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Login as Admin")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withClassName(org.hamcrest.Matchers.containsString("EditText")))
                .perform(ViewActions.replaceText("canvas"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withText("Confirm"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.homeFragment)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.adminTitle))  // replace with actual ID in admin_home_fragment.xml
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.homeFragment))
                .perform(ViewActions.click());
    }

    @Test
    public void testNavigationAdminHomeToAdminEventsView() {
        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.profileFragment)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText("Login as Admin")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Login as Admin")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withClassName(org.hamcrest.Matchers.containsString("EditText")))
                .perform(ViewActions.replaceText("canvas"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withText("Confirm"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.homeFragment)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.admin_events_button))
                .perform(ViewActions.click());

        // Click on the "Events" button or menu item
        Espresso.onView(ViewMatchers.withText("Events"))
                .perform(ViewActions.click());

        // Verify that the AdminEventsView is displayed
        Espresso.onView(ViewMatchers.withId(R.id.home_label))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testNavigationAdminHomeToAdminProfilesView() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.profileFragment)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText("Login as Admin")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Login as Admin")).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withClassName(org.hamcrest.Matchers.containsString("EditText")))
                .perform(ViewActions.replaceText("canvas"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withText("Confirm"))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.homeFragment)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.admin_profiles_button))
                .perform(ViewActions.click());

        // Click on the "Events" button or menu item
        Espresso.onView(ViewMatchers.withText("Profiles"))
                .perform(ViewActions.click());

        // Verify that the AdminEventsView is displayed
        Espresso.onView(ViewMatchers.withId(R.id.home_label))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
