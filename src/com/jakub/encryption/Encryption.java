package com.jakub.encryption;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.SwingUtilities;

import com.jakub.encryption.controller.MainFrameController;

public class Encryption {
	
	static boolean setVisible = true;

	public static void main(String[] args) {
		
		final Properties p = new Properties();
		
		final MainFrameController main = new MainFrameController( p, false );

		loadProperties( p );
		
		if( args.length > 0 ){
			
			if( args[ 0 ].equalsIgnoreCase( "gen" ) ){
				int length = 128;
				if( args.length == 1 ){
					System.out.println( "Using default size = " + length );
				}
				else{
					if( !args[ 1 ].equals( "128" ) &&  !args[ 1 ].equals( "192" ) &&  !args[ 1 ].equals( "256" ) ){
						System.err.println( "Unsupported key size " + args[ 1 ] );
						System.exit( 3 );
					}
					length = Integer.parseInt( args[ 1 ] );
				}
	
				System.out.println( String.format( "Generating a new %1$s-bit AES key", Integer.toString( length ) ) );
				try {
					String key = CryptoEngine.hexEncode( CryptoEngine.generate( length ).getEncoded() );
					System.out.println( "Generated key (hex-encoded): " + key );
					p.setProperty( "key", key );
					MainFrameController.saveProperties( p );
				} catch (NoSuchAlgorithmException e) {
					System.err.println( "Not supported algorithm. " + e.getMessage() );
					System.exit( 4 );
				} catch (FileNotFoundException e) {
					System.err.println( "Unable to save properties. File not found: " + e.getMessage() );
				} catch (IOException e) {
					System.err.println( "Unable to save properties. IOException: " + e.getMessage() );
				}
			}
			else if( args[ 0 ].startsWith( "password" ) ){
				p.setProperty( "password", args[ 0 ].substring( 9, args[ 0 ].length() ) );
			}
			else if( args.length == 1 ){
				p.setProperty( "key", args[ 0 ] );
	
			}
			String file = "";
			
			for( int i = 1; i < args.length; i++ ){
				file += args[ i ] + " ";
			}
			
			if( "decrypt".equalsIgnoreCase( args[ 0 ] ) ){
				try {
					main.decrypt( file.trim() );
					System.exit( 0 );
				} catch (Exception e) {
					System.err.println( "Unable to decrypt. " + e.getMessage() );
					System.exit( 10 );
				}
			}
			else if( "encrypt".equalsIgnoreCase( args[ 0 ] ) ){
				try {
					main.encrypt( file.trim() );
					System.exit( 0 );
				} catch (Exception e) {
					System.err.println( "Unable to encrypt. " + e.getMessage() );
					e.printStackTrace();
					System.exit( 10 );
				}
			}
		}
		
		SwingUtilities.invokeLater( new Runnable() {
			
			@Override
			public void run() {
				main.setVisible( setVisible );
			}
		});

	}
	
	private static void loadProperties( Properties p ){
		try{
			p.loadFromXML( new FileInputStream( MainFrameController.PROPERTIES_FILE_PATH ) );
			System.out.println( String.format( "Property file '%1$s' loaded.", MainFrameController.PROPERTIES_FILE_PATH ) );
			if( !p.containsKey( "key" ) ){
				throw new InvalidPropertiesFormatException( "Key 'key' not found!" );
			}
		}
		catch( InvalidPropertiesFormatException e ){
			System.err.println( String.format( "Property file '%1$s' is invalid. Either use 'gen' to generate a key, or provide that key to work with it.", MainFrameController.PROPERTIES_FILE_PATH ) );
			System.exit( 1 );
		}
		catch( IOException e ){
			System.err.println( String.format( "Property file '%1$s' not found. Either use 'gen' to generate a key, or provide that key to work with it.", MainFrameController.PROPERTIES_FILE_PATH ) );
			System.exit( 2 );
		}
	}
}