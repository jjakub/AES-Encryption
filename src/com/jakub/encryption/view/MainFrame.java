package com.jakub.encryption.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.jakub.encryption.MainFrameControllerI;
import com.jakub.encryption.controller.MainFrameController;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 7782634818592266720L;

	private final MainFrameControllerI _controller;
	
	JTextField fileField;
	JButton encryptButton;
	JButton decryptButton;
	JButton closeButton;
	JButton findFileButton;
	private final Properties _properties;
	private JPasswordField _passwordField;
	
	public MainFrame( MainFrameControllerI theController, Properties someProperties ) {
		
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e2) {
		}
		
		final MainFrame frame = this;
		
		_properties = someProperties;
		
		_controller = theController;
		
		_passwordField = new JPasswordField( 30 );
		
		_passwordField.addComponentListener(new ComponentAdapter(){  
			  public void componentShown(ComponentEvent ce){  
				 _passwordField.requestFocusInWindow();  
			  }  
			});  
		
		setPreferredSize( new Dimension( 300, 100 ) );
		
		setTitle( "Encryption" );

		fileField = new JTextField( 45 );
		encryptButton = new JButton( "Encrypt" );
		
		encryptButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					_controller.encrypt( fileField.getText(), getPassword() );
					System.exit( 0 );
				}
				catch( Throwable t ){
					System.err.println( "Error! " );
					showError( t );
				}
			}
		});
		
		decryptButton = new JButton( "Decrypt" );
		
		decryptButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					_controller.decrypt( fileField.getText(), getPassword() );
					System.exit( 0 );
				}
				catch( Throwable t ){
					System.err.println( "Error! " );
					showError( t );
				}
			}
		});
		
		closeButton = new JButton( "Close" );
		
		closeButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit( 0 );
			}
		});
		
		findFileButton = new JButton( "Browse" );
		
		findFileButton.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc;
				
				fc = new JFileChooser( _properties.getProperty( "path" ) );
				
				int result = fc.showOpenDialog( frame );
				
				if( JFileChooser.APPROVE_OPTION == result ){
					_properties.setProperty( "path", fc.getSelectedFile().getAbsolutePath() );
					try {
						MainFrameController.saveProperties( _properties );
					} catch (FileNotFoundException e1) {
						showError( e1 );
						System.err.println( "Unable to save properties file. Continuing.");
					} catch (IOException e1) {
						showError( e1 );
						System.err.println( "Unable to save properties file. Continuing.");
					}
					fileField.setText( fc.getSelectedFile().getAbsolutePath() );
				}
			}
		});
		
		JLabel fileLabel = new JLabel( "File:" );

		JPanel mainPanel = new JPanel();
		
		mainPanel.setLayout( new GridBagLayout() );
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 3, 3, 3, 3 );
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		
		mainPanel.add( fileLabel, c );
		
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.gridx = 1;
		c.weightx = 1;
		mainPanel.add( fileField, c );
		
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.gridx = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		mainPanel.add( findFileButton, c );
		
		JPanel buttonPanel = new JPanel( new GridBagLayout() );
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		buttonPanel.add( closeButton, c );

		c.anchor = GridBagConstraints.CENTER;
		c.gridx = 1;
		buttonPanel.add( encryptButton, c );
		
		c.anchor = GridBagConstraints.LINE_END;
		c.gridx = 2;
		buttonPanel.add( decryptButton, c );

		c.anchor = GridBagConstraints.SOUTH;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		
		mainPanel.add( buttonPanel, c );
		
		getContentPane().add( mainPanel );
		pack();
		setLocationRelativeTo(null);

	}
	
	public static void showError( Throwable pError ){
		JOptionPane.showMessageDialog( null, pError.getMessage() );
	}
	
	public char[] getPassword(){
		try{
			JOptionPane pane = new JOptionPane( _passwordField, JOptionPane.OK_CANCEL_OPTION ){
				private static final long serialVersionUID = 7053908039876861108L;

				@Override
				public void selectInitialValue() {
					_passwordField.requestFocusInWindow();
				}
			};
			
			pane.createDialog(null, "Enter password").setVisible(true);
			
			if( JOptionPane.OK_OPTION == ( (Integer)pane.getValue() ).intValue() ){
				return _passwordField.getPassword();
			}
			
			return null;
		}
		finally{
			_passwordField.setText( "" );
		}
	}
}
