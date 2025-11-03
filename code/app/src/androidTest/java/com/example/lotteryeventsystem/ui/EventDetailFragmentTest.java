package com.example.lotteryeventsystem.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEmptyString.isEmptyString;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import com.example.lotteryeventsystem.EventDetailFragment;
import com.example.lotteryeventsystem.R;
import com.example.lotteryeventsystem.data.EventRepository;
import com.example.lotteryeventsystem.data.RepositoryCallback;
import com.example.lotteryeventsystem.di.ServiceLocator;
import com.example.lotteryeventsystem.model.Event;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * Makes sure the detail screen reacts to repo data.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class EventDetailFragmentTest {
    private FakeEventRepository fakeRepository;

    @Before
    public void setup() {
        fakeRepository = new FakeEventRepository();
        ServiceLocator.setEventRepository(fakeRepository);
    }

    @After
    public void tearDown() {
        ServiceLocator.reset();
    }

    @Test
    public void showsEventInfoWhenFound() {
        Event event = new Event();
        event.setId("event1");
        event.setTitle("Pool Party");
        event.setDescription("Bring your own floaties.");
        event.setLocation("Downtown pool");
        event.setRegistrationOpen(Timestamp.now());
        event.setRegistrationClose(Timestamp.now());
        fakeRepository.seedEvent(event);

        launchFragment("event1");

        onView(withId(R.id.event_title))
                .check(matches(withText("Pool Party")));
        onView(withId(R.id.event_description))
                .check(matches(withText("Bring your own floaties.")));
        onView(withId(R.id.event_location))
                .check(matches(withText("Downtown pool")));
        onView(withId(R.id.event_registration_window))
                .check(matches(withText(not(isEmptyString()))));
    }

    @Test
    public void showsNotFoundMessageWhenMissing() {
        launchFragment("missing-event");

        onView(withId(R.id.event_message))
                .check(matches(withText(R.string.event_detail_not_found)))
                .check(matches(isDisplayed()));
    }

    private void launchFragment(String eventId) {
        Bundle args = new Bundle();
        args.putString(EventDetailFragment.ARG_EVENT_ID, eventId);
        FragmentScenario.launchInContainer(
                EventDetailFragment.class,
                args,
                R.style.Theme_LotteryEventSystem,
                null);
    }

    private static class FakeEventRepository implements EventRepository {
        private final Map<String, Event> events = new HashMap<>();

        void seedEvent(Event event) {
            events.put(event.getId(), event);
        }

        @Override
        public void getEventById(String eventId, RepositoryCallback<Event> callback) {
            callback.onComplete(events.get(eventId), null);
        }
    }
}
