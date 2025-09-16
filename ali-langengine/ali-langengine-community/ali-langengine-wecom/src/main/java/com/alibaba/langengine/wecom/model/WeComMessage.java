package com.alibaba.langengine.wecom.model;

public class WeComMessage {
    
    /**
     * 指定接收消息的成员，成员ID列表（多个接收者用'|'分隔，最多支持1000个）
     * 特殊情况：指定为"@all"，则向该企业应用的全部成员发送
     */
    private String toUser;
    
    /**
     * 指定接收消息的部门，部门ID列表，多个接收者用'|'分隔，最多支持100个
     */
    private String toParty;
    
    /**
     * 指定接收消息的标签，标签ID列表，多个接收者用'|'分隔，最多支持100个
     */
    private String toTag;
    
    /**
     * 消息类型
     */
    private String msgType;
    
    /**
     * 企业应用的id，整型。企业内部开发，可在应用的设置页面查看
     */
    private String agentId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 链接URL
     */
    private String url;
    
    /**
     * 按钮文字
     */
    private String btnTxt;
    
    /**
     * 媒体文件id，可以调用上传临时素材接口获取
     */
    private String mediaId;
    
    /**
     * 表示是否是保密消息，0表示可对外分享，1表示不能分享且内容显示水印，默认为0
     */
    private String safe = "0";
    
    /**
     * 表示是否开启id转译，0表示否，1表示是，默认0。仅第三方应用需要用到，企业自建应用可以忽略。
     */
    private String enableIdTrans = "0";
    
    /**
     * 表示是否开启重复消息检查，0表示否，1表示是，默认0
     */
    private String enableDuplicateCheck = "0";
    
    /**
     * 表示是否重复消息检查的时间间隔，默认1800s，最大不超过4小时
     */
    private Integer duplicateCheckInterval = 1800;
    
    public WeComMessage() {}
    
    public WeComMessage(String toUser, String msgType, String content, String agentId) {
        this.toUser = toUser;
        this.msgType = msgType;
        this.content = content;
        this.agentId = agentId;
    }
    
    // Getters and Setters
    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }
    
    public String getToParty() { return toParty; }
    public void setToParty(String toParty) { this.toParty = toParty; }
    
    public String getToTag() { return toTag; }
    public void setToTag(String toTag) { this.toTag = toTag; }
    
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getBtnTxt() { return btnTxt; }
    public void setBtnTxt(String btnTxt) { this.btnTxt = btnTxt; }
    
    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
    
    public String getSafe() { return safe; }
    public void setSafe(String safe) { this.safe = safe; }
    
    public String getEnableIdTrans() { return enableIdTrans; }
    public void setEnableIdTrans(String enableIdTrans) { this.enableIdTrans = enableIdTrans; }
    
    public String getEnableDuplicateCheck() { return enableDuplicateCheck; }
    public void setEnableDuplicateCheck(String enableDuplicateCheck) { this.enableDuplicateCheck = enableDuplicateCheck; }
    
    public Integer getDuplicateCheckInterval() { return duplicateCheckInterval; }
    public void setDuplicateCheckInterval(Integer duplicateCheckInterval) { this.duplicateCheckInterval = duplicateCheckInterval; }
}