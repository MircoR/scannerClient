<#
.SYNOPSIS
Get Client Information
.DESCRIPTION
Beschreibung...
.NOTES  
Bemerkungen...
.Version
0.95
#>

param(
    [string]$networkId,
    [string]$companyId
)


#Define log file and time stamp
$LogTime = Get-Date -Format "MM-dd-yyyy-hh-mm-ss"
$LogFile = '.\scan\' + $companyId + "_" + $LogTime + ".log"

#Get the client list
#$clients = Get-Content .\Clients.txt
$scannet = .\scan\PSipcalc.ps1 -NetworkAddress $networkId -Enumerate
$clients = $scannet.IPEnumerated

#XML this is where the document will be saved
#$Path = ".\scan\Inventory"+$LogTime+".xml"
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

#Run the commands for each client in the list
$infoColl = @()

Foreach ($client in $clients)
{
    #Variable to check if it's a WMI client or not
    $wmi = gwmi win32_bios -ComputerName $client -ErrorAction SilentlyContinue

    #Check if client is reachable (ping) and WMI = true
    If ((Test-Connection $client -count 1 -quiet) -and ($wmi))
    {
        
        #Write client online into logfile
        #Write-Output "Host $client is reachable and WMI enabled" | Out-File $LogFile -Append

        #Define and fill variables
	    $OSInfo = Get-WmiObject Win32_OperatingSystem -ComputerName $client #Get OS Information
        
        #Software...
        #$SWInfo = Get-WmiObject Win32_Product -ComputerName $client #Get SW Information
        #Define the variable to hold the location of Currently Installed Programs
        $UninstallKey=”SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall”
  
        #Create an instance of the Registry Object and open the HKLM base key
        $reg=[microsoft.win32.registrykey]::OpenRemoteBaseKey(‘LocalMachine’,$client)  
        #Drill down into the Uninstall key using the OpenSubKey Method
        $regkey=$reg.OpenSubKey($UninstallKey)  
        #Retrieve an array of string that contain all the subkey names
        $subkeys=$regkey.GetSubKeyNames()
  
        #Rest...
        $CPUInfo = Get-WmiObject Win32_Processor -ComputerName $client #Get CPU Information
	    $RAMInfo = Get-WmiObject CIM_PhysicalMemory -ComputerName $client | Measure-Object -Property capacity -Sum | % { [Math]::Round(($_.sum / 1GB), 2) } #Get RAM Information rounded
	    $NetInfo = Get-WmiObject Win32_NetworkAdapterConfiguration -ComputerName $client -EA Stop | ? {$_.IPEnabled} #Get Network Adapter Information
        $ComputerInfo = Get-WmiObject Win32_Computersystem -ComputerName $client #Get Computer Information
        $LocalAccountSID = Get-WmiObject -Query "SELECT SID FROM Win32_UserAccount WHERE LocalAccount = 'True'" | Select-Object -First 1 -ExpandProperty SID #Get the local SID by finding a local account
        $PrinterInfo = Get-WmiObject -Class Win32_PrinterDriver -ComputerName $client #Get Printer Information
        $MachineSID = ($temp = $LocalAccountSID -split "-")[0..($temp.Length-2)]-join"-" #Remove Relative ID (RID) from local Account SID = Machine SID
		
        Foreach ($CPU in $CPUInfo)
	    {
            #Create new object from class PSObject
		    $infoObject = New-Object PSObject

            #Create root element "device"
            $xmlWriter.WriteStartElement('device')

		    #The following add data to the infoObjects...
			
            #Computer(SID, Name, IP-Adresse, RAM, Computertyp)
            #$xmlWriter.WriteStartElement('Computer')
            $xmlWriter.WriteElementString('sid',$MachineSID)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "sid" -value $MachineSID
            $xmlWriter.WriteElementString('devicename',$CPUInfo.SystemName)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "devicename" -value $CPUInfo.SystemName
            $xmlWriter.WriteElementString('ram',$RAMInfo)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "ram" -value $RAMInfo
            $xmlWriter.WriteElementString('computertype',$ComputerInfo.Model)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "computertype" -value $ComputerInfo.Model
            #$xmlWriter.WriteEndElement()			

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
            $xmlWriter.WriteStartElement('printer')
            foreach($Printer in $Printers){
                $xmlWriter.WriteElementString('printname',$PrinterInfo.Name)
                $xmlWriter.WriteElementString('printname',$PrinterInfo.Version)
            }
            $xmlWriter.WriteEndElement()

            #NIC(IPV4/6, MAC, Bezeichnung, Gateway, DHCP, Subnet)
            #IPV4/6
            $xmlWriter.WriteStartElement('network')
            $xmlWriter.WriteElementString('mac',$NetInfo.MACAddress[0])
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "mac" -Value $NetInfo.MACAddress[0]
            $xmlWriter.WriteElementString('description',$NetInfo.Description[0])
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "description" -Value $NetInfo.Description[0]
            $xmlWriter.WriteElementString('gateway',$NetInfo.DefaultIPGateway)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "gateway" -Value $NetInfo.DefaultIPGateway
            $xmlWriter.WriteElementString('dhcp',$NetInfo.DHCPEnabled[0])
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "dhcp" -Value $NetInfo.DHCPEnabled[0]
            $xmlWriter.WriteElementString('subnet',$NetInfo.IPSubnet[0])
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "subnet" -Value $NetInfo.IPSubnet[0]
            $xmlWriter.WriteElementString('ip',$NetInfo.IpAddress[0])
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "ip" -Value $NetInfo.IpAddress[0]		
            $xmlWriter.WriteEndElement()
        
            #Programme
            
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

#XML close the "clients" node:
$xmlWriter.WriteEndElement()
 
#XML finalize the document:
$xmlWriter.WriteEndDocument()
$xmlWriter.Flush()
$xmlWriter.Close()

#For development only, output to console
$infoColl > screen_output.txt