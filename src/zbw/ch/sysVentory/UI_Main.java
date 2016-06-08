/**
 * 
 */
package zbw.ch.sysVentory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.net.*;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.*;
import javax.xml.bind.Marshaller.Listener;

/**
 * @author riedener
 *
 */
public class UI_Main extends JFrame implements ActionListener
{
//	static String configStatus = "";

	JFrame mainFrm;
	// Declare and create panels
	JPanel line1;
	JPanel line2;
	JPanel line3;

		
	JLabel configTxt;
	JLabel ConnStatusTxt;
	JLabel ScanStatusTxt;
	JTextField statusConn;
	JTextField localConfig;
	JTextField statusScan;
	
	public UI_Main()
	{		
		//GUI erstellen
		mainFrm = new JFrame("sysVentory_scanner");
		line1 = new JPanel();
		line2 = new JPanel();
		line3 = new JPanel();
				
		configTxt = new JLabel("read local Config");
		localConfig = new JTextField("");//localhost");

		
		ConnStatusTxt = new JLabel("Connection Status");
		statusConn = new JTextField();
		
		ScanStatusTxt = new JLabel("Scan status");
		statusScan = new JTextField();
		
		guiInit();		

		pack();
	}
	
	public void guiInit()
	{
		// Initialise frame and GUI elements
		//mainFrm.setBackground(Color.lightGray);
		mainFrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrm.setResizable(true);
				
		mainFrm.setLayout(new GridLayout(4,1));
		mainFrm.add(line1, BorderLayout.NORTH);
		mainFrm.add(line2, BorderLayout.NORTH);
	    mainFrm.add(line3, BorderLayout.NORTH);
	
	    
		line1.setLayout(new GridLayout(1,1));
		line2.setLayout(new GridLayout(1,1));
		line3.setLayout(new GridLayout(1,1));
		
		line1.add(configTxt);
		line1.add(localConfig);
		
		line2.add(ConnStatusTxt);
		line2.add(statusConn);
		
		line3.add(ScanStatusTxt);
		line3.add(statusScan);

		
		// Add frame
		mainFrm.setSize(500, 150);
		mainFrm.setVisible(true);	
	//	mainFrm.setLocationRelativeTo(getContentPane());
		
	}

	public void actionPerformed(ActionEvent event)
	{
	
	}
	
	public void setStatusLocalConfig(String _setStatus)
	{
		localConfig.setText(_setStatus);
	}
	public void setStatusConn(String _setStatus)
	{
		statusConn.setText(_setStatus);
	}
	public void setStatusScan(String _setStatus)
	{
		statusScan.setText(_setStatus);
	}
	
}
