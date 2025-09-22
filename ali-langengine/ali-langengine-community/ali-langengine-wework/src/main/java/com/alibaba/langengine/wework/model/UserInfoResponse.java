package com.alibaba.langengine.wework.model;

import lombok.Data;
import java.util.List;

@Data
public class UserInfoResponse {
    private int errcode;
    private String errmsg;
    private String userid;
    private String name;
    private String mobile;
    private String email;
    private String position;
    private String avatar;
    private String thumb_avatar;
    private String telephone;
    private String alias;
    private String address;
    private String open_userid;
    private String main_department;
    private List<Integer> department;
    private List<Integer> order;
    private List<Integer> is_leader_in_dept;
    private String extattr;
    private String to_invite;
    private String external_position;
    private String external_profile;
    private String gender;
    private String qr_code;
    private String status;
    private String english_name;
    private String hide_mobile;
    private String senior;
    private String isleader;
    private String direct_leader;
}