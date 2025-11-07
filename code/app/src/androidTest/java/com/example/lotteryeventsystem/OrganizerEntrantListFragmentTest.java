package com.example.lotteryeventsystem;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import android.os.Bundle;

@RunWith(AndroidJUnit4.class)
public class OrganizerEntrantListFragmentTest {

     // Tests that fragment displays correctly with valid event ID
    @Test
    public void testFragmentLaunch_withValidEventId_shouldDisplay() {
        String eventId = "testEvent123";
        Bundle args = new Bundle();
        args.putString("EVENT_ID", eventId);
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertNotNull(fragment.getArguments());
            assertEquals(eventId, fragment.getArguments().getString("EVENT_ID"));
        });
    }

     // Tests fragment behavior when event ID is null
    @Test
    public void testFragmentLaunch_withNullEventId_shouldHandleGracefully() {
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, null);
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
        });
    }

     // Tests that fragment initializes its views properly
    @Test
    public void testFragmentViews_shouldBeInitialized() {
        Bundle args = new Bundle();
        args.putString("EVENT_ID", "testEvent456");
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView());
        });
    }
}
