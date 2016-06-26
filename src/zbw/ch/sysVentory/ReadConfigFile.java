/**
 * 
 */
package zbw.ch.sysVentory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.util.Properties;

/**
 * @author M. Riedener
 *
 */
public class ReadConfigFile {

	public String CompanyID = "";
	public String ServerIP = "";
	public String ConnRefreshTime = "";
	private String StatusText = "unknown state";
	private static String filepath = "./localconfig/ConfScanner.properties"; 
	
	private Properties properties; 
	BufferedInputStream stream;	
	
	public ReadConfigFile()
	{
		try {
			loadParameterFile();
			readParameters();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.setStatusTxt("file not found");
			e.printStackTrace();
		}
	}
	
	public void loadParameterFile() throws IOException
	{
		properties = new Properties();
		stream = new BufferedInputStream(new FileInputStream(filepath));
	}
	
	public void readParameters() throws IOException
	{
		try
		{
			properties.load(stream);
			this.CompanyID = properties.getProperty("CompanyID");
			this.ServerIP = properties.getProperty("ServerIP");
			this.ConnRefreshTime = properties.getProperty("ConnRefreshTime");
			setStatusTxt("ok -> " + "ConnRefresh: " + ConnRefreshTime);
			stream.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatusTxt("loading failed");
		}
	}
	
	public void writeParameter(String _CompID) throws IOException
	{
		loadParameterFile();
		try
		{
			properties.setProperty("CompanyID", _CompID);		
	        File f = new File("filepath");
	        OutputStream out = new FileOutputStream( f );
			
			out.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatusTxt("loading failed");
		}		
	}
	
	public String getStatusTxt() {

		return this.StatusText;
	}
	
	public void setStatusTxt(String _txt)
	{
		this.StatusText = _txt;
	}
	
}
