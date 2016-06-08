package zbw.ch.sysVentory;

import java.util.concurrent.TimeUnit;

public class ExecutePowershellScript
{
	public GlobalData gData;
	
	public ExecutePowershellScript(GlobalData _gData)
	{
		this.gData = _gData;
	}
	
	public boolean runScript() throws InterruptedException
	{
		PowerShell powerShell = null;
		
		try {
			powerShell = PowerShell.openSession();

			long t0 = System.currentTimeMillis();
			
			String cmdParam = "./scan/PSinventory.ps1 " + this.gData.getIprange() + " " + this.gData.getCompId();
			System.out.println(cmdParam);
			System.out.println(powerShell.executeCommand(cmdParam).getCommandOutput());
			
			//powerShell.wait();			
			powerShell.close();	


			long t1 = System.currentTimeMillis();
			long duration = TimeUnit.MILLISECONDS.toSeconds( t1 - t0 );
			System.out.println(duration);
			
			
		} catch (PowerShellNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return true;
	}
}

