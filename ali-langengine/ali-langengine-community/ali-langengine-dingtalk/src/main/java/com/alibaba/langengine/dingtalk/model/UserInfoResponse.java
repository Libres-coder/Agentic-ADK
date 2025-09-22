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
package com.alibaba.langengine.dingtalk.model;

import lombok.Data;

/**
 * 用户信息响应
 * 
 * @author langengine
 */
@Data
public class UserInfoResponse {
    
    private int errcode;
    private String errmsg;
    private UserInfo result;
    
    @Data
    public static class UserInfo {
        private String userid;
        private String name;
        private String mobile;
        private String email;
        private String position;
        private String avatar;
        private String jobnumber;
        private String deptIdList;
        private String deptOrderList;
        private String extension;
        private String hiredDate;
        private String workPlace;
        private String remark;
        private String loginEmail;
        private String orgEmail;
        private String stateCode;
        private String managerUserid;
        private String leaderInDept;
        private String unionid;
        private String admin;
        private String boss;
        private String exclusiveAccount;
        private String loginId;
        private String exclusiveAccountType;
        private String loginIdType;
        private String hideMobile;
        private String senior;
        private String realAuthed;
        private String orgEmailType;
        private String nickName;
        private String title;
        private String workPlaceCode;
        private String deptIdListV2;
        private String deptOrderListV2;
        private String leaderInDeptV2;
    }
}
