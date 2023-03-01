package com.groupProject.pow;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p> 工作量计算结果 </p>
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PowResult {

    /**
     * 计数器
     */
    private long nonce;
    /**
     * hash值
     */
    private String hash;

}

