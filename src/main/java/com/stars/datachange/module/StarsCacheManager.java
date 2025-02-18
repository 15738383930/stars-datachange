package com.stars.datachange.module;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.stars.datachange.autoconfigure.StarsProperties;
import com.stars.datachange.utils.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存模块管理器
 * @author Hao.
 * @version 2.0
 * @since 2025/2/13 15:41
 */
public class StarsCacheManager implements CacheManager {

    private final DataDictionary dataDictionary;

    public StarsCacheManager(DataDictionary dataDictionary) {
        this.dataDictionary = dataDictionary;
    }

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

    @Override
    public Cache getCache(String name) {
        Cache cache = cacheMap.get(name);
        if (cache != null) {
            return cache;
        }
        if (!StarsProperties.cache.isDynamic() && !StarsProperties.cache.getCacheNames().contains(name)) {
            return cache;
        }

        cache = new StarsCache(name, guavaCache(name));
        Cache oldCache = cacheMap.putIfAbsent(name, cache);
        return oldCache == null ? cache : oldCache;
    }

    public com.google.common.cache.Cache<Object, Object> guavaCache(String cacheName) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        long expireAfterAccess = StarsProperties.cache.getExpireAfterAccess();
        Map<String, Long> expires = StarsProperties.cache.getExpires();
        Long cacheNameExpire = expires.get(cacheName);
        long expire = cacheNameExpire == null ? expireAfterAccess : cacheNameExpire;

        if (expire > 0) {
            cacheBuilder.expireAfterAccess(expire, TimeUnit.SECONDS);
        }

        if (StarsProperties.cache.getExpireAfterWrite() > 0) {
            cacheBuilder.expireAfterWrite(StarsProperties.cache.getExpireAfterWrite(), TimeUnit.SECONDS);
        }

        int initialCapacity = StarsProperties.cache.getInitialCapacity();
        Map<String, Integer> capacityMap = StarsProperties.cache.getCapacityMap();

        Integer capacity = capacityMap.get(cacheName);

        int capacityResult = capacity == null ? initialCapacity : capacity;

        if (capacityResult > 0) {
            cacheBuilder.initialCapacity(capacityResult);
        }

        if (StarsProperties.cache.getMaximumSize() > 0) {
            cacheBuilder.maximumSize(StarsProperties.cache.getMaximumSize());
        }

        if (StarsProperties.cache.getRefreshAfterWrite() > 0) {
            // 写入后刷新逻辑只针对stars-datachange
            if ("stars".equals(cacheName)) {
                return cacheBuilder.refreshAfterWrite(StarsProperties.cache.getRefreshAfterWrite(), TimeUnit.SECONDS)
                    .build(new CacheLoader<Object, Object>() {
                        @Override
                        public Object load(Object key) {
                            String s = key.toString().replace("dictionary::", "");
                            if (StringUtils.isEmpty(s)) {
                                s = null;
                            }
                            // 这里可以加载数据，例如从数据库查询或API调用
                            return dataDictionary.dataDictionary(s);
                        }
                    });
            }
        }
        return cacheBuilder.build();
    }

    @Override
    public Collection<String> getCacheNames() {
        return StarsProperties.cache.getCacheNames();
    }
}
