package com.alibaba.langengine.douyin.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.douyin.model.DouyinVideo;
import com.alibaba.langengine.douyin.sdk.DouyinClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 抖音视频详情工具
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class DouyinVideoTool extends DefaultTool {
    
    private DouyinClient douyinClient;
    
    public DouyinVideoTool() {
        this.douyinClient = new DouyinClient();
        init();
    }
    
    private void init() {
        setName("DouyinVideoTool");
        setDescription("抖音视频详情工具，可以获取抖音视频的详细信息。输入参数：awemeId(视频ID)");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"awemeId\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"抖音视频ID\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"required\": [\"awemeId\"]\n" +
                "}");
    }
    
    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("抖音视频详情工具输入: {}", toolInput);
        
        try {
            Map<String, Object> inputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String awemeId = (String) inputMap.get("awemeId");
            
            if (StringUtils.isBlank(awemeId)) {
                return new ToolExecuteResult("错误：视频ID不能为空");
            }
            
            DouyinVideo video = douyinClient.getVideoDetail(awemeId);
            
            StringBuilder result = new StringBuilder();
            result.append("视频详情：\n");
            result.append("视频ID：").append(video.getAwemeId()).append("\n");
            result.append("描述：").append(video.getDesc()).append("\n");
            result.append("作者：").append(video.getAuthor()).append("\n");
            result.append("作者ID：").append(video.getAuthorId()).append("\n");
            result.append("作者头像：").append(video.getAuthorAvatar()).append("\n");
            result.append("创建时间：").append(video.getCreateTime()).append("\n");
            result.append("播放数：").append(video.getPlayCount()).append("\n");
            result.append("点赞数：").append(video.getDiggCount()).append("\n");
            result.append("评论数：").append(video.getCommentCount()).append("\n");
            result.append("分享数：").append(video.getShareCount()).append("\n");
            result.append("转发数：").append(video.getForwardCount()).append("\n");
            result.append("时长：").append(video.getDuration()).append("秒\n");
            result.append("视频链接：").append(video.getVideoUrl()).append("\n");
            result.append("封面链接：").append(video.getCoverUrl()).append("\n");
            result.append("视频质量：").append(video.getVideoQuality()).append("\n");
            result.append("视频比例：").append(video.getRatio()).append("\n");
            result.append("是否原创：").append(video.getIsOriginal() ? "是" : "否").append("\n");
            
            if (StringUtils.isNotBlank(video.getMusicTitle())) {
                result.append("音乐标题：").append(video.getMusicTitle()).append("\n");
                result.append("音乐作者：").append(video.getMusicAuthor()).append("\n");
                result.append("音乐链接：").append(video.getMusicUrl()).append("\n");
            }
            
            if (StringUtils.isNotBlank(video.getLocation())) {
                result.append("位置：").append(video.getLocation()).append("\n");
            }
            
            if (video.getHashtags() != null && !video.getHashtags().isEmpty()) {
                result.append("话题标签：").append(String.join(", ", video.getHashtags())).append("\n");
            }
            
            return new ToolExecuteResult(result.toString());
            
        } catch (Exception e) {
            log.error("获取抖音视频详情失败", e);
            return new ToolExecuteResult("获取视频详情失败: " + e.getMessage());
        }
    }
}