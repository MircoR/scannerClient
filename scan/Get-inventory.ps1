<#
.SYNOPSIS
Get Client Information
.DESCRIPTION
Beschreibung...
.NOTES  
Bemerkungen...
#>

param(
    [string] $cidr
)

#Define log file and time stamp
$LogTime = Get-Date -Format "MM-dd-yyyy-hh-mm-ss"
$LogFile = '.\'+"LOG_"+$LogTime+".log"

#Get the client list
#$clients = Get-Content .\Clients.txt
$scannet = .\scan\PSipcalc.ps1 -NetworkAddress $cidr -Enumerate
$clients = $scannet.IPEnumerated




#XML this is where the document will be saved
$Path = ".\scan\Inventory"+$LogTime+".xml"
 
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

#Create root element "clients" and add some attributes to it
$XmlWriter.WriteComment('List of clients')
$xmlWriter.WriteStartElement('clients')




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
        Write-Output "Host $client is reachable and WMI enabled" | Out-File $LogFile -Append

        #Define and fill variables
	    $OSInfo = Get-WmiObject Win32_OperatingSystem -ComputerName $client #Get OS Information
        #$SWInfo = Get-WmiObject -Class Win32_Product -ComputerName $client #Get SW Information
        $CPUInfo = Get-WmiObject Win32_Processor -ComputerName $client #Get CPU Information
	    $RAMInfo = Get-WmiObject CIM_PhysicalMemory -ComputerName $client | Measure-Object -Property capacity -Sum | % { [Math]::Round(($_.sum / 1GB), 2) } #Get RAM Information rounded
	    $NetInfo = Get-WmiObject Win32_NetworkAdapterConfiguration -ComputerName $client -EA Stop | ? {$_.IPEnabled} #Get Network Adapter Information
        $ComputerInfo = Get-WmiObject Win32_Computersystem -ComputerName $client #Get Computer Information
        $LocalAccountSID = Get-WmiObject -Query "SELECT SID FROM Win32_UserAccount WHERE LocalAccount = 'True'" | Select-Object -First 1 -ExpandProperty SID #Get the local SID by finding a local account
        $MachineSID = ($temp = $LocalAccountSID -split "-")[0..($temp.Length-2)]-join"-" #Remove Relative ID (RID) from local Account SID = Machine SID
        
        Foreach ($CPU in $CPUInfo)
	    {
            #Create new object from class PSObject
		    $infoObject = New-Object PSObject

            #Create root element "client"
            $xmlWriter.WriteStartElement('client')

		    #The following add data to the infoObjects...

            #Betriebssystem
            $xmlWriter.WriteStartElement('Betriebssystem')
            $xmlWriter.WriteElementString('Betriebssystem',$OSInfo.Caption)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "Betriebssystem" -value $OSInfo.Caption
            $xmlWriter.WriteElementString('Betriebssystemtyp',$OSInfo.OSArchitecture)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "Betriebssystemtyp" -value $OSInfo.OSArchitecture
            $xmlWriter.WriteEndElement()

            #Programme
            $xmlWriter.WriteStartElement('Programme')
            $xmlWriter.WriteElementString('Programme',$SWInfo.Name)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "Programme" -value $SWInfo.Name
            $xmlWriter.WriteElementString('Version',$SWInfo.Version)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "Version" -value $SWInfo.Version
            $xmlWriter.WriteEndElement()
        
            #Prozessor(Prozessorfamilie, Anzahl Kerne, Taktrate)
            $xmlWriter.WriteStartElement('Prozessor')
            $xmlWriter.WriteElementString('Prozessor',$CPUInfo.Name)
		    Add-Member -inputObject $infoObject -memberType NoteProperty -name "Prozessor" -value $CPUInfo.Name
            $xmlWriter.WriteElementString('Taktfrequenz_GHz',($CPUInfo.MaxClockSpeed/1000))
		    Add-Member -inputObject $infoObject -memberType NoteProperty -name "Taktfrequenz GHz" -value ($CPUInfo.MaxClockSpeed/1000)
            $xmlWriter.WriteElementString('Anzahl_Kerne',$CPUInfo.NumberOfCores)
		    Add-Member -inputObject $infoObject -memberType NoteProperty -name "Anzahl Kerne" -value $CPUInfo.NumberOfCores
    		$xmlWriter.WriteEndElement()

            #Drucker(Liste registrierter Drucker, Treiber, Version)

            #Computer(SID, Name, IP-Adresse, RAM, Computertyp)
            $xmlWriter.WriteStartElement('Computer')
            $xmlWriter.WriteElementString('SID',$MachineSID)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "SID" -value $MachineSID
            $xmlWriter.WriteElementString('Name',$CPUInfo.SystemName)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "Name" -value $CPUInfo.SystemName
            $xmlWriter.WriteElementString('IP-Adresse',$NetInfo.IpAddress)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "IP-Adresse" -Value $NetInfo.IpAddress
            $xmlWriter.WriteElementString('RAM_GB',$RAMInfo)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "RAM GB" -value $RAMInfo
            $xmlWriter.WriteElementString('Computertyp',$ComputerInfo.Model)
            Add-Member -inputObject $infoObject -memberType NoteProperty -name "Computertyp" -value $ComputerInfo.Model
            $xmlWriter.WriteEndElement()

            #NIC(IPV4/6, MAC, Bezeichnung, Gateway, DHCP, Subnet)
            #IPV4/6
            $xmlWriter.WriteStartElement('NIC')
            $xmlWriter.WriteElementString('MAC',$NetInfo.MACAddress)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "MAC" -Value $NetInfo.MACAddress
            $xmlWriter.WriteElementString('Bezeichnung',$NetInfo.Description)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "Bezeichnung" -Value $NetInfo.Description
            $xmlWriter.WriteElementString('Gateway',$NetInfo.DefaultIPGateway[0])
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "Gateway" -Value $NetInfo.DefaultIPGateway[0]
            $xmlWriter.WriteElementString('DHCP',$NetInfo.DHCPEnabled)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "DHCP" -Value $NetInfo.DHCPEnabled
            $xmlWriter.WriteElementString('Subnet',$NetInfo.IPSubnet)
            Add-Member -inputObject $infoObject -MemberType NoteProperty -Name "Subnet" -Value $NetInfo.IPSubnet
            $xmlWriter.WriteEndElement()
        
            $infoObject #Output to the screen for a visual feedback
		    $infoColl += $infoObject #Add infoObject to collection

            #Close the "client" node
            $xmlWriter.WriteEndElement()
        }
    }

    #This is for test-connection if statement: false
    Else
    {   
        Write-Output "Host $client is not reachable or not WMI enabled" | Out-File $LogFile -Append
    }
}

#XML close the "clients" node:
$xmlWriter.WriteEndElement()
 
#XML finalize the document:
$xmlWriter.WriteEndDocument()
$xmlWriter.Flush()
$xmlWriter.Close()

#For development output to console
$infoColl > screen_output.txt