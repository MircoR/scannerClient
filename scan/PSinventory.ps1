<#
.SYNOPSIS
Get client system information (inventory)
.DESCRIPTION
Der Powershell Skript durchsucht das Netzwerk nach aktiven WMI-Clients und fragt eine Liste von definierten Informationen ab.
Beim starten des Skripts werden zwei Übergabeparameter erwartet: 1. Netzwerkgrösse (CIDR schreibweise) und 2. GUID (eindeutige Company-Nr.).
Am Schluss des Durchlaufs werden die Informationen in eine XML-Datei geschrieben und an den Sysinventory-Server schickt.
.NOTES  
Technische Unterstützung und Anwendungssupport erhalten Sie unter support@sysventory.com oder telefonisch +41719005050.
.VERSION  
1.0
#>

param(
    [string]$networkId,
    [string]$companyId
)

#Define log file and time stamp
$LogTime = Get-Date -Format "yyyy-MM-dd hh:mm:ss"
$LogFile = '.\scan\' + $companyId + "_" + $LogTime + ".log"

#Get the client list
$scannet = .\scan\PSipcalc.ps1 -NetworkAddress $networkId -Enumerate
$clients = $scannet.IPEnumerated

#XML this is where the document will be saved
$Path = '.\scan\' + $companyId + '.xml'
 
#XML get an XMLTextWriter to create the XML
$XmlWriter = New-Object System.XMl.XmlTextWriter($Path,$Null)
 
#XML choose a pretty formatting:
$xmlWriter.Formatting = 'Indented'
$xmlWriter.Indentation = 1
$XmlWriter.IndentChar = "`t"
 
#XML write the header
$xmlWriter.WriteStartDocument()
 
#XML set XSL statements
$xmlWriter.WriteProcessingInstruction("xml-stylesheet", "type='text/xsl' href='style.xsl'")

#Create root element "scanitem" and add some attributes to it
$XmlWriter.WriteComment('List of scanitems')
$xmlWriter.WriteStartElement('scanitem')
$xmlWriter.WriteElementString('userid',$companyId)
$xmlWriter.WriteElementString('scandatetime',$LogTime)

#Run the commands for each client in the list
$infoColl = @()

