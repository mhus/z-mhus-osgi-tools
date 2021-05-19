/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.dev.dev.testit;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import de.mhus.lib.core.MCast;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.crypt.Blowfish;
import de.mhus.lib.core.crypt.MBouncy;
import de.mhus.lib.core.crypt.MCrypt;
import de.mhus.lib.core.util.Base64;

// shityo crypt blowfish.type.encrypt Blowfish/ECB/PKCS5Padding "" asdf HelloWorld

public class CryptShit implements ShitIfc {

    @Override
    public void printUsage() {
        System.out.println("blowfish.encrypt <passphrase> <base64>");
        System.out.println("blowfish.decrypt <passphrase> <base64>");
        System.out.println("blowfish.string.encrypt <passphrase> <String>");
        System.out.println("blowfish.string.decrypt <passphrase> <String>");
        System.out.println("blowfish.type.encrypt <type> <provider> <passphrase> <String>");
        System.out.println("blowfish.type.decrypt <type> <provider> <passphrase> <String>");
        System.out.println("       e.g. type: Blowfish/CBC/PKCS5Padding");
        System.out.println("       e.g. provider: empty or BC (use jcainfo)");
        System.out.println("base64.decode <base64 string>");
        System.out.println("base64.encode <Hex string: FF FF FF FF>");
        System.out.println("hmacsha1 <key> <text>");
    }

    @Override
    public Object doExecute(CmdShitYo base, String cmd, String[] parameters) throws Exception {
        switch (cmd) {
            case "aes.string.encrypt":
                {
                    String md5 = MCrypt.md5(parameters[0]);
                    byte[] key = MCast.hexStringToByteArray(md5);

                    System.out.println("KeyMD5   : " + md5);
                    System.out.println("Key      : " + Arrays.toString(key));
                    System.out.println("KeyBase64: " + Base64.encode(key));
                    //			System.out.println("KeyLength: " + key.length + " (" + (key.length*8) + "
                    // bytes)");
                    byte[] enc = MBouncy.encryptAes(key, parameters[1].getBytes("utf-8"));
                    System.out.println(
                            "Real     : " + Base64.encode(parameters[1].getBytes("utf-8")));
                    System.out.println("Encoded  : " + Base64.encode(enc));
                }
                break;
            case "md5":
                {
                    String out = MCrypt.md5(parameters[0]);
                    System.out.println(out);
                }
                break;
            case "blowfish.encrypt":
                {
                    byte[] strClearText = Base64.decode(parameters[1]);
                    byte[] strEncText = Blowfish.encrypt(strClearText, parameters[0]);
                    System.out.println();
                    System.out.println(Base64.encode(strEncText));
                }
                break;
            case "blowfish.decrypt":
                {
                    byte[] strEncText = Base64.decode(parameters[1]);
                    byte[] strClearText = Blowfish.decrypt(strEncText, parameters[0]);
                    System.out.println();
                    System.out.println(Base64.encode(strClearText));
                }
                break;
            case "blowfish.string.encrypt":
                {
                    byte[] strClearText = parameters[1].getBytes("utf-8");
                    byte[] strEncText = Blowfish.encrypt(strClearText, parameters[0]);
                    System.out.println();
                    System.out.println("Base64 : " + Base64.encode(strClearText));
                    System.out.println("Encoded: " + Base64.encode(strEncText));
                }
                break;
            case "blowfish.string.decrypt":
                {
                    byte[] strEncText = Base64.decode(parameters[1]);
                    byte[] strClearText = Blowfish.decrypt(strEncText, parameters[0]);
                    System.out.println();
                    System.out.println(new String(strClearText, "utf-8"));
                }
                break;
            case "blowfish.type.encrypt":
                {
                    String provider = parameters[1];
                    byte[] strClearText = parameters[3].getBytes("utf-8");
                    SecretKeySpec skeyspec =
                            new SecretKeySpec(parameters[2].getBytes("utf-8"), parameters[0]);
                    Cipher cipher =
                            provider.length() == 0
                                    ? Cipher.getInstance(parameters[0])
                                    : Cipher.getInstance(parameters[0], provider);
                    cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
                    byte[] strEncText = cipher.doFinal(strClearText);
                    System.out.println();
                    System.out.println("Base64 : " + Base64.encode(strClearText));
                    System.out.println("Encoded: " + Base64.encode(strEncText));
                }
                break;
            case "blowfish.type.decrypt":
                {
                    String provider = parameters[1];
                    byte[] strEncText = Base64.decode(parameters[3]);
                    SecretKeySpec skeyspec =
                            new SecretKeySpec(parameters[2].getBytes("utf-8"), parameters[0]);
                    Cipher cipher =
                            provider.length() == 0
                                    ? Cipher.getInstance(parameters[0])
                                    : Cipher.getInstance(parameters[0], provider);
                    cipher.init(Cipher.DECRYPT_MODE, skeyspec);
                    byte[] strClearText = cipher.doFinal(strEncText);
                    System.out.println();
                    System.out.println(new String(strClearText, "utf-8"));
                }
                break;
            case "base64.decode":
                {
                    byte[] out = java.util.Base64.getDecoder().decode(parameters[0]);
                    System.out.println(MString.toHexDump(out, 50));
                }
                break;
            case "base64.encode":
                {
                    String[] parts = parameters[0].split(" ");
                    byte[] data = new byte[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        data[i] = (byte) MCast.tointFromHex(parts[i]);
                    String out = java.util.Base64.getEncoder().encodeToString(data);
                    System.out.println(out);
                }
                break;
            case "hmacsha1":
                { // RFC2104HMAC
                    SecretKeySpec signingKey =
                            new SecretKeySpec(parameters[0].getBytes(), "HmacSHA1");
                    Mac mac = Mac.getInstance("HmacSHA1");
                    mac.init(signingKey);
                    System.out.println(MCast.toHexString(mac.doFinal(parameters[1].getBytes())));
                }
                break;
            case "aes.encrypt":
                {
                    System.out.println(
                            encrpytString(
                                    parameters[0],
                                    parameters[1],
                                    parameters[2])); // not working correct ...
                }
                break;
            case "aes.decrypt":
                {
                    System.out.println(
                            decrpytString(
                                    parameters[0],
                                    parameters[1],
                                    parameters[2])); // not working correct ...
                }
                break;
            default:
                System.out.println("Command unknown");
        }
        return null;
    }

    public static String encrpytString(String passwd, String salt, String input) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(passwd.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        // byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        byte[] encryptedTextBytes = cipher.doFinal(input.getBytes("UTF-8"));
        return DatatypeConverter.printBase64Binary(encryptedTextBytes);
    }

    public static String decrpytString(String passwd, String salt, String input) throws Exception {
        byte[] encryptedTextBytes = DatatypeConverter.parseBase64Binary(input);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(passwd.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));
        byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        return new String(decryptedTextBytes);
    }
}
