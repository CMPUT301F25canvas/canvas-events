package com.example.lotteryeventsystem.data;

import androidx.annotation.Nullable;

/**
 * Simple callback so async repo calls can report back.
 * It keeps things flexible for tests that use fake data.
 *
 * @param <T> the type of result returned on success
 */
public interface RepositoryCallback<T> {
    /**
     * Called when the request finishes.
     *
     * @param data result or {@code null} when nothing is available
     * @param error error if something went wrong, otherwise {@code null}
     */
    void onComplete(@Nullable T data, @Nullable Exception error);
}
