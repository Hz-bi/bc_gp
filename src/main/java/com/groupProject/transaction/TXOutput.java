package com.groupProject.transaction;

import com.groupProject.utils.Base58Check;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * @description: some desc
 * @Author: huang
 * @date: 2/3/2023 3:54 pm
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXOutput {

    /**
     * 数值
     */
    private int value;

//    /**
//     * 锁定脚本
//     */
//    private String scriptPubKey;
    /**
     * 公钥Hash
     */
    private byte[] pubKeyHash;


    /**
     * 创建交易输出
     *
     * @param value
     * @param address
     * @return
     */
    public static TXOutput newTXOutput(int value, String address) {
        // 反向转化为 byte 数组
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        return new TXOutput(value, pubKeyHash);
    }
    /**
     * 检查交易输出是否能够使用指定的公钥
     *
     * @param pubKeyHash
     * @return
     */
    public boolean isLockedWithKey(byte[] pubKeyHash) {
        return Arrays.equals(this.getPubKeyHash(), pubKeyHash);
    }
//    /**
//     * 判断解锁数据是否能够解锁交易输出
//     *
//     * @param unlockingData
//     * @return
//     */
//    public boolean canBeUnlockedWith(String unlockingData) {
//        return this.getScriptPubKey().endsWith(unlockingData);
//    }
}
