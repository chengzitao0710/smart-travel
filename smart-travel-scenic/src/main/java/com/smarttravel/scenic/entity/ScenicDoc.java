package com.smarttravel.scenic.entity;

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
@Document(indexName = "scenic")
@Schema(description = "景点ES文档")
public class ScenicDoc {

    @Id
    @Schema(description = "景点ID")
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    @Schema(description = "景点名称")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    @Schema(description = "景点地址")
    private String address;

    @Field(type = FieldType.Keyword)
    @Schema(description = "所在区域")
    private String area;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    @Schema(description = "景点描述")
    private String description;

    @Field(type = FieldType.Keyword)
    @Schema(description = "标签")
    private String tags;

    @Field(type = FieldType.Long)
    @Schema(description = "景点类型ID")
    private Long typeId;

    @Field(type = FieldType.Integer)
    @Schema(description = "评分")
    private Integer score;

    @Field(type = FieldType.Integer)
    @Schema(description = "销量")
    private Integer sold;

    @Field(type = FieldType.Integer)
    @Schema(description = "评论数")
    private Integer comments;

    @Field(type = FieldType.Long)
    @Schema(description = "平均价格")
    private Long avgPrice;

    @Field(type = FieldType.Keyword)
    @Schema(description = "开放时间")
    private String openHours;

    @Field(type = FieldType.Keyword)
    @Schema(description = "图片列表JSON")
    private String images;

    @Field(type = FieldType.Integer)
    @Schema(description = "状态：0下架 1上架")
    private Integer status;

    public static ScenicDoc from(Scenic scenic) {
        return ScenicDoc.builder()
                .id(scenic.getId())
                .name(scenic.getName())
                .address(scenic.getAddress())
                .area(scenic.getArea())
                .description(scenic.getDescription())
                .tags(scenic.getTags())
                .typeId(scenic.getTypeId())
                .score(scenic.getScore())
                .sold(scenic.getSold() != null ? scenic.getSold() : 0)
                .comments(scenic.getComments() != null ? scenic.getComments() : 0)
                .avgPrice(scenic.getAvgPrice())
                .openHours(scenic.getOpenHours())
                .images(scenic.getImages())
                .status(scenic.getStatus())
                .build();
    }
}