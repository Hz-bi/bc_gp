package com.groupProject.block;

import com.groupProject.transaction.SpendableOutputResult;
import com.groupProject.transaction.TXInput;
import com.groupProject.transaction.TXOutput;
import com.groupProject.transaction.Transaction;
import com.groupProject.utils.ByteUtils;
import com.groupProject.utils.RocksDBUtils;

import lombok.Data;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


@Data

@NoArgsConstructor
@Slf4j
public class Blockchain {

    private String lastBlockHash;

    private Blockchain(String lastBlockHash) {
        this.lastBlockHash = lastBlockHash;
    }


    /**
     * <p> 添加区块  </p>
     *
     * @param data
     */
//    public void addBlock(String data) throws Exception {
//        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
//        if (StringUtils.isBlank(lastBlockHash)) {
//            throw new Exception("Fail to add block into blockchain ! ");
//        }
//        this.addBlock(Block.newBlock(lastBlockHash, data));
//    }

    /**
     * <p> 添加区块  </p>
     *
     * @param block
     */
    public void addBlock(Block block) throws Exception {
        RocksDBUtils.getInstance().putLastBlockHash(block.getHash());
        RocksDBUtils.getInstance().putBlock(block);
        this.lastBlockHash = block.getHash();
    }


    /**
     * <p> 创建区块链 </p>
     *
     * @return
     */
//    public static Blockchain newBlockchain() throws Exception {
//        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
//        if (StringUtils.isBlank(lastBlockHash)) {
//            Block genesisBlock = Block.newGenesisBlock();
//            lastBlockHash = genesisBlock.getHash();
//            RocksDBUtils.getInstance().putBlock(genesisBlock);
//            RocksDBUtils.getInstance().putLastBlockHash(lastBlockHash);
//        }
//        return new Blockchain(lastBlockHash);
//    }
    public static Blockchain newBlockchain(String address) throws Exception {
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (StringUtils.isBlank(lastBlockHash)) {
            Transaction coinbaseTX = Transaction.newCoinBaseTX(address, "");
            Block genesisBlock = Block.newGenesisBlock(coinbaseTX);
            lastBlockHash = genesisBlock.getHash();
            RocksDBUtils.getInstance().putBlock(genesisBlock);
            RocksDBUtils.getInstance().putLastBlockHash(lastBlockHash);
        }
        return new Blockchain(lastBlockHash);
    }

