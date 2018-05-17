package com.freshdirect.fdstore.ewallet.util;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.freshdirect.fdstore.FDStoreProperties;


public class EWalletCryptoUtil
{
	private static final String ALGO = FDStoreProperties.getEwalletEncryptionAlgorithm();
    //generate 128bit key
    private static final String keyStr = FDStoreProperties.getEwalletEncryptionKey();

    private static Key generateKey() throws Exception {
        byte[] keyValue = keyStr.getBytes("UTF-8");
        Key key=null;
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			keyValue = sha.digest(keyValue);
			keyValue = Arrays.copyOf(keyValue, 16); // use only first 128 bit       
			key = new SecretKeySpec(keyValue, ALGO);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return key;
    }

    public static String encrypt(String Data) throws Exception {
            String encryptedValue=null;
			try {
				Key key = generateKey();
				Cipher c = Cipher.getInstance(ALGO);
				c.init(Cipher.ENCRYPT_MODE, key);
				byte[] encVal = c.doFinal(Data.getBytes());
				encryptedValue = DatatypeConverter.printBase64Binary(encVal);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return encryptedValue;
    }

    public static String decrypt(String encryptedData) throws Exception {
        String decryptedValue=null;
		try {
			Key key = generateKey();
			Cipher c = Cipher.getInstance(ALGO);
			c.init(Cipher.DECRYPT_MODE, key);       
			byte[] decordedValue = DatatypeConverter.parseBase64Binary(encryptedData);
			byte[] decValue = c.doFinal(decordedValue);
			decryptedValue = new String(decValue);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return decryptedValue;
    }
}
