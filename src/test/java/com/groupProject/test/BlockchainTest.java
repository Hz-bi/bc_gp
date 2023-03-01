package com.groupProject.test;
import com.groupProject.block.Block;
import com.groupProject.block.Blockchain;
import com.groupProject.pow.ProofOfWork;

/**
 * 测试
 */
public class BlockchainTest {

    public static void main(String[] args) {

        try {
            Blockchain blockchain = Blockchain.newBlockchain();

            blockchain.addBlock("Send 1.0 BTC to wangwei");
            blockchain.addBlock("Send 2.5 more BTC to wangwei");
            blockchain.addBlock("Send 3.5 more BTC to wangwei");

            for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator(); iterator.hashNext(); ) {
                Block block = iterator.next();

                if (block != null) {
                    boolean validate = ProofOfWork.newProofOfWork(block).validate();
                    System.out.println(block.toString() + ", validate = " + validate);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}