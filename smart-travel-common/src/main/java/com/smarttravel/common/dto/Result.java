package com.smarttravel.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果")
public class Result {
    @Schema(description = "是否成功")
    private Boolean success;
    @Schema(description = "错误信息")
    private String message;
    @Schema(description = "响应数据")
    private Object data;
    @Schema(description = "总条数(分页用)")
    private Long total;

    public static Result ok() {
        return new Result(true, null, null, null);
    }

    public static Result ok(Object data) {
        return new Result(true, null, data, null);
    }

    public static Result ok(List<?> data, Long total) {
        return new Result(true, null, data, total);
    }

    public static Result fail(String message) {
        return new Result(false, message, null, null);
    }
}