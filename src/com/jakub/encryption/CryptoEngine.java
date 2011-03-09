package com.jakub.encryption;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class CryptoEngine {

	public static SecretKey generate( int pSize ) throws NoSuchAlgorithmException{
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init( pSize );

		SecretKey skey = kgen.generateKey();
		return skey;
	}
	
	public static String hexEncode( byte[] anArray ){
		return Hex.encodeHexString( anArray );
	}
	
	public static byte[] hexDecode( String aHexEncodedArray ) throws DecoderException{
		return Hex.decodeHex( aHexEncodedArray.toCharArray() );
	}
	
	private static byte[] toByteArray( char[] pArray ){
		byte[] result = new byte[ pArray.length ];

		for( int i = 0; i < pArray.length; i++ ){
			result[ i ] = (byte)pArray[ i ];
		}
		
		return result;
	}

	private static byte[] getIv( byte[] pAll ){
		byte[] iv = new byte[ 16 ];
		System.arraycopy( pAll, 0, iv, 0, iv.length );
		return iv;
	}
	
	private static byte[] getMessage( byte[] pAll ){
		byte[] message = new byte[ pAll.length - 16 ];
		System.arraycopy( pAll, 16, message, 0, message.length );
		return message;
	}
	
	public static byte[] decrypt( byte[] pEncrypted, char[] pPassword, byte[] pKey ) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		SecretKey spec;
		
		if( null == pPassword || pPassword.length == 0 ){
			spec = new SecretKeySpec( pKey, "AES" );
		}
		else{
			MessageDigest sha256 = MessageDigest.getInstance( "SHA-256" );
			sha256.reset();
			byte[] digest = sha256.digest( toByteArray( pPassword ) );
			spec = new SecretKeySpec( xor( pKey, digest ), "AES" );
		}

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, spec, new IvParameterSpec( getIv( pEncrypted ) ) );
		return cipher.doFinal( getMessage( pEncrypted ) );
	}
	
	private static byte[] xor( byte[] aA, byte[] aB ){
		byte[] a;
		byte[] b;
		byte[] result;
		
		if( aA.length <= aB.length ){
			a = aA;
			b = aB;
		}
		else{
			a = aB;
			b = aA;
		}
		
		result = new byte[ a.length ];

		for( int i = 0; i < a.length; i++ ){
			result[ i ] = (byte) (a[ i ] ^ b[ i % b.length ]);
		}
		
		return result;
	}
	
	private static byte[] concatenate( byte[] p1, byte[] p2 ){
		byte[] result = new byte[ p1.length + p2.length ];
		
		System.arraycopy( p1, 0, result, 0, p1.length );
		System.arraycopy( p2, 0, result, p1.length, p2.length );
		
		return result;
	}
	
	public static byte[] encrypt( byte[] pClear, char[] pPassword, byte[] pKey ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
		SecretKey spec;
		
		if( null == pPassword || pPassword.length == 0 ){
			spec = new SecretKeySpec( pKey, "AES" );
		}
		else{
			MessageDigest sha256 = MessageDigest.getInstance( "SHA-256" );
			sha256.reset();
			byte[] digest = sha256.digest( toByteArray( pPassword ) );
			spec = new SecretKeySpec( xor( pKey, digest ), "AES" );
		}

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, spec);
		AlgorithmParameters params = cipher.getParameters();
		byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		return concatenate( iv, cipher.doFinal( pClear ) );
	}
}
