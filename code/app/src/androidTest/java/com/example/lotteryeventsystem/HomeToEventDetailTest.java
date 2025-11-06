package com.example.lotteryeventsystem;

import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class HomeToEventDetailTest {

    @Test
    public void testNavigationToEventDetailFragment() {
        // Launch MainActivity
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            // Prepare test arguments for EventDetailFragment
            Bundle args = new Bundle();
            args.putString(EventDetailFragment.ARG_EVENT_ID, "testEvent123");

            // Replace nav host fragment with EventDetailFragment
            EventDetailFragment fragment = new EventDetailFragment();
            fragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment) // use actual nav_host ID
                    .commitNow();
        });

        // Check that the header title TextView exists (default empty or placeholder text)
        Espresso.onView(ViewMatchers.withId(R.id.header_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testEventIdPassedToEventDetailFragment() {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            Bundle args = new Bundle();
            args.putString(EventDetailFragment.ARG_EVENT_ID, "12345");

            EventDetailFragment fragment = new EventDetailFragment();
            fragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commitNow();

            // Verify the fragment received the correct argument
            assert fragment.getArguments() != null;
            assert "12345".equals(fragment.getArguments().getString(EventDetailFragment.ARG_EVENT_ID));
        });
    }
}