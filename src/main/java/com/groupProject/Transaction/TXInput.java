package com.groupProject.Transaction;

import com.groupProject.utils.BtcAddressUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXInput {

    /**
     * HashValue of the txId
     */
    private byte[] txId;
    /**
     * Index of the txId Output
     */
    private int txOutputIndex;
    /**
     * Signature
     */
    private byte[] signature;
    /**
     * PublicKey
     */
    private byte[] pubKey;


    /**
     * UseKey checks whether the address initiated the transaction
     *
     * @param pubKeyHash
     * @return
     */
    public boolean usesKey(byte[] pubKeyHash) {
        byte[] lockingHash = BtcAddressUtils.ripeMD160Hash(this.getPubKey());
        return Arrays.equals(lockingHash, pubKeyHash);
    }

}