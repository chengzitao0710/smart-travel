package com.smarttravel.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "游记评论")
@TableName("tb_note_comment")
public class NoteComment {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "游记ID")
    private Long noteId;
    @Schema(description = "父评论ID")
    private Long parentId;
    @Schema(description = "回复目标评论ID")
    private Long answerId;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "点赞数")
    private Integer liked;
    @Schema(description = "状态：0正常 1被举报 2被禁用")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "是否点赞")
    @TableField(exist = false)
    private Boolean isLiked;
    @Schema(description = "回复列表")
    @TableField(exist = false)
    private List<NoteComment> replies;
}