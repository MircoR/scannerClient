/**
 * 
 */
package zbw.ch.sysVentory;

import java.io.IOException;

/**
 * @author riedener
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
		
		// Read localconfig file 
		ReadConfigFile localConfig = new ReadConfigFile();
		
		// show state of local config file
		gui.setStatusLocalConfig(localConfig.getStatusTxt());
		
		ExecutePowershellScript script = new ExecutePowershellScript();
		script.runScript();

		gui.setStatusLocalConfig("ps ausgeführt");
		
		
	}
	
	
}
