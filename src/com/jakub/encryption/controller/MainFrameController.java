package com.jakub.encryption.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JFrame;

import org.apache.commons.codec.DecoderException;

import com.jakub.encryption.CryptoEngine;
import com.jakub.encryption.MainFrameControllerI;
import com.jakub.encryption.view.MainFrame;

public class MainFrameController implements MainFrameControllerI {

	private final MainFrame _frame;
	private final Properties _props;

	private static final int SIZE = 4096;

	public static final String PROPERTIES_FILE_PATH = "key.prefs";

	public MainFrameController(Properties someProperties, boolean setVisible) {
		_props = someProperties;
		_frame = new MainFrame(this, someProperties);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setVisible(setVisible);
	}

	public static void saveProperties(Properties pProperties)
			throws FileNotFoundException, IOException {
		pProperties
				.storeToXML(new FileOutputStream(PROPERTIES_FILE_PATH), null);
	}

	public void decrypt(String aFile) throws IOException, DecoderException,
			InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeySpecException,
			InvalidParameterSpecException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {
		if (_props.getProperty("password") != null) {
			decrypt(aFile, _props.getProperty("password").toCharArray());
		} else {
			decrypt(aFile, _frame.getPassword());
		}
	}

	public void encrypt(String aFile) throws DecoderException, IOException,
			InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeySpecException,
			InvalidParameterSpecException, IllegalBlockSizeException,
			BadPaddingException {
		if (_props.getProperty("password") != null) {
			encrypt(aFile, _props.getProperty("password").toCharArray());
		} else {
			encrypt(aFile, _frame.getPassword());
		}
	}

	@Override
	public void encrypt(String aFile, char[] aPassword)
			throws DecoderException, IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeySpecException, InvalidParameterSpecException,
			IllegalBlockSizeException, BadPaddingException {
		if (null == aFile || !new File(aFile).exists()) {
			throw new RuntimeException("File does not exist!");
		}

		byte[] key = CryptoEngine.hexDecode(_props.getProperty("key"));

		File f = new File(aFile);
		FileInputStream fis = null;

		byte[] clear = new byte[(int) f.length()];
		try {
			fis = new FileInputStream(f);
			int read = 0;
			int total = 0;

			while (0 != (read = fis.read(clear, total,
					Math.min(SIZE, clear.length - total)))) {
				total += read;
			}
		} finally {
			if (null != fis) {
				fis.close();
			}
		}

		byte[] encrypted = CryptoEngine.encrypt(clear, aPassword, key);

		FileOutputStream fos = null;
		try {
			int written = 0;
			fos = new FileOutputStream(aFile + ".enc");
			while (written < encrypted.length) {
				fos.write(encrypted, written,
						Math.min(SIZE, encrypted.length - written));
				written += Math.min(SIZE, encrypted.length - written);
			}
		} finally {
			if (null != fos) {
				fos.close();
			}
		}
	}

	@Override
	public void decrypt(String aFile, char[] aPassword) throws IOException,
			DecoderException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeySpecException,
			InvalidParameterSpecException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {
		if (null == aFile || !new File(aFile).exists()) {
			throw new RuntimeException("File does not exist!");
		}

		byte[] key = CryptoEngine.hexDecode(_props.getProperty("key"));

		File f = new File(aFile);
		FileInputStream fis = null;

		byte[] encrypted = new byte[(int) f.length()];
		try {
			fis = new FileInputStream(f);
			int read = 0;
			int total = 0;

			while (0 != (read = fis.read(encrypted, total,
					Math.min(SIZE, encrypted.length - total)))) {
				total += read;
			}
		} finally {
			if (null != fis) {
				fis.close();
			}
		}

		byte[] clear = CryptoEngine.decrypt(encrypted, aPassword, key);

		FileOutputStream fos = null;

		try {
			int written = 0;

			fos = new FileOutputStream(aFile.replaceAll("\\.enc$", ""));

			while (written < clear.length) {
				fos.write(clear, written,
						Math.min(SIZE, clear.length - written));
				written += Math.min(SIZE, clear.length - written);
			}
		} finally {
			if (null != fos) {
				fos.close();
			}
		}

	}

	public void setVisible(boolean b) {
		_frame.setVisible(b);
	}

}
