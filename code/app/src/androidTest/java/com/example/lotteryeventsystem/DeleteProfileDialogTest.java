package com.example.lotteryeventsystem;

import android.Manifest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test the delete profile dialog, as a UI element
 *
 * @author Ethan Kinch
 */
public class DeleteProfileDialogTest {
    /**
     * stops android permissions from impeding tests
     */
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Test
    public void testDeleteProfileDialog() {
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
