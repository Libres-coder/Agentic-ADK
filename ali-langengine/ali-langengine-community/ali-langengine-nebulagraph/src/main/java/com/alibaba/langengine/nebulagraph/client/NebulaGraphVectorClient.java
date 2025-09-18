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
package com.alibaba.langengine.nebulagraph.client;

import com.alibaba.langengine.nebulagraph.NebulaGraphConfiguration;
import com.alibaba.langengine.nebulagraph.exception.NebulaGraphVectorStoreException;
import com.alibaba.langengine.nebulagraph.vectorstore.NebulaGraphParam;
import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import com.vesoft.nebula.client.graph.net.Session;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Slf4j
@Data
public class NebulaGraphVectorClient implements Closeable {
    
    private final NebulaPool nebulaPool;
    private final String spaceName;
    private final String username;
    private final String password;
    private final NebulaGraphParam param;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    /**
     * 构造函数
     */
    public NebulaGraphVectorClient(NebulaGraphParam param) {
        this(null, param);
    }

    /**
     * 构造函数
     */
    public NebulaGraphVectorClient(String spaceName, NebulaGraphParam param) {
        this.spaceName = spaceName;
        this.param = param != null ? param : NebulaGraphParam.createDefault();
        this.username = NebulaGraphConfiguration.NEBULAGRAPH_USERNAME;
        this.password = NebulaGraphConfiguration.NEBULAGRAPH_PASSWORD;
        
        try {
            // 创建连接池配置
            NebulaPoolConfig poolConfig = new NebulaPoolConfig();
            poolConfig.setMaxConnSize(this.param.getInitParam().getConnectionPoolSize());
            poolConfig.setMinConnSize(1);
            poolConfig.setTimeout(this.param.getInitParam().getTimeoutMs());
            poolConfig.setIdleTime(this.param.getInitParam().getIdleTimeMs());
            poolConfig.setIntervalIdle(this.param.getInitParam().getHealthCheckIntervalMs());
            poolConfig.setWaitTime(0);
            poolConfig.setEnableSsl(this.param.getInitParam().isEnableSsl());
            
            // 创建连接池
            this.nebulaPool = new NebulaPool();
            
            // 初始化连接池
            List<HostAddress> addresses = Collections.singletonList(
                new HostAddress(NebulaGraphConfiguration.NEBULAGRAPH_GRAPH_HOST, 
                               NebulaGraphConfiguration.NEBULAGRAPH_GRAPH_PORT)
            );
            
            boolean initResult = nebulaPool.init(addresses, poolConfig);
            if (!initResult) {
                throw NebulaGraphVectorStoreException.connectionError(
                    "Failed to initialize NebulaGraph connection pool", null);
            }
            
            log.info("NebulaGraph client initialized successfully, space: {}, pool size: {}", 
                    spaceName, this.param.getInitParam().getConnectionPoolSize());
            
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.connectionError(
                "Failed to create NebulaGraph client", e);
        }
    }
    
