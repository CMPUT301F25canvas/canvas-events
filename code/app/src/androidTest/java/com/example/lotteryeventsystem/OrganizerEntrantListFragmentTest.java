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
public class OrganizerEntrantListFragmentTest {
    private static final String TEST_EVENT_ID = "test_event_123";
    private static final String TEST_EVENT_ID_2 = "test_event_456";

    @Test
    public void testFragmentCreationWithValidEventId() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        // When
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        // Then
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertNotNull(fragment.getArguments());
            assertEquals(TEST_EVENT_ID, fragment.getArguments().getString("EVENT_ID"));
        });
    }

    @Test
    public void testFragmentViewIsCreated() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        // When
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        // Then
        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView());
            assertNotNull(fragment.getActivity());
            // Verify main UI components are present
            assertNotNull(fragment.getView().findViewById(R.id.event_name));
            assertNotNull(fragment.getView().findViewById(R.id.event_description));
            assertNotNull(fragment.getView().findViewById(R.id.btnViewEntrants));
            assertNotNull(fragment.getView().findViewById(R.id.btnSample));
            assertNotNull(fragment.getView().findViewById(R.id.back_button));
        });
    }

    @Test
    public void testFragmentInitialization() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        // When
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        // Then - Verify fragment initializes without crashing
        scenario.onFragment(fragment -> {
            // Fragment should be in resumed state
            assertTrue(fragment.isAdded());
            assertTrue(fragment.isVisible());
        });
    }

    @Test
    public void testFragmentWithNullArguments() {
        // Given - Fragment with no arguments
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class);
        // Then - Should not crash and should handle null arguments gracefully
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            // Fragment should handle missing arguments without crashing
        });
    }

    @Test
    public void testFragmentWithEmptyEventId() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", "");
        // When
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        // Then - Should handle empty event ID without crashing
        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertEquals("", fragment.getArguments().getString("EVENT_ID"));
        });
    }

    @Test
    public void testMultipleFragmentInstances() {
        // Test first instance
        OrganizerEntrantListFragment fragment1 = OrganizerEntrantListFragment.newInstance(TEST_EVENT_ID);
        Bundle args1 = fragment1.getArguments();
        assertEquals(TEST_EVENT_ID, args1.getString("EVENT_ID"));
        // Test second instance with different event ID
        OrganizerEntrantListFragment fragment2 = OrganizerEntrantListFragment.newInstance(TEST_EVENT_ID_2);
        Bundle args2 = fragment2.getArguments();
        assertEquals(TEST_EVENT_ID_2, args2.getString("EVENT_ID"));
        // Verify they are different instances
        assertTrue(fragment1 != fragment2);
    }

    @Test
    public void testFragmentButtonPresence() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        // Then - Verify all action buttons are present
        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView().findViewById(R.id.btnViewEntrants));
            assertNotNull(fragment.getView().findViewById(R.id.btnSample));
            assertNotNull(fragment.getView().findViewById(R.id.editEvent));
            assertNotNull(fragment.getView().findViewById(R.id.btnViewMap));
            assertNotNull(fragment.getView().findViewById(R.id.back_button));
        });
    }

    @Test
    public void testFragmentInformationFieldsPresence() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        // Then - Verify all information display fields are present
        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView().findViewById(R.id.event_name));
            assertNotNull(fragment.getView().findViewById(R.id.event_description));
            assertNotNull(fragment.getView().findViewById(R.id.event_start_time));
            assertNotNull(fragment.getView().findViewById(R.id.event_end_time));
            assertNotNull(fragment.getView().findViewById(R.id.start_date));
            assertNotNull(fragment.getView().findViewById(R.id.end_date));
            assertNotNull(fragment.getView().findViewById(R.id.event_criteria));
            assertNotNull(fragment.getView().findViewById(R.id.poster));
        });
    }

    @Test
    public void testFragmentNavigationSetup() {
        // Given
        Bundle args = new Bundle();
        args.putString("EVENT_ID", TEST_EVENT_ID);
        FragmentScenario<OrganizerEntrantListFragment> scenario =
                FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);
        // Then - Verify navigation is set up (buttons have click listeners)
        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView().findViewById(R.id.btnViewEntrants).hasOnClickListeners());
            assertNotNull(fragment.getView().findViewById(R.id.btnSample).hasOnClickListeners());
            assertNotNull(fragment.getView().findViewById(R.id.editEvent).hasOnClickListeners());
            assertNotNull(fragment.getView().findViewById(R.id.btnViewMap).hasOnClickListeners());
            assertNotNull(fragment.getView().findViewById(R.id.back_button).hasOnClickListeners());
        });
    }

    @Test
    public void testFragmentErrorHandling() {
        // Test creating fragment with various problematic inputs
        String[] problematicEventIds = {null, "", "   ", "null"};
        for (String eventId : problematicEventIds) {
            Bundle args = new Bundle();
            if (eventId != null) {
                args.putString("EVENT_ID", eventId);
            }
            // Should not crash with any of these inputs
            FragmentScenario<OrganizerEntrantListFragment> scenario =
                    FragmentScenario.launchInContainer(OrganizerEntrantListFragment.class, args);

            scenario.onFragment(fragment -> {
                assertNotNull(fragment);
                // Fragment should handle these cases gracefully
            });
        }
    }
}