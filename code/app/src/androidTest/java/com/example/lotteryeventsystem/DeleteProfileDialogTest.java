package com.example.lotteryeventsystem;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;

/**
 * Test the delete profile dialog, as a UI element
 *
 * @author Ethan Kinch
 */
public class DeleteProfileDialogTest {
    @Test
    public void testNavigationProfileToPersonalInfo() {
        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        // navigate to profile
        Espresso.onView(ViewMatchers.withId(R.id.profileFragment)).perform(ViewActions.click());

        // click delete profile
        Espresso.onView(ViewMatchers.withId(R.id.delete_profile)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.delete_profile)).perform(ViewActions.click());

        // cancel delete
        Espresso.onView(ViewMatchers.withText("Yes")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Cancel")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withText("Cancel")).perform(ViewActions.click());

        // check for a unique UI element
        Espresso.onView(ViewMatchers.withId(R.id.delete_profile)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
