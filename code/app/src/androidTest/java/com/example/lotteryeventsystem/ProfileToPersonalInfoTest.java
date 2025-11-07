package com.example.lotteryeventsystem;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test navigation to and from personal info page
 *
 * @author Ethan Kinch
 */
@RunWith(AndroidJUnit4.class)
public class ProfileToPersonalInfoTest {
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
}
