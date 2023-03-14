package com.groupProject.transaction;

import lombok.*;

import java.util.Map;

/**
 * @description: some desc
 * @Author: huang
 * @date: 2/3/2023 10:31 pm
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpendableOutputResult {

    /**
     * 交易時的支付金額
     */
    private int accumulated;

    /**
     *未花費的交易
     */
    private Map<String, int[]> unspentOuts;
}
