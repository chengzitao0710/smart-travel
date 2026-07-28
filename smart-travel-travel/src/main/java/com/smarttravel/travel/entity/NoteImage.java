package com.smarttravel.travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "游记图片")
@TableName("tb_note_image")
public class NoteImage {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "游记ID")
    private Long noteId;
    @Schema(description = "图片URL")
    private String imageUrl;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}