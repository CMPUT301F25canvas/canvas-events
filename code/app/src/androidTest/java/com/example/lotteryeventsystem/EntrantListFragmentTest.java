package com.example.lotteryeventsystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import android.os.Bundle;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EntrantListFragmentTest {
    private static final String TEST_EVENT_ID = "test_event_123";
    private static final String TEST_EVENT_ID_2 = "test_event_456";

    @Test
    public void testFragmentCreationWithEnrolledListType() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        args.putString("LIST_TYPE", "enrolled");
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        // Then
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertEquals("enrolled", fragment.getArguments().getString("LIST_TYPE"));
        });
    }

    @Test
    public void testFragmentCreationWithCanceledListType() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        args.putString("LIST_TYPE", "canceled");
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        // Then
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertEquals("canceled", fragment.getArguments().getString("LIST_TYPE"));
        });
    }

    @Test
    public void testFragmentCreationWithWaitingListType() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        args.putString("LIST_TYPE", "waiting");
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        // Then
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertEquals("waiting", fragment.getArguments().getString("LIST_TYPE"));
        });
    }

    @Test
    public void testFragmentCreationWithAllListType() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        args.putString("LIST_TYPE", "all");
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        // Then
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertEquals("all", fragment.getArguments().getString("LIST_TYPE"));
        });
    }

    @Test
    public void testFragmentViewIsCreated() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        args.putString("LIST_TYPE", "unenrolled");
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        // Then
        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView());
            assertNotNull(fragment.getActivity());
        });
    }

    @Test
    public void testFragmentInitialization() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        args.putString("LIST_TYPE", "unenrolled");
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        // Then - Verify fragment initializes without crashing
        scenario.onFragment(fragment -> {
            // Fragment should be in resumed state
            assertTrue(fragment.isAdded());
            assertTrue(fragment.isVisible());
        });
    }

    @Test
    public void testMultipleFragmentInstances() {
        // Test first instance
        EntrantListFragment fragment1 = EntrantListFragment.newInstance(TEST_EVENT_ID, "unenrolled");
        Bundle args1 = fragment1.getArguments();
        assertEquals(TEST_EVENT_ID, args1.getString("EVENT_ID"));
        assertEquals("unenrolled", args1.getString("LIST_TYPE"));
        // Test second instance with different arguments
        EntrantListFragment fragment2 = EntrantListFragment.newInstance(TEST_EVENT_ID_2, "enrolled");
        Bundle args2 = fragment2.getArguments();
        assertEquals(TEST_EVENT_ID_2, args2.getString("EVENT_ID"));
        assertEquals("enrolled", args2.getString("LIST_TYPE"));
        // Verify they are different instances
        assertTrue(fragment1 != fragment2);
    }

    @Test
    public void testFragmentWithNullArguments() {
        // Given - Fragment with no arguments
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class);
        // Then - Should not crash and should handle null arguments gracefully
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            // Fragment should handle missing arguments without crashing
        });
    }

    @Test
    public void testFragmentWithEmptyArguments() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", "");
        args.putString("LIST_TYPE", "");
        // When
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        // Then - Should handle empty arguments without crashing
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertEquals("", fragment.getArguments().getString("EVENT_ID"));
            assertEquals("", fragment.getArguments().getString("LIST_TYPE"));
        });
    }

    @Test
    public void testFragmentWithInvalidListTypes() {
        // Test with unknown list type
        EntrantListFragment fragment = EntrantListFragment.newInstance(TEST_EVENT_ID, "unknown_type");
        assertEquals("unknown_type", fragment.getArguments().getString("LIST_TYPE"));
        // Test with null list type
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        args.putString("LIST_TYPE", null);
        FragmentScenario<EntrantListFragment> scenario =
                FragmentScenario.launchInContainer(EntrantListFragment.class, args);
        scenario.onFragment(fragmentInstance -> {
            // Should handle null list type without crashing
            assertNotNull(fragmentInstance);
        });
    }
}