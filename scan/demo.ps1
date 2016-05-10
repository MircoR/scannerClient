<#
.SYNOPSIS
Get Client Information
.DESCRIPTION
Beschreibung...
.NOTES  
Bemerkungen...
#>

#Get the client list
$clients = Get-Content .\Clients.txt

#Run the commands for each client in the list
$infoColl = @()

Foreach ($client in $clients)
{
	$OSInfo = Get-WmiObject Win32_OperatingSystem -ComputerName $client #Get OS Information
    #$SWInfo = Get-WmiObject -Class Win32_Product -ComputerName $client #Get SW Information
    $CPUInfo = Get-WmiObject Win32_Processor -ComputerName $client #Get CPU Information
	$RAMInfo = Get-WmiObject CIM_PhysicalMemory -ComputerName $client | Measure-Object -Property capacity -Sum | % { [Math]::Round(($_.sum / 1GB), 2) } #Get RAM Information rounded
	$NetInfo = Get-WmiObject Win32_NetworkAdapterConfiguration -ComputerName $client -EA Stop | ? {$_.IPEnabled} #Get Network Adapter Information
    $ComputerInfo = Get-WmiObject Win32_Computersystem -ComputerName $client #Get Computer Information
    $LocalAccountSID = Get-WmiObject -Query "SELECT SID FROM Win32_UserAccount WHERE LocalAccount = 'True'" |Select-Object -First 1 -ExpandProperty SID #Get the local SID by finding a local account
    $MachineSID = ($temp = $LocalAccountSID -split "-")[0..($temp.Length-2)]-join"-" #Remove Relative ID (RID) from local Account SID = Machine SID
        
Foreach ($CPU in $CPUInfo)
	{
		$infoObject = New-Object PSObject #Create new object from class PSObject

		#The following add data to the infoObjects:

        #Betriebssystem
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "Betriebssystem" -value $OSInfo.Caption
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "Betriebssystemtyp" -value $OSInfo.OSArchitecture

        #Programme
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "Programme" -value $SWInfo.Name
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "Version" -value $SWInfo.Version
        
        #Prozessor(Prozessorfamilie, Anzahl Kerne, Taktrate)
		Add-Member -inputObject $infoObject -memberType NoteProperty -name "Prozessor" -value $CPUInfo.Name
		Add-Member -inputObject $infoObject -memberType NoteProperty -name "Taktfrequenz GHz" -value ($CPUInfo.MaxClockSpeed/1000)
		Add-Member -inputObject $infoObject -memberType NoteProperty -name "Anzahl Kerne" -value $CPUInfo.NumberOfCores
		
        #Drucker(Liste registrierter Drucker, Treiber, Version)

        #Computer(SID, Name, IP-Adresse, RAM, Computertyp)
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "SID" -value $MachineSID
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "Name" -value $CPUInfo.SystemName
        Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "IP-Adresse" -Value $NetInfo.IpAddress
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "RAM GB" -value $RAMInfo
        Add-Member -inputObject $infoObject -memberType NoteProperty -name "Computertyp" -value $ComputerInfo.Model

        #NIC(IPV4/6, MAC, Bezeichnung, Gateway, DHCP, Subnet)
        #IPV4/6
        Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "MAC" -Value $NetInfo.MACAddress
        Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "Bezeichnung" -Value $NetInfo.Description
        Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "Gateway" -Value $NetInfo.DefaultIPGateway[0]
        Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "DHCP" -Value $NetInfo.DHCPEnabled
        Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "Subnet" -Value $NetInfo.IPSubnet
        
        $infoObject #Output to the screen for a visual feedback
		$infoColl += $infoObject #Add infoObject to collection
	}
}

$infoColl | export-clixml -path .\Server_Inventory_$((Get-Date).ToString('MM-dd-yyyy')).xml
$infoColl > screen_output.txt