Foreach ($client in $clients)
{
    #Variable to check if it's a WMI client or not
    $wmi = gwmi win32_bios -ComputerName $client -ErrorAction SilentlyContinue

    #Check if client is reachable (ping) and WMI = true
    If ((Test-Connection $client -count 1 -quiet) -and ($wmi))
    {
        #Define the variable to hold the location of Currently Installed Programs
        $UninstallKey=”SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall”
  
        #Create an instance of the Registry Object and open the HKLM base key
        $reg=[microsoft.win32.registrykey]::OpenRemoteBaseKey(‘LocalMachine’,$client)  
        #Drill down into the Uninstall key using the OpenSubKey Method
        $regkey=$reg.OpenSubKey($UninstallKey)
        #Retrieve an array of string that contain all the subkey names
        $subkeys=$regkey.GetSubKeyNames()
  
        #Gather necessary information and write in variables
		$OSInfo = Get-WmiObject Win32_OperatingSystem -ComputerName $client #Get OS Information
        $CPUInfo = Get-WmiObject Win32_Processor -ComputerName $client #Get CPU Information
	    $RAMInfo = Get-WmiObject CIM_PhysicalMemory -ComputerName $client | Measure-Object -Property capacity -Sum | % { [Math]::Round(($_.sum / 1GB), 2) } #Get RAM Information rounded
	    $NetInfo = Get-WmiObject Win32_NetworkAdapterConfiguration -ComputerName $client -EA Stop | ? {$_.IPEnabled} #Get Network Adapter Information
        $ComputerInfo = Get-WmiObject Win32_Computersystem -ComputerName $client #Get Computer Information
        $MachineSID = (New-Object System.Security.Principal.NTAccount($CPUInfo.SystemName+'$')).Translate([System.Security.Principal.SecurityIdentifier]).value #Remove Relative ID (RID) from local Account SID = Machine SID
		
        Foreach ($CPU in $CPUInfo)
	    {
            #Create new object from class PSObject
		    $infoObject = New-Object PSObject

            #Create element "device"
            $xmlWriter.WriteStartElement('device')

		    #The following add data to the infoObjects...
			
            #Computer(SID, Name, IP-Adresse, RAM, Computertyp)
            $xmlWriter.WriteElementString('sid',$MachineSID)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "sid" -value $MachineSID
            $xmlWriter.WriteElementString('devicename',$CPUInfo.SystemName)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "devicename" -value $CPUInfo.SystemName
            $xmlWriter.WriteElementString('ram',$RAMInfo)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "ram" -value $RAMInfo
            $xmlWriter.WriteElementString('computertype',$ComputerInfo.Model)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "computertype" -value $ComputerInfo.Model		

            #Betriebssystem
            $xmlWriter.WriteStartElement('operatingsystem')
            $xmlWriter.WriteElementString('osname',$OSInfo.Caption)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "osname" -value $OSInfo.Caption
            $xmlWriter.WriteElementString('ostype',$OSInfo.OSArchitecture)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "ostype" -value $OSInfo.OSArchitecture
            $xmlWriter.WriteEndElement()
            
            #Prozessor(Prozessorfamilie, Anzahl Kerne, Taktrate)
            $xmlWriter.WriteStartElement('processor')
            $xmlWriter.WriteElementString('processorname',$CPUInfo.Name)
		    Add-Member -inputObject $infoObject -memberType NoteProperty -name "processorname" -value $CPUInfo.Name
            $xmlWriter.WriteElementString('taktrate',($CPUInfo.MaxClockSpeed/1000))
		    Add-Member -inputObject $infoObject -memberType NoteProperty -name "taktrate" -value ($CPUInfo.MaxClockSpeed/1000)
            $xmlWriter.WriteElementString('cores',$CPUInfo.NumberOfCores)
		    Add-Member -inputObject $infoObject -memberType NoteProperty -name "cores" -value $CPUInfo.NumberOfCores
    		$xmlWriter.WriteEndElement()

            #Drucker(Liste registrierter Drucker, Treiber, Version)
            ForEach ($PDriver in (Get-WmiObject Win32_PrinterDriver -computerName $client))
            {
				$xmlWriter.WriteStartElement('printer')			
                $Drive = $PDriver.DriverPath.Substring(0,1)
                $temp = $PDriver.Name
                $temp = $temp.Substring(0,$temp.Length-17)
                $xmlWriter.WriteElementString('printname', $temp)
                $xmlWriter.WriteEndElement()				
            }

            #NIC(MAC, Bezeichnung, Gateway, DHCP, Subnet)
            $xmlWriter.WriteStartElement('network')
            $xmlWriter.WriteElementString('mac',$NetInfo.MACAddress)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "mac" -Value $NetInfo.MACAddress
            $xmlWriter.WriteElementString('description',$NetInfo.Description)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "description" -Value $NetInfo.Description
            $xmlWriter.WriteElementString('gateway',$NetInfo.DefaultIPGateway)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "gateway" -Value $NetInfo.DefaultIPGateway
            $xmlWriter.WriteElementString('dhcp',$NetInfo.DHCPEnabled)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "dhcp" -Value $NetInfo.DHCPEnabled
            $xmlWriter.WriteElementString('subnet',$NetInfo.IPSubnet)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "subnet" -Value $NetInfo.IPSubnet
            $xmlWriter.WriteElementString('ip',$NetInfo.IpAddress)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "ip" -Value $NetInfo.IpAddress		
            $xmlWriter.WriteEndElement()
        
            #Software(Programme, Version)            
            #Open each Subkey and use the GetValue Method to return the string value for DisplayName for each
            foreach($key in $subkeys){
                $thiskey=$UninstallKey+"\\"+$key
                $thisSubKey=$reg.OpenSubKey($thiskey)
                $DisplayName=$thisSubKey.GetValue("DisplayName")
                $DisplayVersion=$thisSubKey.GetValue("DisplayVersion")
                if ($DisplayName) {
                $xmlWriter.WriteStartElement('software')
                $xmlWriter.WriteElementString('swname',$DisplayName)
                $xmlWriter.WriteElementString('swversion',$DisplayVersion)
                $xmlWriter.WriteEndElement()
                }
            }
            
            #Close xml device
            $xmlWriter.WriteEndElement()

            $infoObject #Output to the screen for a visual feedback
		    $infoColl += $infoObject #Add infoObject to collection
        }
        
    }

    #This is for test-connection if statement: false
    Else
    {   
        #Write-Output "Host $client is not reachable or not WMI enabled" | Out-File $LogFile -Append
    }
}

#XML close the "device" node:
$xmlWriter.WriteEndElement()
 
#XML finalize the document:
$xmlWriter.WriteEndDocument()
$xmlWriter.Flush()
$xmlWriter.Close()