    /**
     *獲取所有花費交易
     * @param address
     * @return
     * @throws Exception
     */
    private Map<String, int[]> getAllSpentTXOs(String address) throws Exception {
        // 定义TxId ——> spentOutIndex[]，存储交易ID与已被花费的交易输出数组索引值
        Map<String, int[]> spentTXOs = new HashMap<>();
        for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
            Block block = blockchainIterator.next();

            for (Transaction transaction : block.getTransactions()) {
                // 如果是 coinbase 交易，直接跳过，因为它不存在引用前一个区块的交易输出
                if (transaction.isCoinbase()) {
                    continue;
                }
                for (TXInput txInput : transaction.getInputs()) {
                    if (txInput.canUnlockOutputWith(address)) {
                        String inTxId = Hex.encodeHexString(txInput.getTxId());
                        int[] spentOutIndexArray = spentTXOs.get(inTxId);
                        if (spentOutIndexArray == null) {
                            spentTXOs.put(inTxId, new int[]{txInput.getTxOutputIndex()});
                        } else {
                            spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, txInput.getTxOutputIndex());
                            spentTXOs.put(inTxId, spentOutIndexArray);
                        }
                    }
                }
            }
        }
        return spentTXOs;
    }

    /**
     *獲取所有未花費的交易
     * @param address
     * @return
     * @throws Exception
     */
    private Transaction[] findUnspentTransactions(String address) throws Exception {
        Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs(address);
        Transaction[] unspentTxs = {};

        // 再次遍历所有区块中的交易输出
        for (BlockchainIterator blockchainIterator = this.getBlockchainIterator(); blockchainIterator.hashNext(); ) {
            Block block = blockchainIterator.next();
            for (Transaction transaction : block.getTransactions()) {

                String txId = Hex.encodeHexString(transaction.getTxId());

                int[] spentOutIndexArray = allSpentTXOs.get(txId);

                for (int outIndex = 0; outIndex < transaction.getOutputs().length; outIndex++) {
                    if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
                        continue;
                    }

                    // 保存不存在 allSpentTXOs 中的交易
                    if (transaction.getOutputs()[outIndex].canBeUnlockedWith(address)) {
                        unspentTxs = ArrayUtils.add(unspentTxs, transaction);
                    }
                }
            }
        }
        return unspentTxs;
    }

    /**
     *
     * @param address
     * @return
     * @throws Exception
     */
    public TXOutput[] findUTXO(String address) throws Exception {
        Transaction[] unspentTxs = this.findUnspentTransactions(address);
        TXOutput[] utxos = {};
        if (unspentTxs == null || unspentTxs.length == 0) {
            return utxos;
        }
        for (Transaction tx : unspentTxs) {
            for (TXOutput txOutput : tx.getOutputs()) {
                if (txOutput.canBeUnlockedWith(address)) {
                    utxos = ArrayUtils.add(utxos, txOutput);
                }
            }
        }
        return utxos;
    }

    /**
     *找到可以花費的Output
     * @param address
     * @param amount
     * @return
     * @throws Exception
     */
    public SpendableOutputResult findSpendableOutputs(String address, int amount) throws Exception {
        Transaction[] unspentTXs = this.findUnspentTransactions(address);
        int accumulated = 0;
        Map<String, int[]> unspentOuts = new HashMap<>();
        for (Transaction tx : unspentTXs) {

            String txId = Hex.encodeHexString(tx.getTxId());

            for (int outId = 0; outId < tx.getOutputs().length; outId++) {

                TXOutput txOutput = tx.getOutputs()[outId];

                if (txOutput.canBeUnlockedWith(address) && accumulated < amount) {
                    accumulated += txOutput.getValue();

                    int[] outIds = unspentOuts.get(txId);
                    if (outIds == null) {
                        outIds = new int[]{outId};
                    } else {
                        outIds = ArrayUtils.add(outIds, outId);
                    }
                    unspentOuts.put(txId, outIds);
                    if (accumulated >= amount) {
                        break;
                    }
                }
            }
        }
        return new SpendableOutputResult(accumulated, unspentOuts);
    }

    public void mineBlock(Transaction[] transactions) throws Exception {
        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
        if (lastBlockHash == null) {
            throw new Exception("ERROR: Fail to get last block hash ! ");
        }
        Block block = Block.newBlock(lastBlockHash, transactions);
        this.addBlock(block);
    }


    /**
     * 区块链迭代器
     */
    public class BlockchainIterator {

        private String currentBlockHash;

        public BlockchainIterator(String currentBlockHash) {
            this.currentBlockHash = currentBlockHash;
        }

        /**
         * 是否有下一个区块
         *
         * @return
         */
        public boolean hashNext() throws Exception {
            if (StringUtils.isBlank(currentBlockHash) || currentBlockHash.equals(ByteUtils.ZERO_HASH)) {
                return false;
            }
            Block lastBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
            if (lastBlock == null) {
                return false;
            }
            boolean flag =  lastBlock.getPreviousHash().equals(ByteUtils.ZERO_HASH);
            if(flag){
                return true;
            }
            // 创世区块直接放行
            if (lastBlock.getPreviousHash().length() == 0) {
                return true;
            }
            return RocksDBUtils.getInstance().getBlock(lastBlock.getPreviousHash()) != null;
        }

        /**
         * 返回区块
         *
         * @return
         */
        public Block next() throws Exception {
            Block currentBlock = RocksDBUtils.getInstance().getBlock(currentBlockHash);
            if (currentBlock != null) {
                this.currentBlockHash = currentBlock.getPreviousHash();
//                if(this.currentBlockHash.equals(ByteUtils.ZERO_HASH)) return null;
                return currentBlock;
            }
            return null;
        }
        
    }
    public BlockchainIterator getBlockchainIterator() {
        return new BlockchainIterator(lastBlockHash);
    }


}
