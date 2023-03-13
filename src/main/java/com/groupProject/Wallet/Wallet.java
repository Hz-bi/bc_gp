package com.groupProject.Wallet;

import com.groupProject.utils.Base58Check;
import com.groupProject.utils.BtcAddressUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;

@Data
@AllArgsConstructor
@Slf4j
public class Wallet implements Serializable {

    private static final long serialVersionUID = 166249065006236265L;

    // The Length of the Check Digits
    private static final int ADDRESS_CHECKSUM_LEN = 4;
    /**
     * Private Key
     */
    private PrivateKey privateKey;
    /**
     * Public Key
     */
    private byte[] publicKey;

    public Wallet() {
        initWallet();
    }

    /**
     * Initiate the wallet
     */
    private void initWallet() {
        try {
            KeyPair keyPair = newECKeyPair();
            BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
            BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

            byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);

            this.setPrivateKey(privateKey);
            this.setPublicKey(publicKeyBytes);
        } catch (Exception e) {
            log.error("Fail to init wallet ! ", e);
            throw new RuntimeException("Fail to init wallet ! ", e);
        }
    }

    /**
     * Create a new key pair
     *
     * @return
     * @throws Exception
     */
    private KeyPair newECKeyPair() throws Exception {
        // Register for BC Provider
        Security.addProvider(new BouncyCastleProvider());
        // Create Elliptic Curve Algorithm key pair generator by using ECDSA Algorithm
        KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        // Set the domain parameter for the Elliptic Curve
        // The reason why bitcoin choose secp256k1, details are in: https://bitcointalk.org/index.php?topic=151120.0
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
        g.initialize(ecSpec, new SecureRandom());
        return g.generateKeyPair();
    }


    /**
     * Get the wallet address
     *
     * @return
     */
    public String getAddress() {
        try {
            // 1. Get Ripemd HashedKey
            byte[] ripemdHashedKey = BtcAddressUtils.ripeMD160Hash(this.getPublicKey());

            // 2. Add a new version 0x00
            ByteArrayOutputStream addrStream = new ByteArrayOutputStream();
            addrStream.write((byte) 0);
            addrStream.write(ripemdHashedKey);
            byte[] versionedPayload = addrStream.toByteArray();

            // 3. Calculate the check digits
            byte[] checksum = BtcAddressUtils.checksum(versionedPayload);

            // 4. Get the combination of version + paylod + checksum
            addrStream.write(checksum);
            byte[] binaryAddress = addrStream.toByteArray();

            // 5. Perform Base58 conversion processing
            return Base58Check.rawBytesToBase58(binaryAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Fail to get wallet address ! ");
    }

}
