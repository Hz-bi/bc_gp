package com.groupProject.transaction;

import com.groupProject.Wallet.Wallet;
import com.groupProject.Wallet.WalletUtils;
import com.groupProject.block.Blockchain;
import com.groupProject.utils.BtcAddressUtils;
import com.groupProject.utils.RocksDBUtils;
import com.groupProject.utils.SerializeUtils;
import lombok.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;


import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.Arrays;
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
        TXInput txInput = new TXInput(new byte[]{}, -1, null, data.getBytes());
        // 创建交易输出
        TXOutput txOutput = TXOutput.newTXOutput(SUBSIDY, to);
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
//    public static Transaction newUTXOTransaction(String from, String to, int amount, Blockchain blockchain) throws Exception {
//        // 获取钱包
//        Wallet senderWallet = WalletUtils.getInstance().getWallet(from);
//        byte[] pubKey = senderWallet.getPublicKey();
//        byte[] pubKeyHash = BtcAddressUtils.ripeMD160Hash(pubKey);
//
//        SpendableOutputResult result = new UTXOSet(blockchain).findSpendableOutputs(pubKeyHash, amount);
//        int accumulated = result.getAccumulated();
//        Map<String, int[]> unspentOuts = result.getUnspentOuts();
//
//        if (accumulated < amount) {
//            throw new Exception("ERROR: Not enough funds");
//        }
//        Iterator<Map.Entry<String, int[]>> iterator = unspentOuts.entrySet().iterator();
//
//        TXInput[] txInputs = {};
//        while (iterator.hasNext()) {
//            Map.Entry<String, int[]> entry = iterator.next();
//            String txIdStr = entry.getKey();
//            int[] outIdxs = entry.getValue();
//            byte[] txId = Hex.decodeHex(txIdStr);
//            for (int outIndex : outIdxs) {
//                txInputs = ArrayUtils.add(txInputs, new TXInput(txId, outIndex, null, pubKey));
//            }
//        }
//
//        TXOutput[] txOutput = {};
//        txOutput = ArrayUtils.add(txOutput, TXOutput.newTXOutput(amount, to));
//        if (accumulated > amount) {
//            txOutput = ArrayUtils.add(txOutput, TXOutput.newTXOutput((accumulated - amount), from));
//        }
//
//        Transaction newTx = new Transaction(null, txInputs, txOutput);
//        newTx.setTxId(newTx.hash());
//        return newTx;
//    }
    public static Transaction newUTXOTransaction(String from, String to, int amount, Blockchain blockchain) throws Exception {
        // 获取钱包
        Wallet senderWallet = WalletUtils.getInstance().getWallet(from);
        byte[] pubKey = senderWallet.getPublicKey();
        byte[] pubKeyHash = BtcAddressUtils.ripeMD160Hash(pubKey);

        SpendableOutputResult result = new UTXOSet(blockchain).findSpendableOutputs(pubKeyHash, amount);
        int accumulated = result.getAccumulated();
        Map<String, int[]> unspentOuts = result.getUnspentOuts();

        if (accumulated < amount) {
//            log.error("ERROR: Not enough funds ! accumulated=" + accumulated + ", amount=" + amount);
            throw new RuntimeException("ERROR: Not enough funds ! ");
        }
        Iterator<Map.Entry<String, int[]>> iterator = unspentOuts.entrySet().iterator();

        TXInput[] txInputs = {};
        while (iterator.hasNext()) {
            Map.Entry<String, int[]> entry = iterator.next();
            String txIdStr = entry.getKey();
            int[] outIds = entry.getValue();
            byte[] txId = Hex.decodeHex(txIdStr);
            for (int outIndex : outIds) {
                txInputs = ArrayUtils.add(txInputs, new TXInput(txId, outIndex, null, pubKey));
            }
        }

        TXOutput[] txOutput = {};
        txOutput = ArrayUtils.add(txOutput, TXOutput.newTXOutput(amount, to));
        if (accumulated > amount) {
            txOutput = ArrayUtils.add(txOutput, TXOutput.newTXOutput((accumulated - amount), from));
        }

        Transaction newTx = new Transaction(null, txInputs, txOutput);
        newTx.setTxId(newTx.hash());

        // 进行交易签名
        blockchain.signTransaction(newTx, senderWallet.getPrivateKey());
//        for(TXInput input : txInputs){
//            String txId = Hex.encodeHexString(input.getTxId());
//            RocksDBUtils.getInstance().deleteUTXOs(txId);
//        }
//        updateUTXO(newTx);

        return newTx;
    }

    public static void updateUTXO(Transaction transaction){
        for (TXInput txInput : transaction.getInputs()) {
            // 余下未被使用的交易输出
            TXOutput[] remainderUTXOs = {};
            String txId = Hex.encodeHexString(txInput.getTxId());
            TXOutput[] txOutputs = RocksDBUtils.getInstance().getUTXOs(txId);

            if (txOutputs == null) {
                continue;
            }

            for (int outIndex = 0; outIndex < txOutputs.length; outIndex++) {
                if (outIndex != txInput.getTxOutputIndex()) {
                    remainderUTXOs = ArrayUtils.add(remainderUTXOs, txOutputs[outIndex]);
                }
            }

            // 没有剩余则删除，否则更新
            if (remainderUTXOs.length == 0) {
                RocksDBUtils.getInstance().deleteUTXOs(txId);
            } else {
                RocksDBUtils.getInstance().putUTXOs(txId, remainderUTXOs);
            }
        }

    // 新的交易输出保存到DB中
    TXOutput[] txOutputs = transaction.getOutputs();
    String txId = Hex.encodeHexString(transaction.getTxId());
    RocksDBUtils.getInstance().putUTXOs(txId, txOutputs);
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

    /**
     * 创建用于签名的交易数据副本，交易输入的 signature 和 pubKey 需要设置为null
     *
     * @return
     */
    public Transaction trimmedCopy() {
        TXInput[] tmpTXInputs = new TXInput[this.getInputs().length];
        for (int i = 0; i < this.getInputs().length; i++) {
            TXInput txInput = this.getInputs()[i];
            tmpTXInputs[i] = new TXInput(txInput.getTxId(), txInput.getTxOutputIndex(), null, null);
        }

        TXOutput[] tmpTXOutputs = new TXOutput[this.getOutputs().length];
        for (int i = 0; i < this.getOutputs().length; i++) {
            TXOutput txOutput = this.getOutputs()[i];
            tmpTXOutputs[i] = new TXOutput(txOutput.getValue(), txOutput.getPubKeyHash());
        }

        return new Transaction(this.getTxId(), tmpTXInputs, tmpTXOutputs);
    }

    /**
     * 签名
     *
     * @param privateKey 私钥
     * @param prevTxMap  前面多笔交易集合
     */
    public void sign(BCECPrivateKey privateKey, Map<String, Transaction> prevTxMap) throws Exception {
        // coinbase 交易信息不需要签名，因为它不存在交易输入信息
        if (this.isCoinbase()) {
            return;
        }
        // 再次验证一下交易信息中的交易输入是否正确，也就是能否查找对应的交易数据
        for (TXInput txInput : this.getInputs()) {
            if (prevTxMap.get(Hex.encodeHexString(txInput.getTxId())) == null) {
                throw new RuntimeException("ERROR: Previous transaction is not correct");
            }
        }

        // 创建用于签名的交易信息的副本
        Transaction txCopy = this.trimmedCopy();

        Security.addProvider(new BouncyCastleProvider());
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ecdsaSign.initSign(privateKey);

        for (int i = 0; i < txCopy.getInputs().length; i++) {
            TXInput txInputCopy = txCopy.getInputs()[i];
            // 获取交易输入TxID对应的交易数据
            Transaction prevTx = prevTxMap.get(Hex.encodeHexString(txInputCopy.getTxId()));
            // 获取交易输入所对应的上一笔交易中的交易输出
            TXOutput prevTxOutput = prevTx.getOutputs()[txInputCopy.getTxOutputIndex()];
            txInputCopy.setPubKey(prevTxOutput.getPubKeyHash());
            txInputCopy.setSignature(null);
            // 得到要签名的数据，即交易ID
            txCopy.setTxId(txCopy.hash());
            txInputCopy.setPubKey(null);

            // 对整个交易信息仅进行签名，即对交易ID进行签名
            ecdsaSign.update(txCopy.getTxId());
            byte[] signature = ecdsaSign.sign();

            // 将整个交易数据的签名赋值给交易输入，因为交易输入需要包含整个交易信息的签名
            // 注意是将得到的签名赋值给原交易信息中的交易输入
            this.getInputs()[i].setSignature(signature);
        }
    }

    /**
     * 验证交易信息
     *
     * @param prevTxMap 前面多笔交易集合
     * @return
     */
    public boolean verify(Map<String, Transaction> prevTxMap) throws Exception {
        // coinbase 交易信息不需要签名，也就无需验证
        if (this.isCoinbase()) {
            return true;
        }

        // 再次验证一下交易信息中的交易输入是否正确，也就是能否查找对应的交易数据
        for (TXInput txInput : this.getInputs()) {
            if (prevTxMap.get(Hex.encodeHexString(txInput.getTxId())) == null) {
                throw new RuntimeException("ERROR: Previous transaction is not correct");
            }
        }

        // 创建用于签名验证的交易信息的副本
        Transaction txCopy = this.trimmedCopy();

        Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);

        for (int i = 0; i < this.getInputs().length; i++) {
            TXInput txInput = this.getInputs()[i];
            // 获取交易输入TxID对应的交易数据
            Transaction prevTx = prevTxMap.get(Hex.encodeHexString(txInput.getTxId()));
            // 获取交易输入所对应的上一笔交易中的交易输出
            TXOutput prevTxOutput = prevTx.getOutputs()[txInput.getTxOutputIndex()];

            TXInput txInputCopy = txCopy.getInputs()[i];
            txInputCopy.setSignature(null);
            txInputCopy.setPubKey(prevTxOutput.getPubKeyHash());
            // 得到要签名的数据，即交易ID
            txCopy.setTxId(txCopy.hash());
            txInputCopy.setPubKey(null);

            // 使用椭圆曲线 x,y 点去生成公钥Key
            BigInteger x = new BigInteger(1, Arrays.copyOfRange(txInput.getPubKey(), 1, 33));
            BigInteger y = new BigInteger(1, Arrays.copyOfRange(txInput.getPubKey(), 33, 65));
            ECPoint ecPoint = ecParameters.getCurve().createPoint(x, y);

            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameters);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(txCopy.getTxId());
            if (!ecdsaVerify.verify(txInput.getSignature())) {
                return false;
            }
        }
        return true;
    }

}
