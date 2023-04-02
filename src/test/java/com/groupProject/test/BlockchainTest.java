package com.groupProject.test;
import com.groupProject.Network.TransactionPool;
import com.groupProject.Wallet.Wallet;
import com.groupProject.Wallet.WalletUtils;
import com.groupProject.block.Block;
import com.groupProject.block.Blockchain;
import com.groupProject.pow.ProofOfWork;
import com.groupProject.transaction.TXInput;
import com.groupProject.transaction.TXOutput;
import com.groupProject.transaction.Transaction;
import com.groupProject.transaction.UTXOSet;
import com.groupProject.utils.Base58Check;
import com.groupProject.utils.RocksDBUtils;
import org.apache.commons.codec.binary.Hex;

import java.util.*;

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
            String C = "1JDGcRwjyteU2XERg48ySWxqSwzEojDmpb";
            String D = "19HvkPmP7As1tqLrwT2sfmagVb9Qv4Z3NG";
//            bt.createWallet();
//            bt.printAddresses();
//            bt.createBlockchain(A);

//            bt.getBalance(A);
//            bt.getBalance(B);
//            bt.send(B,A,2);
//            bt.getBalance(A);
//            bt.getBalance(B);

            bt.getBalance(A);
            bt.getBalance(B);
            bt.getBalance(C);
            bt.getBalance(D);
//            Transaction t1 = bt.send1(A,B,2);
////            Transaction t2 = bt.send1(A,C,2);
//            Transaction t3 = bt.send1(B,C,2);
//////            Transaction t4 = bt.send1(C,D,2);
//            List<Transaction> transactions = new ArrayList<>();
//            transactions.add(t1);
////            transactions.add(t2);
//            transactions.add(t3);
////
//            bt.mineBlock(D, transactions);

//            bt.getBalance(A);
//            bt.getBalance(B);
//            bt.getBalance(C);
//            bt.getBalance(D);


        }catch (Exception e){
            e.printStackTrace();

        }
    }

    private void send(String from, String to, int amount) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain(from);
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
//        Transaction rewardTx = Transaction.newCoinBaseTX(from, "");
//        Block newBlock = blockchain.mineBlock(new Transaction[]{transaction, rewardTx});
        Block newBlock = blockchain.mineBlock(new Transaction[]{transaction});
        new UTXOSet(blockchain).update(newBlock);
        RocksDBUtils.getInstance().closeDB();
        System.out.println("Success!");
    }

    private Transaction send1(String from, String to, int amount) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain(from);
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        System.out.println("Create Transaction " + transaction.getTxId() + " success!");
        return transaction;
    }

    private void mineBlock(String miner, List<Transaction> transactions) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain(miner);
        Transaction rewardTx = Transaction.newCoinBaseTX(miner, "");
        transactions.add(rewardTx);
        checkTransaction(transactions);
        Transaction[] transactions1 = new Transaction[transactions.size()];

        Block newBlock = blockchain.mineBlock(transactions.toArray(transactions1));
        new UTXOSet(blockchain).update(newBlock);
        RocksDBUtils.getInstance().closeDB();
        for(Transaction transaction : transactions){
            System.out.println("Transaction " + transaction.getTxId() + " already mine in the block " + newBlock.getHash());
        }
    }

    private static void checkTransaction(List<Transaction> transactions){
        Map<String, TXInput> inputPool = new HashMap<>();
        Iterator<Transaction> iterator = transactions.iterator();
        while(iterator.hasNext()) {
            Transaction transaction = iterator.next();
            if (!transaction.isCoinbase()) {
                for (TXInput input : transaction.getInputs()) {
                    String txId = Hex.encodeHexString(input.getTxId());
                    if (inputPool.containsKey(txId)) {
                        iterator.remove();
                        System.out.println("Transaction " + transaction.getTxId() + " TxInput " + txId + " Double Spending! remove!");
                        break;
                    } else {
                        inputPool.put(txId, input);
//                    System.out.println("Transaction " + transaction.getTxId() + " TxInput " + txId + " valid!");
                    }
                }
            }
        }
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