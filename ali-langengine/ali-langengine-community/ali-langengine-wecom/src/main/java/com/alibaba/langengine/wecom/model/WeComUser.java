package com.alibaba.langengine.wecom.model;

import java.util.List;

public class WeComUser {
    
    /**
     * 用户id
     */
    private String userId;
    
    /**
     * 用户姓名
     */
    private String name;
    
    /**
     * 用户别名
     */
    private String alias;
    
    /**
     * 手机号码
     */
    private String mobile;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 用户所属部门id列表
     */
    private List<Integer> department;
    
    /**
     * 部门内的排序值，默认为0，数值越大排序越前面
     */
    private List<Integer> order;
    
    /**
     * 职务信息
     */
    private String position;
    
    /**
     * 性别。0表示未定义，1表示男性，2表示女性
     */
    private Integer gender;
    
    /**
     * 座机号码
     */
    private String telephone;
    
    /**
     * 个人微信号
     */
    private String weixinId;
    
    /**
     * 头像url
     */
    private String avatar;
    
    /**
     * 激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业
     */
    private Integer status;
    
    /**
     * 启用/禁用成员。1表示启用成员，0表示禁用成员
     */
    private Integer enable;
    
    /**
     * 隐藏手机号后四位：0-明文显示手机号，1-隐藏手机号后4位
     */
    private Integer hideMobile;
    
    /**
     * 成员对外属性
     */
    private Object extattr;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 全局唯一。对于同一个服务商，不同应用获取到企业内同一个成员的open_userid是相同的，最多64个字节。
     */
    private String openUserId;
    
    /**
     * 主部门
     */
    private Integer mainDepartment;
    
    public WeComUser() {}
    
    public WeComUser(String userId, String name, List<Integer> department) {
        this.userId = userId;
        this.name = name;
        this.department = department;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public List<Integer> getDepartment() { return department; }
    public void setDepartment(List<Integer> department) { this.department = department; }
    
    public List<Integer> getOrder() { return order; }
    public void setOrder(List<Integer> order) { this.order = order; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getWeixinId() { return weixinId; }
    public void setWeixinId(String weixinId) { this.weixinId = weixinId; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Integer getEnable() { return enable; }
    public void setEnable(Integer enable) { this.enable = enable; }
    
    public Integer getHideMobile() { return hideMobile; }
    public void setHideMobile(Integer hideMobile) { this.hideMobile = hideMobile; }
    
    public Object getExtattr() { return extattr; }
    public void setExtattr(Object extattr) { this.extattr = extattr; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getOpenUserId() { return openUserId; }
    public void setOpenUserId(String openUserId) { this.openUserId = openUserId; }
    
    public Integer getMainDepartment() { return mainDepartment; }
    public void setMainDepartment(Integer mainDepartment) { this.mainDepartment = mainDepartment; }
}