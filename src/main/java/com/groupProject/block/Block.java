package com.groupProject.block;



import com.groupProject.pow.PowResult;
import com.groupProject.pow.ProofOfWork;
import com.groupProject.utils.ByteUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import java.math.BigInteger;
import java.time.Instant;

@Data
@Getter
@Setter
public class Block {

    /**
     * 区块hash值
     */
    private String hash;
    /**
     * 前一个区块的hash值
     */
    private String previousHash;
    /**
     * 区块数据
     */
    private String data;
    /**
     * 区块创建时间(单位:秒)
     */
    private long timeStamp;
    /**
     * 工作量证明计数器
     */
    private long nonce;

    public Block(String hash, String previousHash, String data, long timeStamp, long nonce) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
    }
    public Block() {

    }
    private static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);

    /**
     * <p> 创建新区块 </p>
     *
     * @param previousHash
     * @param data
     * @return
     */
    public static Block newBlock(String previousHash, String data) {
        Block block = new Block("", previousHash, data, Instant.now().getEpochSecond(), 0);
        ProofOfWork pow = ProofOfWork.newProofOfWork(block);
        PowResult powResult = pow.run();
        block.setHash(powResult.getHash());
        block.setNonce(powResult.getNonce());
        return block;
    }

    /**
     * 计算区块Hash
     * <p>
     * 注意：在准备区块数据时，一定要从原始数据类型转化为byte[]，不能直接从字符串进行转换
     *
     * @return
     */
    private void setHash() {
        byte[] prevBlockHashBytes = {};
        if (StringUtils.isNoneBlank(this.getPreviousHash())) {
            prevBlockHashBytes = new BigInteger(this.getPreviousHash(), 16).toByteArray();
        }

        byte[] headers = ByteUtils.merge(
                prevBlockHashBytes,
                this.getData().getBytes(),
                ByteUtils.toBytes(this.getTimeStamp()));

        this.setHash(DigestUtils.sha256Hex(headers));
    }
    /**
     * <p> 创建创世区块 </p>
     *
     * @return
     */
    public static Block newGenesisBlock() {
        return Block.newBlock(ZERO_HASH, "Genesis Block");
    }


}