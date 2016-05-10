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
 * @author riedener
 *
 */
public class ReadConfigFile {

	private String CompanyID = "";
	private String IPRange = "";
	private String StatusText = "unknown state";
	
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
		stream = new BufferedInputStream(new FileInputStream("./localconfig/ConfScanner.properties"));
	}
	
	public void readParameters() throws IOException
	{
		try
		{
			properties.load(stream);
			this.CompanyID = properties.getProperty("CompanyID");	
			this.IPRange = properties.getProperty("IPRange");
			setStatusTxt("ok");
			stream.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatusTxt("loading failed");
		}
	}
	
	public void writeParameter(String _CompID, String _IPRange) throws IOException
	{
		loadParameterFile();
		try
		{
			properties.setProperty("CompanyID", _CompID);	
			properties.setProperty("IPRange", _IPRange);	
	        File f = new File("./localconfig/ConfScanner.properties");
	        OutputStream out = new FileOutputStream( f );
	        properties.store(out, "This is an optional header comment string");
			
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
