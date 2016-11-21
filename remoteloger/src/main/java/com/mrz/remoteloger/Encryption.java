package com.mrz.remoteloger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by GaoShan on 2016/1/15.
 */
public class Encryption {

    public static String encrypt(String s,String checkCode) throws Exception {
        return encrypt1(s,checkCode);
    }

    /**
     * @param s
     * @param checkCode
     * @return
     * @throws Exception
     */
    public static String encrypt1(String s,String checkCode) throws Exception {
        String TYPE= "AES/ECB/PKCS5Padding";
        try {
            String data=s;
            String key = checkCode;
            Cipher cipher = Cipher.getInstance(TYPE);
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = data.getBytes("UTF-8");
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keyspec);
            byte[] encrypted = cipher.doFinal(plaintext);
            return parseByte2HexStr(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Byte transfer to hex
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }


    public static String desEncrypt(String data,String checkCode) throws Exception {
        try {
            String key = checkCode;
            String iv = checkCode;
            byte[] encrypted1 = parseHexStr2Byte(data);
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * hex transfer to byte
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}

