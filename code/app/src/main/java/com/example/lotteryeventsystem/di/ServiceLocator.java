package com.example.lotteryeventsystem.di;

import androidx.annotation.VisibleForTesting;

import com.example.lotteryeventsystem.data.EventRepository;
import com.example.lotteryeventsystem.data.FirebaseEventRepository;
import com.example.lotteryeventsystem.data.FirebaseWaitlistRepository;
import com.example.lotteryeventsystem.data.NotificationRepository;
import com.example.lotteryeventsystem.data.WaitlistRepository;
import com.example.lotteryeventsystem.service.WaitlistService;

/**
 * Tiny service locator so UI code can grab shared singletons.
 * Lets tests swap in fake repos without touching production code.
 */
public final class ServiceLocator {
    private static EventRepository eventRepository = new FirebaseEventRepository();
    private static WaitlistRepository waitlistRepository = new FirebaseWaitlistRepository();
    private static WaitlistService waitlistService = new WaitlistService(waitlistRepository);
    private static NotificationRepository notificationRepository = new NotificationRepository();

    private ServiceLocator() {
    }

    public static EventRepository provideEventRepository() {
        return eventRepository;
    }

    public static WaitlistRepository provideWaitlistRepository() {
        return waitlistRepository;
    }

    public static WaitlistService provideWaitlistService() {
        return waitlistService;
    }

    public static NotificationRepository provideNotificationRepository() {
        return notificationRepository;
    }

    @VisibleForTesting
    public static void setEventRepository(EventRepository repository) {
        eventRepository = repository;
    }

    @VisibleForTesting
    public static void setWaitlistRepository(WaitlistRepository repository) {
        waitlistRepository = repository;
        waitlistService = new WaitlistService(waitlistRepository);
    }

    @VisibleForTesting
    public static void reset() {
        eventRepository = new FirebaseEventRepository();
        waitlistRepository = new FirebaseWaitlistRepository();
        waitlistService = new WaitlistService(waitlistRepository);
        notificationRepository = new NotificationRepository();
    }
}
