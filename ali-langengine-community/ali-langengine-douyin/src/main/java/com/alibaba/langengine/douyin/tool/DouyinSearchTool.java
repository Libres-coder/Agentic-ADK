package com.alibaba.langengine.douyin.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.douyin.model.DouyinSearchResponse;
import com.alibaba.langengine.douyin.model.DouyinVideo;
import com.alibaba.langengine.douyin.sdk.DouyinClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 抖音搜索工具
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class DouyinSearchTool extends DefaultTool {
    
    private DouyinClient douyinClient;
    
    public DouyinSearchTool() {
        this.douyinClient = new DouyinClient();
        init();
    }
    
    private void init() {
        setName("DouyinSearchTool");
        setDescription("抖音搜索工具，可以搜索抖音上的视频和用户。输入参数：keyword(搜索关键词), type(搜索类型：video或user), count(返回数量，默认10)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"keyword\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索关键词\"\n" +
                "    },\n" +
                "    \"type\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"搜索类型：video(视频)或user(用户)\",\n" +
                "      \"enum\": [\"video\", \"user\"]\n" +
                "    },\n" +
                "    \"count\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"返回结果数量，默认10\",\n" +
                "      \"default\": 10\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"keyword\", \"type\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("抖音搜索工具输入: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String keyword = (String) inputMap.get("keyword");
            String type = (String) inputMap.get("type");
            Integer count = (Integer) inputMap.getOrDefault("count", 10);
            
            if (StringUtils.isBlank(keyword)) {
                return new ToolExecuteResult("错误：搜索关键词不能为空");
            }
            
            if (StringUtils.isBlank(type)) {
                return new ToolExecuteResult("错误：搜索类型不能为空");
            }
            
            if (count == null || count <= 0) {
                count = 10;
            }
            
            DouyinSearchResponse response;
            if ("video".equals(type)) {
                response = douyinClient.searchVideos(keyword, count);
            } else if ("user".equals(type)) {
                response = douyinClient.searchUsers(keyword, count);
            } else {
                return new ToolExecuteResult("错误：搜索类型必须是video或user");
            }
            
            if (response.getVideos() == null || response.getVideos().isEmpty()) {
                return new ToolExecuteResult("未找到相关" + ("video".equals(type) ? "视频" : "用户"));
            }
            
            StringBuilder result = new StringBuilder();
            result.append("找到 ").append(response.getVideos().size()).append(" 个相关").append("video".equals(type) ? "视频" : "用户").append("：\n\n");
            
            for (int i = 0; i < response.getVideos().size(); i++) {
                DouyinVideo video = response.getVideos().get(i);
                result.append("结果 ").append(i + 1).append("：\n");
                result.append("视频ID：").append(video.getAwemeId()).append("\n");
                result.append("描述：").append(video.getDesc()).append("\n");
                result.append("作者：").append(video.getAuthor()).append("\n");
                result.append("作者ID：").append(video.getAuthorId()).append("\n");
                result.append("创建时间：").append(video.getCreateTime()).append("\n");
                result.append("播放数：").append(video.getPlayCount()).append("\n");
                result.append("点赞数：").append(video.getDiggCount()).append("\n");
                result.append("评论数：").append(video.getCommentCount()).append("\n");
                result.append("分享数：").append(video.getShareCount()).append("\n");
                result.append("时长：").append(video.getDuration()).append("秒\n");
                result.append("视频链接：").append(video.getVideoUrl()).append("\n");
                result.append("封面：").append(video.getCoverUrl()).append("\n");
                if (video.getMusicTitle() != null) {
                    result.append("音乐：").append(video.getMusicTitle()).append(" - ").append(video.getMusicAuthor()).append("\n");
                }
                if (video.getLocation() != null) {
                    result.append("位置：").append(video.getLocation()).append("\n");
                }
                result.append("---\n");
            }
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("抖音搜索失败", e);
            return new ToolExecuteResult("搜索失败: " + e.getMessage());
        }
    }
}