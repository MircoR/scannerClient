package zbw.ch.sysVentory;

import java.util.concurrent.TimeUnit;

public class ExecutePowershellScript {

	public ExecutePowershellScript()
	{
		
	}
	
	public boolean runScript()
	{
		PowerShell powerShell = null;
		
		try {
			powerShell = PowerShell.openSession();

			long t0 = System.currentTimeMillis();
			
			String iprange = "./scan/Get-inventory.ps1 172.16.4.132/32";
			System.out.println(powerShell.executeCommand(iprange).getCommandOutput());
			
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

