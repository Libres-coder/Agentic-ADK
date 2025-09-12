/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.computer.use.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("ali.adk.browser.use.properties")
public class AdkBrowserUseProperties {

    private String enable;

    private String enableWuying;

    private String computerResourceId;

    private String mobileResourceId;

    private String ak;

    private String sk;

    private String endpoint;

    private String mobileEndPoint;

    private String appStreamEndPoint;

    @Value("#{'${ali.adk.browser.use.properties.endpoints}'.split(',')}")
    private List<String> endpoints;

    private String path;

    private String userId;

    private String regionId;

    private String password;

    private String officeSiteId;

    private String instanceGroupId;

    public String getComputerResourceId() {
        return computerResourceId;
    }

    public void setComputerResourceId(String computerResourceId) {
        this.computerResourceId = computerResourceId;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getEnableWuying() {
        return enableWuying;
    }

    public void setEnableWuying(String enableWuying) {
        this.enableWuying = enableWuying;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOfficeSiteId() {
        return officeSiteId;
    }

    public void setOfficeSiteId(String officeSiteId) {
        this.officeSiteId = officeSiteId;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getInstanceGroupId() {
        return instanceGroupId;
    }

    public void setInstanceGroupId(String instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }

    public String getMobileEndPoint() {
        return mobileEndPoint;
    }

    public void setMobileEndPoint(String mobileEndPoint) {
        this.mobileEndPoint = mobileEndPoint;
    }

    public String getAppStreamEndPoint() {
        return appStreamEndPoint;
    }

    public void setAppStreamEndPoint(String appStreamEndPoint) {
        this.appStreamEndPoint = appStreamEndPoint;
    }

    public String getMobileResourceId() {
        return mobileResourceId;
    }

    public void setMobileResourceId(String mobileResourceId) {
        this.mobileResourceId = mobileResourceId;
    }
}
