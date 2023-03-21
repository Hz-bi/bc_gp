package com.groupProject.test;
import com.groupProject.Wallet.Wallet;
import com.groupProject.Wallet.WalletUtils;
import com.groupProject.block.Block;
import com.groupProject.block.Blockchain;
import com.groupProject.pow.ProofOfWork;
import com.groupProject.transaction.TXOutput;
import com.groupProject.transaction.Transaction;
import com.groupProject.transaction.UTXOSet;
import com.groupProject.utils.Base58Check;
import com.groupProject.utils.RocksDBUtils;

import java.util.Arrays;
import java.util.Set;

/**
 * 测试
 */
public class BlockchainTest {

    public static void main(String[] args) throws Exception {

//        try {
//            Blockchain blockchain = Blockchain.newBlockchain();
//
//            blockchain.addBlock("Send 1.0 BTC to wangwei");
//            blockchain.addBlock("Send 2.5 more BTC to wangwei");
//            blockchain.addBlock("Send 3.5 more BTC to wangwei");
//
//            for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
//                Block block = iterator.next();
//
//                if (block != null) {
//                    boolean validate = ProofOfWork.newProofOfWork(block).validate();
//                    System.out.println(block.toString() + ", validate = " + validate);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        BlockchainTest bt = new BlockchainTest();
//        try {
//            String A = "A";
//            String B = "B";
////            Blockchain blockchain = Blockchain.newBlockchain(A);
////            System.out.println(blockchain.toString());
////
////            for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
////                Block block = iterator.next();
////
////                if (block != null) {
////                    boolean validate = ProofOfWork.newProofOfWork(block).validate();
////                    System.out.println(block.toString() + ", validate = " + validate);
////                }
////            }
//            bt.send(A,B,5);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        try {
//            BlockchainTest bt = new BlockchainTest();
//            String A = "A";
//            String B = "B";
//            Blockchain blockchain = Blockchain.newBlockchain(A);
//            System.out.println(blockchain.toString());
//            bt.send(A, A, 15);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        BlockchainTest bt = new BlockchainTest();
//        String address = "A";
//        String B = "B";
//        Blockchain blockchain = Blockchain.newBlockchain(address);
////        bt.send(address,B,5);
//        TXOutput[] txOutputs = blockchain.findUTXO(address);
//        int balance = 0;
//        if (txOutputs != null && txOutputs.length > 0) {
//            for (TXOutput txOutput : txOutputs) {
//                balance += txOutput.getValue();
//            }
//        }
//        System.out.printf("Balance of '%s': %d\n", address, balance);
        try {
            BlockchainTest bt = new BlockchainTest();
            String A = "17waKkoSt6ZNMTvQdM5uxMQceCAhh2YFyP";
            String B = "1CKPT55P3y3e3efv9LnoxXxYRCt35dKypb";
//            bt.createWallet();
            bt.printAddresses();
//            bt.createBlockchain(A);

            bt.getBalance(A);
//            bt.send(A,B,2);
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    private void send(String from, String to, int amount) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain(from);
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        Transaction rewardTx = Transaction.newCoinBaseTX(from, "");
        Block newBlock = blockchain.mineBlock(new Transaction[]{transaction, rewardTx});
        new UTXOSet(blockchain).update(newBlock);
        RocksDBUtils.getInstance().closeDB();
        System.out.println("Success!");
    }

    /**
     * 查询钱包余额
     *
     * @param address 钱包地址
     */
    private void getBalance(String address) throws Exception {
        // 检查钱包地址是否合法
        try {
            Base58Check.base58ToBytes(address);
        } catch (Exception e) {
//            log.error("ERROR: invalid wallet address", e);
            throw new RuntimeException("ERROR: invalid wallet address", e);
        }

        // 得到公钥Hash值
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);

        Blockchain blockchain = Blockchain.newBlockchain(address);
        UTXOSet utxoSet = new UTXOSet(blockchain);

        TXOutput[] txOutputs = utxoSet.findUTXOs(pubKeyHash);
        int balance = 0;
        if (txOutputs != null && txOutputs.length > 0) {
            for (TXOutput txOutput : txOutputs) {
                balance += txOutput.getValue();
            }
        }
        System.out.println("Balance of " + address + " : " + balance);
//        log.info("Balance of '{}': {}\n", new Object[]{address, balance});
    }

    /**
     * 创建区块链
     *
     * @param address
     */
    private void createBlockchain(String address) throws Exception {
        try {
            Blockchain blockchain = Blockchain.newBlockchain(address);
            UTXOSet utxoSet = new UTXOSet(blockchain);
            utxoSet.reIndex();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 打印出区块链中的所有区块
     */
    private void printChain() throws Exception {
        Blockchain blockchain = Blockchain.initBlockchainFromDB();
        for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
            Block block = iterator.next();
            if (block != null) {
                boolean validate = ProofOfWork.newProofOfWork(block).validate();
                System.out.println(block.toString() + ", validate = " + validate);
//                log.info(block.toString() + ", validate = " + validate);
            }
        }
    }

    /**
     * 创建钱包
     *
     * @throws Exception
     */
    private void createWallet() throws Exception {
        Wallet wallet = WalletUtils.getInstance().createWallet();
        System.out.println("wallet address : " + wallet.getAddress());
//        log.info("wallet address : " + wallet.getAddress());
    }

    /**
     * 打印钱包地址
     */
    private void printAddresses() {
        Set<String> addresses = WalletUtils.getInstance().getAddresses();
        if (addresses == null || addresses.isEmpty()) {
//            log.info("There isn't address");
            System.out.println("There isn't address");
            return;
        }
        for (String address : addresses) {
            System.out.println("Wallet address: " + address);
//            log.info("Wallet address: " + address);
        }
    }

}