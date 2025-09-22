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
package com.alibaba.langengine.supabase;

import lombok.Data;

/**
 * Supabase 配置类
 * 
 * @author langengine
 */
@Data
public class SupabaseConfiguration {
    
    /**
     * Supabase项目URL
     */
    public static final String SUPABASE_URL = System.getProperty("supabase.url", "");
    
    /**
     * Supabase API密钥
     */
    public static final String SUPABASE_ANON_KEY = System.getProperty("supabase.anon.key", "");
    
    /**
     * Supabase服务角色密钥
     */
    public static final String SUPABASE_SERVICE_KEY = System.getProperty("supabase.service.key", "");
    
    /**
     * 数据库连接URL
     */
    public static final String SUPABASE_DB_URL = System.getProperty("supabase.db.url", "");
    
    /**
     * 数据库用户名
     */
    public static final String SUPABASE_DB_USER = System.getProperty("supabase.db.user", "");
    
    /**
     * 数据库密码
     */
    public static final String SUPABASE_DB_PASSWORD = System.getProperty("supabase.db.password", "");
    
    /**
     * 默认表名
     */
    public static final String SUPABASE_DEFAULT_TABLE = System.getProperty("supabase.table.name", "documents");
    
    /**
     * 向量维度
     */
    public static final int SUPABASE_VECTOR_DIM = Integer.parseInt(System.getProperty("supabase.vector.dim", "1536"));
    
    /**
     * 连接超时时间（秒）
     */
    public static final int SUPABASE_TIMEOUT = Integer.parseInt(System.getProperty("supabase.timeout", "30"));
    
    /**
     * 最大连接数
     */
    public static final int SUPABASE_MAX_CONNECTIONS = Integer.parseInt(System.getProperty("supabase.max.connections", "10"));
    
    /**
     * 批量插入大小
     */
    public static final int SUPABASE_BATCH_SIZE = Integer.parseInt(System.getProperty("supabase.batch.size", "100"));
    
    /**
     * 是否启用SSL
     */
    public static final boolean SUPABASE_SSL_ENABLED = Boolean.parseBoolean(System.getProperty("supabase.ssl.enabled", "true"));
    
    /**
     * 是否启用连接池
     */
    public static final boolean SUPABASE_POOL_ENABLED = Boolean.parseBoolean(System.getProperty("supabase.pool.enabled", "true"));
    
    /**
     * 连接池最小连接数
     */
    public static final int SUPABASE_POOL_MIN_SIZE = Integer.parseInt(System.getProperty("supabase.pool.min.size", "2"));
    
    /**
     * 连接池最大连接数
     */
    public static final int SUPABASE_POOL_MAX_SIZE = Integer.parseInt(System.getProperty("supabase.pool.max.size", "10"));
    
    /**
     * 连接池空闲超时时间（秒）
     */
    public static final int SUPABASE_POOL_IDLE_TIMEOUT = Integer.parseInt(System.getProperty("supabase.pool.idle.timeout", "300"));
    
    /**
     * 是否启用自动重连
     */
    public static final boolean SUPABASE_AUTO_RECONNECT = Boolean.parseBoolean(System.getProperty("supabase.auto.reconnect", "true"));
    
    /**
     * 重连间隔时间（秒）
     */
    public static final int SUPABASE_RECONNECT_INTERVAL = Integer.parseInt(System.getProperty("supabase.reconnect.interval", "5"));
    
    /**
     * 最大重连次数
     */
    public static final int SUPABASE_MAX_RECONNECT_ATTEMPTS = Integer.parseInt(System.getProperty("supabase.max.reconnect.attempts", "3"));
    
    /**
     * 是否启用查询缓存
     */
    public static final boolean SUPABASE_QUERY_CACHE_ENABLED = Boolean.parseBoolean(System.getProperty("supabase.query.cache.enabled", "false"));
    
    /**
     * 查询缓存大小
     */
    public static final int SUPABASE_QUERY_CACHE_SIZE = Integer.parseInt(System.getProperty("supabase.query.cache.size", "1000"));
    
    /**
     * 查询缓存过期时间（秒）
     */
    public static final int SUPABASE_QUERY_CACHE_TTL = Integer.parseInt(System.getProperty("supabase.query.cache.ttl", "300"));
    
    /**
     * 是否启用向量索引
     */
    public static final boolean SUPABASE_VECTOR_INDEX_ENABLED = Boolean.parseBoolean(System.getProperty("supabase.vector.index.enabled", "true"));
    
    /**
     * 向量索引类型
     */
    public static final String SUPABASE_VECTOR_INDEX_TYPE = System.getProperty("supabase.vector.index.type", "ivfflat");
    
    /**
     * 向量索引列表数量
     */
    public static final int SUPABASE_VECTOR_INDEX_LISTS = Integer.parseInt(System.getProperty("supabase.vector.index.lists", "100"));
    
    /**
     * 是否启用实时订阅
     */
    public static final boolean SUPABASE_REALTIME_ENABLED = Boolean.parseBoolean(System.getProperty("supabase.realtime.enabled", "false"));
    
    /**
     * 实时订阅频道
     */
    public static final String SUPABASE_REALTIME_CHANNEL = System.getProperty("supabase.realtime.channel", "documents");
}
