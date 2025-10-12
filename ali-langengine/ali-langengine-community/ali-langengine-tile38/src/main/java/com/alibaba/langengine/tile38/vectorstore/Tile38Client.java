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
package com.alibaba.langengine.tile38.vectorstore;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;


@Slf4j
public class Tile38Client {

    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;

    public Tile38Client(Tile38Param param) {
        try {
            RedisURI.Builder uriBuilder = RedisURI.Builder
                    .redis(param.getHost(), param.getPort())
                    .withTimeout(Duration.ofMillis(param.getTimeout()));

            if (StringUtils.isNotEmpty(param.getPassword())) {
                uriBuilder.withPassword(param.getPassword().toCharArray());
            }

            RedisURI redisURI = uriBuilder.build();
            this.redisClient = RedisClient.create(redisURI);
            this.connection = redisClient.connect();
            this.commands = connection.sync();

            log.info("Tile38 client connected to {}:{}", param.getHost(), param.getPort());
        } catch (Exception e) {
            throw new Tile38Exception("CONNECT_ERROR", "Failed to connect to Tile38 server", e);
        }
    }

    /**
     * Set a point in a collection
     */
    public String set(String collection, String id, double lat, double lon, Map<String, String> fields) {
        try {
            StringBuilder cmd = new StringBuilder();
            cmd.append("SET ").append(collection).append(" ").append(id);
            
            if (fields != null && !fields.isEmpty()) {
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    cmd.append(" FIELD ").append(entry.getKey()).append(" ").append(entry.getValue());
                }
            }
            
            cmd.append(" POINT ").append(lat).append(" ").append(lon);
            
            return commands.eval(cmd.toString(), null);
        } catch (Exception e) {
            throw new Tile38Exception("SET_ERROR", "Failed to set point in collection: " + collection, e);
        }
    }

    /**
     * Set an object with vector data
     */
    public String setObject(String collection, String id, String geoJson, Map<String, String> fields) {
        try {
            StringBuilder cmd = new StringBuilder();
            cmd.append("SET ").append(collection).append(" ").append(id);
            
            if (fields != null && !fields.isEmpty()) {
                for (Map.Entry<String, String> entry : fields.entrySet()) {
                    cmd.append(" FIELD ").append(entry.getKey()).append(" ").append(entry.getValue());
                }
            }
            
            cmd.append(" OBJECT ").append(geoJson);
            
            return commands.eval(cmd.toString(), null);
        } catch (Exception e) {
            throw new Tile38Exception("SET_OBJECT_ERROR", "Failed to set object in collection: " + collection, e);
        }
    }

    /**
     * Search nearby points
     */
    public List<Object> nearby(String collection, double lat, double lon, int limit) {
        try {
            String cmd = String.format("NEARBY %s POINT %f %f LIMIT %d", collection, lat, lon, limit);
            return (List<Object>) commands.eval(cmd, null);
        } catch (Exception e) {
            throw new Tile38Exception("NEARBY_ERROR", "Failed to search nearby in collection: " + collection, e);
        }
    }

    /**
     * Search within a radius
     */
    public List<Object> within(String collection, double lat, double lon, double radius, int limit) {
        try {
            String cmd = String.format("WITHIN %s CIRCLE %f %f %f LIMIT %d", collection, lat, lon, radius, limit);
            return (List<Object>) commands.eval(cmd, null);
        } catch (Exception e) {
            throw new Tile38Exception("WITHIN_ERROR", "Failed to search within radius in collection: " + collection, e);
        }
    }

    /**
     * Get an object by id
     */
    public Object get(String collection, String id) {
        try {
            String cmd = String.format("GET %s %s", collection, id);
            return commands.eval(cmd, null);
        } catch (Exception e) {
            throw new Tile38Exception("GET_ERROR", "Failed to get object from collection: " + collection, e);
        }
    }

    /**
     * Delete an object by id
     */
    public String del(String collection, String id) {
        try {
            String cmd = String.format("DEL %s %s", collection, id);
            return commands.eval(cmd, null);
        } catch (Exception e) {
            throw new Tile38Exception("DELETE_ERROR", "Failed to delete object from collection: " + collection, e);
        }
    }

    /**
     * Drop a collection
     */
    public String drop(String collection) {
        try {
            String cmd = String.format("DROP %s", collection);
            return commands.eval(cmd, null);
        } catch (Exception e) {
            throw new Tile38Exception("DROP_ERROR", "Failed to drop collection: " + collection, e);
        }
    }

    /**
     * Execute raw command
     */
    public Object execute(String command) {
        try {
            return commands.eval(command, null);
        } catch (Exception e) {
            throw new Tile38Exception("EXECUTE_ERROR", "Failed to execute command: " + command, e);
        }
    }

    /**
     * Close the client connection
     */
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
            if (redisClient != null) {
                redisClient.shutdown();
            }
            log.info("Tile38 client connection closed");
        } catch (Exception e) {
            log.error("Error closing Tile38 client", e);
        }
    }

}