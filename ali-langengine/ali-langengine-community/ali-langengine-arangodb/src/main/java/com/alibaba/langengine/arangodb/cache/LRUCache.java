/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.arangodb.cache;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class LRUCache<K, V> {
    
    private final int capacity;
    private final Map<K, Node<K, V>> cache;
    private final Node<K, V> head;
    private final Node<K, V> tail;
    private final ReadWriteLock lock;
    private volatile int size;
    
    /**
     * 双向链表节点
     */
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;
        
        Node() {
            this(null, null);
        }
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    /**
     * 构造函数
     * 
     * @param capacity 缓存容量
     */
    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        
        this.capacity = capacity;
        this.cache = new HashMap<>(capacity);
        this.lock = new ReentrantReadWriteLock();
        this.size = 0;
        
        // 初始化双向链表
        this.head = new Node<>();
        this.tail = new Node<>();
        head.next = tail;
        tail.prev = head;
    }
    
    /**
     * 获取缓存值
     * 
     * @param key 键
     * @return 值，如果不存在返回 null
     */
    public V get(K key) {
        if (key == null) {
            return null;
        }
        
        lock.writeLock().lock();
        try {
            Node<K, V> node = cache.get(key);
            if (node == null) {
                return null;
            }
            
            // 移动到头部（最近使用）
            moveToHead(node);
            return node.value;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 放入缓存
     * 
     * @param key 键
     * @param value 值
     * @return 如果键已存在，返回旧值；否则返回 null
     */
    public V put(K key, V value) {
        if (key == null || value == null) {
            return null;
        }
        
        lock.writeLock().lock();
        try {
            Node<K, V> node = cache.get(key);
            
            if (node != null) {
                // 更新现有节点
                V oldValue = node.value;
                node.value = value;
                moveToHead(node);
                return oldValue;
            } else {
                // 创建新节点
                Node<K, V> newNode = new Node<>(key, value);
                
                if (size >= capacity) {
                    // 移除尾部节点（最少使用）
                    Node<K, V> tailNode = removeTail();
                    cache.remove(tailNode.key);
                    size--;
                }
                
                // 添加到头部
                addToHead(newNode);
                cache.put(key, newNode);
                size++;
                return null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 移除缓存项
     * 
     * @param key 键
     * @return 被移除的值，如果不存在返回 null
     */
    public V remove(K key) {
        if (key == null) {
            return null;
        }
        
        lock.writeLock().lock();
        try {
            Node<K, V> node = cache.get(key);
            if (node == null) {
                return null;
            }
            
            // 从链表中移除
            removeNode(node);
            cache.remove(key);
            size--;
            
            return node.value;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 检查是否包含键
     * 
     * @param key 键
     * @return 是否包含
     */
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }
        
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 当前缓存大小
     */
    public int size() {
        lock.readLock().lock();
        try {
            return size;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 检查缓存是否为空
     * 
     * @return 是否为空
     */
    public boolean isEmpty() {
        return size() == 0;
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            head.next = tail;
            tail.prev = head;
            size = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取所有键
     * 
     * @return 键集合
     */
    public Set<K> keySet() {
        lock.readLock().lock();
        try {
            return new HashSet<>(cache.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取所有值
     * 
     * @return 值集合
     */
    public Collection<V> values() {
        lock.readLock().lock();
        try {
            List<V> values = new ArrayList<>(size);
            for (Node<K, V> node = head.next; node != tail; node = node.next) {
                values.add(node.value);
            }
            return values;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 如果指定的键尚未与值关联，则尝试使用给定的映射函数计算其值，并将其输入到此映射中，除非为 null。
     * 
     * @param key 键
     * @param mappingFunction 映射函数
     * @return 与指定键关联的值
     */
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) {
            return null;
        }
        
        lock.writeLock().lock();
        try {
            Node<K, V> node = cache.get(key);
            if (node != null) {
                moveToHead(node);
                return node.value;
            }
            
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                put(key, newValue);
            }
            return newValue;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getStats() {
        lock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("size", size);
            stats.put("capacity", capacity);
            stats.put("usage", (double) size / capacity);
            stats.put("isEmpty", isEmpty());
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 将节点移动到头部
     */
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }
    
    /**
     * 添加节点到头部
     */
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    /**
     * 移除节点
     */
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    /**
     * 移除尾部节点
     */
    private Node<K, V> removeTail() {
        Node<K, V> last = tail.prev;
        removeNode(last);
        return last;
    }
    
    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("LRUCache{size=").append(size)
              .append(", capacity=").append(capacity)
              .append(", entries=[");
            
            Node<K, V> node = head.next;
            boolean first = true;
            while (node != tail) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(node.key).append("=").append(node.value);
                node = node.next;
                first = false;
            }
            
            sb.append("]}");
            return sb.toString();
        } finally {
            lock.readLock().unlock();
        }
    }
}
