package de.viadee.sonarissuescoring.service.prediction.load;

import org.springframework.stereotype.Component;

import javax.annotation.concurrent.GuardedBy;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Caches strings in memory, using weak keys and values
 * Guava Cache with weak keys and values is insufficient because it uses object equality
 */
@Component
public class StringCache {
    @GuardedBy("this") private final WeakHashMap<String, WeakReference<String>> cache = new WeakHashMap<>();

    public synchronized String deduplicate(String in) {
        WeakReference<String> ref = cache.computeIfAbsent(in, WeakReference::new);
        String value = ref.get();
        if (value != null)
            return value;
        //GC got the object after we got the reference
        cache.put(in, new WeakReference<>(in));
        return in;
    }
}
