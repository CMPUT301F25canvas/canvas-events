package com.example.lotteryeventsystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import android.os.Bundle;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantListFragmentTest {

    // Test fragment behavior with valid arguments
    @Test
    public void testFragmentLaunch_withValidArguments_shouldDisplay() {
        String eventId = "testEvent123";
        String listType = "enrolled";
        Bundle args = new Bundle();
        args.putString("EVENT_ID", eventId);
        args.putString("LIST_TYPE", listType);
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertNotNull(fragment.getArguments());
            assertEquals(eventId, fragment.getArguments().getString("EVENT_ID"));
            assertEquals(listType, fragment.getArguments().getString("LIST_TYPE"));
        });
    }

     // Tests fragment behavior with null arguments
    @Test
    public void testFragmentLaunch_withNullArguments_shouldHandleGracefully() {
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, null);
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
        });
    }
}
