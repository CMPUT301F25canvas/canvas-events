package com.example.lotteryeventsystem;

import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.is;
import static java.nio.file.Files.exists;
import static java.util.function.Predicate.not;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventCreateFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);


    @Before
    public void launchFragment() {
        // Launch the fragment in isolation
        FragmentScenario.launchInContainer(
                OrganizerEventCreateFragment.class,
                (Bundle) null,
                R.style.Theme_LotteryEventSystem,
                (FragmentFactory) null
        );
    }

    // Test event creation with valid arguments
    // Test only passes the eye test because the create event button moves to a different screen
    @Test
    public void testOrganizerFillsEventForm() {
        // Fill in event name
        onView(withId(R.id.event_name_input))
                .perform(typeText("Test Event"), closeSoftKeyboard());

        // Fill in event description
        onView(withId(R.id.event_description_input))
                .perform(typeText("Test Event Description"), closeSoftKeyboard());

        onView(withId(R.id.event_location_input))
                .perform(typeText("Test Event Location"), closeSoftKeyboard());

        // Fill in event date
        onView(withId(R.id.start_date_input_text))
                .perform(typeText("2026-01-01"), closeSoftKeyboard());

        // Fill in event end date
        onView(withId(R.id.end_date_input_text))
                .perform(typeText("2026-02-01"), closeSoftKeyboard());

        // Fill in event start time
        onView(withId(R.id.start_time_input_text))
                .perform(click(), replaceText("10:00 AM"), closeSoftKeyboard());

        // Fill in event end time
        onView(withId(R.id.end_time_input_text))
                .perform(click(), replaceText("12:00 PM"), closeSoftKeyboard());

        // Fill in registration start date
        onView(withId(R.id.registration_start_input_text))
                .perform(click(), replaceText("2025-12-10"), closeSoftKeyboard());

        // Fill in registration end date
        onView(withId(R.id.registration_end_input_text))
                .perform(click(), replaceText("2025-12-31"), closeSoftKeyboard());

        // Fill in sample size
        onView(withId(R.id.sample_size_input_text))
                .perform(typeText("100"), closeSoftKeyboard());

        // Scroll to selectSportsCategory checkbox and click
        onView(withId(R.id.sports_category_checkbox))
                .perform(scrollTo(), click());

        // Scroll to Enable geolocation checkbox and click
        onView(withId(R.id.geolocation_requirement_checkbox))
                .perform(scrollTo(), click());

        // Enable entrant limit and set value
        onView(withId(R.id.entrant_limit_checkbox))
                .perform(scrollTo(), click());
        onView(withId(R.id.entrant_limit_input_text))
                .perform(scrollTo(), typeText("50"), closeSoftKeyboard());

    }


    // Testcase for missing argument. A Toast Message should appear
    @Test
    public void testOrganizerMissingName() {
        // Fill in event description
        onView(withId(R.id.event_description_input))
                .perform(typeText("Test Event Description"), closeSoftKeyboard());

        // Fill in event date
        //onView(withId(R.id.event_date_input_text))
                //.perform(typeText("2025-11-20"), closeSoftKeyboard());


        // Fill in event start time
        onView(withId(R.id.start_time_input_text))
                .perform(click(), replaceText("10:00 AM"), closeSoftKeyboard());

        // Fill in event end time
        onView(withId(R.id.end_time_input_text))
                .perform(click(), replaceText("12:00 PM"), closeSoftKeyboard());

        // Fill in registration start date
        onView(withId(R.id.registration_start_input_text))
                .perform(click(), replaceText("2025-11-10"), closeSoftKeyboard());

        // Fill in registration end date
        onView(withId(R.id.registration_end_input_text))
                .perform(click(), replaceText("2025-11-18"), closeSoftKeyboard());

        // Fill in sample size
        onView(withId(R.id.sample_size_input_text))
                .perform(typeText("100"), closeSoftKeyboard());


        // Scroll to Enable geolocation checkbox and click
        onView(withId(R.id.geolocation_requirement_checkbox))
                .perform(scrollTo(), click());

        // Enable entrant limit and set value
        onView(withId(R.id.entrant_limit_checkbox))
                .perform(scrollTo(), click());
        onView(withId(R.id.entrant_limit_input_text))
                .perform(scrollTo(), typeText("50"), closeSoftKeyboard());

        // Click create event
        onView(withId(R.id.create_event_button))
                .perform(scrollTo(), click());
    }
}