    /**
     * 获取会话
     */
    public Session getSession() {
        lock.readLock().lock();
        try {
            if (closed.get()) {
                throw NebulaGraphVectorStoreException.connectionError(
                    "NebulaGraph client has been closed", null);
            }

            Session session = nebulaPool.getSession(username, password, false);
            if (session == null) {
                throw NebulaGraphVectorStoreException.sessionError(
                    "Failed to get session from connection pool", null);
            }

            return session;

        } catch (NebulaGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.sessionError(
                "Failed to get NebulaGraph session", e);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 释放会话
     */
    public void releaseSession(Session session) {
        if (session != null && !closed.get()) {
            try {
                session.release();
                log.debug("Released NebulaGraph session");
            } catch (Exception e) {
                log.warn("Failed to release NebulaGraph session", e);
            }
        }
    }
    
    /**
     * 检查连接健康状态
     */
    public boolean isHealthy() {
        lock.readLock().lock();
        try {
            if (closed.get()) {
                return false;
            }
            
            Session session = null;
            try {
                session = getSession();
                // 执行简单的健康检查查询
                ResultSet result = session.execute("SHOW HOSTS");
                return result.isSucceeded();
            } catch (Exception e) {
                log.warn("Health check failed", e);
                return false;
            } finally {
                releaseSession(session);
            }
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 获取连接池统计信息
     */
    public String getPoolStats() {
        if (closed.get()) {
            return "Client is closed";
        }
        
        try {
            return String.format("Active connections: %d, Max connections: %d",
                                0, // NebulaPool doesn't expose active connection count in this API version
                                param.getInitParam().getConnectionPoolSize());
        } catch (Exception e) {
            log.warn("Failed to get pool stats", e);
            return "Stats unavailable";
        }
    }
    
    /**
     * 重新连接
     */
    public void reconnect() {
        lock.writeLock().lock();
        try {
            if (closed.get()) {
                throw NebulaGraphVectorStoreException.connectionError(
                    "Cannot reconnect a closed client", null);
            }
            
            log.info("Reconnecting NebulaGraph client...");
            
            // 关闭现有连接池
            try {
                nebulaPool.close();
            } catch (Exception e) {
                log.warn("Error closing existing connection pool", e);
            }
            
            // 重新初始化连接池
            NebulaPoolConfig poolConfig = new NebulaPoolConfig();
            poolConfig.setMaxConnSize(param.getInitParam().getConnectionPoolSize());
            poolConfig.setMinConnSize(1);
            poolConfig.setTimeout(param.getInitParam().getTimeoutMs());
            poolConfig.setIdleTime(param.getInitParam().getIdleTimeMs());
            poolConfig.setIntervalIdle(param.getInitParam().getHealthCheckIntervalMs());
            poolConfig.setWaitTime(0);
            poolConfig.setEnableSsl(param.getInitParam().isEnableSsl());
            
            List<HostAddress> addresses = Collections.singletonList(
                new HostAddress(NebulaGraphConfiguration.NEBULAGRAPH_GRAPH_HOST,
                               NebulaGraphConfiguration.NEBULAGRAPH_GRAPH_PORT)
            );

            boolean initResult = nebulaPool.init(addresses, poolConfig);
            if (!initResult) {
                throw NebulaGraphVectorStoreException.connectionError(
                    "Failed to reconnect NebulaGraph client", null);
            }

            log.info("NebulaGraph client reconnected successfully");

        } catch (NebulaGraphVectorStoreException e) {
            throw e;
        } catch (Exception e) {
            throw NebulaGraphVectorStoreException.connectionError(
                "Failed to reconnect NebulaGraph client", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 执行带重试的操作
     */
    public <T> T executeWithRetry(RetryableOperation<T> operation) {
        int maxRetries = param.getInitParam().getRetryCount();
        int retryInterval = param.getInitParam().getRetryIntervalMs();
        
        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < maxRetries) {
                    log.warn("Operation failed on attempt {}/{}, retrying in {}ms", 
                            attempt + 1, maxRetries + 1, retryInterval, e);
                    
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw NebulaGraphVectorStoreException.unknownError(
                            "Operation interrupted during retry", ie);
                    }
                } else {
                    log.error("Operation failed after {} attempts", maxRetries + 1, e);
                }
            }
        }
        
        throw NebulaGraphVectorStoreException.unknownError(
            "Operation failed after " + (maxRetries + 1) + " attempts", lastException);
    }
    
    /**
     * 可重试操作接口
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * 关闭客户端
     */
    @Override
    public void close() throws IOException {
        lock.writeLock().lock();
        try {
            if (closed.compareAndSet(false, true)) {
                log.info("Closing NebulaGraph client...");
                
                try {
                    if (nebulaPool != null) {
                        nebulaPool.close();
                    }
                    log.info("NebulaGraph client closed successfully");
                } catch (Exception e) {
                    log.error("Error closing NebulaGraph client", e);
                    throw new IOException("Failed to close NebulaGraph client", e);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 检查客户端是否已关闭
     */
    public boolean isClosed() {
        return closed.get();
    }
    
    /**
     * 获取空间名称
     */
    public String getSpaceName() {
        return spaceName;
    }
    
    /**
     * 获取参数配置
     */
    public NebulaGraphParam getParam() {
        return param;
    }
}
