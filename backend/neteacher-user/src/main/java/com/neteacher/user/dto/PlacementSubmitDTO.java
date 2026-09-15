package com.neteacher.user.dto;

import lombok.Data;
import java.util.List;

/**
 * 提交入学测评定级请求体。
 */
@Data
public class PlacementSubmitDTO {
    private List<PlacementAnswerDTO> answers;
}
