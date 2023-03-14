package com.groupProject.transaction;

import com.groupProject.block.Blockchain;
import com.groupProject.utils.SerializeUtils;
import lombok.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

    private static final int SUBSIDY = 10;

    /**
     * 交易的Hash
     */
    private byte[] txId;

    /**
     * 交易輸入
     */
    private TXInput[] inputs;

    /**
     * 交易輸出
     */
    private TXOutput[] outputs;

    public byte[] getTxId() {
        return txId;
    }

    public void setTxId() {
        this.txId = txId;
    }

    public TXInput[] getInputs() {
        return inputs;
    }

    public void setInputs(TXInput[] inputs) {
        this.inputs = inputs;
    }

    public TXOutput[] getOutputs() {
        return outputs;
    }

    public void setOutputs(TXOutput[] outputs) {
        this.outputs = outputs;
    }

    /**
     * 计算交易信息的Hash值
     *
     * @return
     */
    public byte[] hash() {
        // 使用序列化的方式对Transaction对象进行深度复制
        byte[] serializeBytes = SerializeUtils.serialize(this);
        Transaction copyTx = (Transaction) SerializeUtils.deserialize(serializeBytes);
        copyTx.setTxId(new byte[]{});
        return DigestUtils.sha256(SerializeUtils.serialize(copyTx));
    }

    /**
     *創建Coinbase交易
     * @param to
     * @param data
     * @return
     */
    public static Transaction newCoinBaseTX(String to, String data){
        if(StringUtils.isBlank(data)){
            data = String.format("Reward to %s", to);
        }
        // 创建交易输入
        TXInput txInput = new TXInput(new byte[]{}, -1, data);
        // 创建交易输出
        TXOutput txOutput = new TXOutput(SUBSIDY, to);
        // 创建交易
        Transaction tx = new Transaction(null, new TXInput[]{txInput}, new TXOutput[]{txOutput});
        // 设置交易ID
        tx.setTxId(tx.hash());
        return tx;
    }

    /**
     *創建新的UTXO交易
     * @param from
     * @param to
     * @param amount
     * @param blockchain
     * @return
     * @throws Exception
     */
    public static Transaction newUTXOTransaction(String from, String to, int amount, Blockchain blockchain) throws Exception {
        SpendableOutputResult result = blockchain.findSpendableOutputs(from, amount);
        int accumulated = result.getAccumulated();
        Map<String, int[]> unspentOuts = result.getUnspentOuts();

        if (accumulated < amount) {
            throw new Exception("ERROR: Not enough funds");
        }
        Iterator<Map.Entry<String, int[]>> iterator = unspentOuts.entrySet().iterator();

        TXInput[] txInputs = {};
        while (iterator.hasNext()) {
            Map.Entry<String, int[]> entry = iterator.next();
            String txIdStr = entry.getKey();
            int[] outIdxs = entry.getValue();
            byte[] txId = Hex.decodeHex(txIdStr);
            for (int outIndex : outIdxs) {
                txInputs = ArrayUtils.add(txInputs, new TXInput(txId, outIndex, from));
            }
        }

        TXOutput[] txOutput = {};
        txOutput = ArrayUtils.add(txOutput, new TXOutput(amount, to));
        if (accumulated > amount) {
            txOutput = ArrayUtils.add(txOutput, new TXOutput((accumulated - amount), from));
        }

        Transaction newTx = new Transaction(null, txInputs, txOutput);
        newTx.setTxId(newTx.hash());
        return newTx;
    }

    /**判斷是否為Coinbase交易
     *
     * @return
     */
    public boolean isCoinbase() {
        return this.getInputs().length == 1
                && this.getInputs()[0].getTxId().length == 0
                && this.getInputs()[0].getTxOutputIndex() == -1;
    }

}
