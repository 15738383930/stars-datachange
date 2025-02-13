package com.stars.datachange.module;

import com.google.common.cache.Cache;
import com.stars.datachange.autoconfigure.StarsProperties;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 缓存模块
 * @author Hao.
 * @version 1.0
 * @since 2025/2/13 15:42
 */
public class StarsCache extends AbstractValueAdaptingCache {

    private String name;

    private final Cache<Object, Object> loadingCache;

    private final Map<String, ReentrantLock> keyLockMap = new ConcurrentHashMap<>();

    public StarsCache(String name, Cache<Object, Object> loadingCache) {
        super(StarsProperties.cache.isCacheNullValues());
        this.name = name;
        this.loadingCache = loadingCache;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) value;
        }

        ReentrantLock lock = keyLockMap.get(key.toString());
        if (lock == null) {
            lock = new ReentrantLock();
            keyLockMap.putIfAbsent(key.toString(), lock);
        }
        try {
            lock.lock();
            value = lookup(key);
            if (value != null) {
                return (T) value;
            }
            value = valueLoader.call();
            Object storeValue = toStoreValue(value);
            put(key, storeValue);
            return (T) value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e.getCause());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (!super.isAllowNullValues() && value == null) {
            this.evict(key);
            return;
        }
        loadingCache.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Object prevValue = null;
        synchronized (key) {
            loadingCache.put(key, toStoreValue(value));
        }
        return toValueWrapper(prevValue);
    }

    @Override
    public void evict(Object key) {
        loadingCache.invalidate(key);
    }

    @Override
    public void clear() {
        loadingCache.invalidateAll();
    }

    @Override
    protected Object lookup(Object key) {
        return loadingCache.getIfPresent(key);
    }
}