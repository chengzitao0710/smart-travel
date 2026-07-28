package com.smarttravel.travel.entity;

import com.smarttravel.travel.entity.TravelNote;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "travel_note")
@Schema(description = "游记ES文档")
public class TravelNoteDoc {

    @Id
    @Schema(description = "游记ID")
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    @Schema(description = "游记标题")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    @Schema(description = "游记内容")
    private String content;

    @Field(type = FieldType.Keyword)
    @Schema(description = "标签")
    private String tags;

    @Field(type = FieldType.Long)
    @Schema(description = "用户ID")
    private Long userId;

    @Field(type = FieldType.Long)
    @Schema(description = "景点ID")
    private Long scenicId;

    @Field(type = FieldType.Integer)
    @Schema(description = "点赞数")
    private Integer liked;

    @Field(type = FieldType.Integer)
    @Schema(description = "评论数")
    private Integer comments;

    @Field(type = FieldType.Integer)
    @Schema(description = "状态：0草稿 1审核中 2已发布 3已删除")
    private Integer status;

    @Field(type = FieldType.Keyword)
    @Schema(description = "创建时间")
    private String createTime;

    public static TravelNoteDoc from(TravelNote note) {
        return TravelNoteDoc.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .tags(note.getTags())
                .userId(note.getUserId())
                .scenicId(note.getScenicId())
                .liked(note.getLiked())
                .comments(note.getComments())
                .status(note.getStatus())
                .createTime(note.getCreateTime() != null ? note.getCreateTime().toString() : null)
                .build();
    }
}