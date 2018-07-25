package de.viadee.sonarIssueScoring.service.misc;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Ensures that only a single instance is run concurrently, and only projects for different keys can be queued.
 */
@Component
public class ParallelismManager {
    private final Set<String> waitingOrRunningProjects = new HashSet<>();

    /**
     * Runs the given supplier eventually, if there isn't one already waiting for the same key.
     *
     * If the given key is already waiting / running, doesn't run and returns an empty optional
     * <p>
     * Otherwise waits for other suppliers to finish, to run the supplied one eventually
     *
     * @param key project id to lock by
     * @return result of the operation, if it was run
     */
    public <T> Optional<T> runIfNotAlreadyWaiting(String key, Supplier<T> code) {
        synchronized (waitingOrRunningProjects) {
            if (!waitingOrRunningProjects.add(key)) {
                return Optional.empty(); //Bail out if the key is already running / waiting
            }
        }

        synchronized (this) {
            try {
                return Optional.of(code.get()); //Execute actual long-running computation
            } finally {
                //noinspection NestedSynchronizedStatement nested locks are fine, the inner lock is very short-lived
                synchronized (waitingOrRunningProjects) {
                    waitingOrRunningProjects.remove(key); //Remove from waiting list
                }
            }
        }
    }

    /** same as runIfNotAlreadyWaiting, but returns appropriate HTTP results */
    public <T> ResponseEntity<T> runIfNotAlreadyWaitingAsHttp(String key, Supplier<T> code) {
        Optional<T> res = runIfNotAlreadyWaiting(key, code);
        return res.map(ResponseEntity::ok).orElse(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
    }
}


