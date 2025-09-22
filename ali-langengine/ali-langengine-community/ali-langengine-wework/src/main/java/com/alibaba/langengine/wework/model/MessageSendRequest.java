package com.alibaba.langengine.wework.model;

import lombok.Data;

@Data
public class MessageSendRequest {
    private String touser;
    private String toparty;
    private String totag;
    private String msgtype;
    private int agentid;
    private TextMessage text;
    private ImageMessage image;
    private VoiceMessage voice;
    private VideoMessage video;
    private FileMessage file;
    private TextcardMessage textcard;
    private NewsMessage news;
    private MpnewsMessage mpnews;
    private MarkdownMessage markdown;
    private MiniprogramNoticeMessage miniprogram_notice;
    private int safe;
    private int enable_id_trans;
    private int enable_duplicate_check;
    private int duplicate_check_interval;
}
