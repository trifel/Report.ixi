# Report.ixi

## About

IXI plugin for [Ict](https://github.com/iotaledger/ict) to report node information to external services. 


## Installation

Go to [releases](https://github.com/trifel/Report.ixi/releases) and download `report.ixi-{VERSION}.jar` from the most recent release. 

Alternatively, you can build the .jar file from the source code (advanced users). **Git** and **Gradle** is required for the following steps:
```shell
git clone https://github.com/trifel/Report.ixi
cd Report.ixi
gradle fatJar
```

## Configure

### Adjust the Ict config file

In your **ict.cfg** file, make sure that `ixi_enabled` is set to `true`:

```
ixi_enabled=true
```

### Create and adjust the Report.ixi config file

Create or open the **report.ixi.cfg** file and complete the settings.

```
// The host IP address is used to create the UDP socket. 
// If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, 
// an IP address chosen by the kernel. Mostly you don`t need to change the host IP.
host=0.0.0.0

// The name of your Ict Client which is configured in the ict.cfg file. This name
// is only used for the RMI connection between Report.ixi and the Ict Client.
// The default value is set to "ict".
ictName=ict

// The additional port of the Report.ixi application of your instance. 
// This additional port is not the Ict port.
reportPort=1338

// A simple name chosen by the Ict operator for this Ict node.
// There is no guarantee that this name is unique, multiple Ict nodes may
// use the same name. Follow the naming convention: "<name> (ict-<number>)", e.g. 
// "ixuz (ict-0)" or "testIT (ict-0)".
name=

// Add host and port of your neighbors Report.ixi (comma separated, no whitespace).
// Please note that the port numbers should refer to your neighbor's Report.ixi operational port.
neighbors=10.10.10.10:1338,20.20.20.20:1338,public.address.to.neighbor:1338

// A unique identifier that has been generated upon initial start of
// the Report.ixi application. Don't change the uuid by yourself.
uuid=61c134d5-915e-457b-999e-e91fd2aa8fe3
```

### Port forwarding, firewall settings

The Report.ixi application will only operate correctly, if you open or forward the additional UDP port (`reportPort`, which is defined in the file `report.ixi.cfg`) in your router and/or firewall settings.

## Run Report.ixi

### Step 1: Start your Ict Client

```shell
java -jar ict-{VERSION}.jar
```

### Step 2: Start Report.ixi

```shell
java -jar report.ixi-{VERSION}.jar
```
If your **report.ixi.cfg** file is not in the same directory, you can set the path as argument:
```shell
java -jar report.ixi-{VERSION}.jar ../report.ixi.cfg
```
