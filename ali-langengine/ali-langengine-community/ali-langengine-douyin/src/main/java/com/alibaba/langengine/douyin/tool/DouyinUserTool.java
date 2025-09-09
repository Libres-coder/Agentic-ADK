package com.alibaba.langengine.douyin.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.douyin.model.DouyinUser;
import com.alibaba.langengine.douyin.sdk.DouyinClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 抖音用户信息工具
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class DouyinUserTool extends DefaultTool {
    
    private DouyinClient douyinClient;
    
    public DouyinUserTool() {
        this.douyinClient = new DouyinClient();
        init();
    }
    
    private void init() {
        setName("DouyinUserTool");
        setDescription("抖音用户信息工具，可以获取抖音用户的详细信息和视频列表。输入参数：userId(用户ID), includeVideos(是否包含视频列表，默认false), videoCount(视频数量，默认5)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"userId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"抖音用户ID\"\n" +
                "    },\n" +
                "    \"includeVideos\": {\n" +
                "      \"type\": \"boolean\",\n" +
                "      \"description\": \"是否包含视频列表，默认false\",\n" +
                "      \"default\": false\n" +
                "    },\n" +
                "    \"videoCount\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"视频数量，默认5\",\n" +
                "      \"default\": 5\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"userId\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("抖音用户信息工具输入: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String userId = (String) inputMap.get("userId");
            Boolean includeVideos = (Boolean) inputMap.getOrDefault("includeVideos", false);
            Integer videoCount = (Integer) inputMap.getOrDefault("videoCount", 5);
            
            if (StringUtils.isBlank(userId)) {
                return new ToolExecuteResult("错误：用户ID不能为空");
            }
            
            if (videoCount == null || videoCount <= 0) {
                videoCount = 5;
            }
            
            // 获取用户信息
            DouyinUser user = douyinClient.getUserInfo(userId);
            
            StringBuilder result = new StringBuilder();
            result.append("用户信息：\n");
            result.append("昵称：").append(user.getNickname()).append("\n");
            result.append("用户ID：").append(user.getUserId()).append("\n");
            result.append("个性签名：").append(user.getSignature()).append("\n");
            result.append("头像：").append(user.getAvatarUrl()).append("\n");
            result.append("短ID：").append(user.getShortId()).append("\n");
            result.append("唯一ID：").append(user.getUniqueId()).append("\n");
            result.append("粉丝数：").append(user.getFollowerCount()).append("\n");
            result.append("关注数：").append(user.getFollowingCount()).append("\n");
            result.append("作品数：").append(user.getAwemeCount()).append("\n");
            result.append("获赞总数：").append(user.getTotalFavorited()).append("\n");
            result.append("性别：").append(user.getGender()).append("\n");
            result.append("生日：").append(user.getBirthday()).append("\n");
            result.append("所在地：").append(user.getLocation()).append("\n");
            result.append("学校：").append(user.getSchool()).append("\n");
            result.append("企业：").append(user.getEnterprise()).append("\n");
            result.append("是否认证：").append(user.getIsVerified() ? "是" : "否").append("\n");
            if (user.getIsVerified()) {
                result.append("认证类型：").append(user.getVerificationType()).append("\n");
                result.append("认证信息：").append(user.getVerificationInfo()).append("\n");
                result.append("自定义认证：").append(user.getCustomVerify()).append("\n");
            }
            result.append("星座：").append(user.getConstellation()).append("\n");
            
            if (user.getIsLive()) {
                result.append("正在直播：是\n");
                result.append("直播间ID：").append(user.getRoomId()).append("\n");
                result.append("直播标题：").append(user.getLiveTitle()).append("\n");
                result.append("观看人数：").append(user.getLiveViewerCount()).append("\n");
                result.append("直播链接：").append(user.getLiveUrl()).append("\n");
            } else {
                result.append("正在直播：否\n");
            }
            
            // 获取用户视频列表
            if (includeVideos) {
                result.append("\n--- 最新视频列表 ---\n");
                try {
                    var videoResponse = douyinClient.getUserVideos(userId, videoCount);
                    if (videoResponse.getVideos() != null && !videoResponse.getVideos().isEmpty()) {
                        for (int i = 0; i < videoResponse.getVideos().size(); i++) {
                            var video = videoResponse.getVideos().get(i);
                            result.append("\n视频 ").append(i + 1).append("：\n");
                            result.append("视频ID：").append(video.getAwemeId()).append("\n");
                            result.append("描述：").append(video.getDesc()).append("\n");
                            result.append("播放数：").append(video.getPlayCount()).append("\n");
                            result.append("点赞数：").append(video.getDiggCount()).append("\n");
                            result.append("评论数：").append(video.getCommentCount()).append("\n");
                            result.append("分享数：").append(video.getShareCount()).append("\n");
                            result.append("时长：").append(video.getDuration()).append("秒\n");
                            result.append("---\n");
                        }
                    } else {
                        result.append("暂无视频\n");
                    }
                } catch (Exception e) {
                    result.append("获取视频列表失败：").append(e.getMessage()).append("\n");
                }
            }
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("获取抖音用户信息失败", e);
            return new ToolExecuteResult("获取用户信息失败: " + e.getMessage());
        }
    }
}