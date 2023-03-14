package com.groupProject.test;
import com.groupProject.block.Block;
import com.groupProject.block.Blockchain;
import com.groupProject.pow.ProofOfWork;
import com.groupProject.transaction.TXOutput;
import com.groupProject.transaction.Transaction;
import com.groupProject.utils.RocksDBUtils;

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
        BlockchainTest bt = new BlockchainTest();
        String address = "A";
        String A = "A";
        Blockchain blockchain = Blockchain.newBlockchain(address);
        TXOutput[] txOutputs = blockchain.findUTXO(address);
        int balance = 0;
        if (txOutputs != null && txOutputs.length > 0) {
            for (TXOutput txOutput : txOutputs) {
                balance += txOutput.getValue();
            }
        }
        System.out.printf("Balance of '%s': %d\n", "A", balance);
        bt.send(A,address,5);
    }

    private void send(String from, String to, int amount) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain(from);
        Transaction transaction = Transaction.newUTXOTransaction(from, to, amount, blockchain);
        blockchain.mineBlock(new Transaction[]{transaction});
        RocksDBUtils.getInstance().closeDB();
        System.out.println("Success!");
    }
}