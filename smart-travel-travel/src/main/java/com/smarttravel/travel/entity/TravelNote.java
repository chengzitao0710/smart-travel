package com.smarttravel.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "游记")
@TableName("tb_travel_note")
public class TravelNote {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "景点ID")
    private Long scenicId;
    @Schema(description = "游记标题")
    private String title;
    @Schema(description = "游记内容")
    private String content;
    @Schema(description = "标签")
    private String tags;
    @Schema(description = "是否点赞")
    @TableField(exist = false)
    private Boolean isLiked;
    @Schema(description = "点赞数")
    private Integer liked;
    @Schema(description = "评论数")
    private Integer comments;
    @Schema(description = "是否置顶 0=否 1=是")
    private Integer isTop;
    @Schema(description = "状态：0草稿 1审核中 2已发布 3已删除")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "图片列表")
    @TableField(exist = false)
    private List<NoteImage> imageList;
}