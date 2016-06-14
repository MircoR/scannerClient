/**
 * 
 */
package zbw.ch.sysVentory;
 
import java.io.IOException;

/**
 * @author M. Riedener
 *
 */
public class Main {
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// build GUI
		UI_Main gui = new UI_Main();
		GlobalData gData = new GlobalData();
		
		// Read localconfig file 
		ReadConfigFile localConfig = new ReadConfigFile();
		gData.setCompId(localConfig.CompanyID);
		
		PostXML post = new PostXML(gData);
		
		post.getFileName();
		post.deleteXMLFiles();
/*
		
		// show state of local config file
		gui.setStatusLocalConfig(localConfig.getStatusTxt());
		
//		GetDataConn datacon = new GetDataConn(); 
		Thread_Connection thread1_Conn = new Thread_Connection(1000, gData, gui); 
		thread1_Conn.start();


		Thread_Scan thread2_Scan = new Thread_Scan(gData, gui); 
		thread2_Scan.start();
	*/
	}		

	

}
