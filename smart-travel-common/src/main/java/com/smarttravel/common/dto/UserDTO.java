package com.smarttravel.common.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDTO {
    private Long id;
    private String nickname;
    private String icon;
}
