# Report.ixi

## About

IXI plugin for [Ict](https://github.com/iotaledger/ict) to report node information to external services. 


## Installation

Go to [releases](https://github.com/trifel/Report.ixi/releases) and download `report.ixi-{VERSION}.jar**` from the most recent release. 

Alternatively, you can build the .jar file from the source code (advanced users). **Git** and **Gradle** is required for the following steps:
```shell
git clone https://github.com/trifel/Report.ixi
cd Report.ixi
gradle fatJar
```

## Configure

### Adjust the Ict config file

In your **ict.cfg** file, add `Report.ixi` to `ixis` and make sure that `ixi_enabled` is set to `true`:

```
ixis=Report.ixi
ixi_enabled = true
```

### Create and adjust the Report.ixi config file

Create or open the **report.ixi.cfg** file and complete the settings.

```
// The host IP address is used to create the UDP socket. 
// If the IP address is 0.0.0.0, the socket will be bound to the wildcard address, 
// an IP address chosen by the kernel. Mostly you don`t need to change the host IP.
host=0.0.0.0

// The additional port of the Report.ixi application of your instance. 
// This additional port is not the Ict port.
reportPort=1338

// A simple name chosen by the Ict operator for this Ict node.
// There is no guarantee that this name is unique, multiple Ict nodes may
// use the same name. Follow the naming convention: "<name> (ict-<number>)", e.g. 
// "ixuz (ict-0)" or "testIT (ict-0)".
name=

// Insert the IP or domain of your neighbor A.
neighborAHost=
// Insert the additional port of the Report.ixi application of your neighbor A (report port). 
// If your neighbor didn't install Report.ixi, ignore this setting.
neighborAPort=1338

// Insert the IP or domain of your neighbor B.
neighborBHost=
// Insert the additional port of the Report.ixi application of your neighbor B (report port). 
// If your neighbor didn't install Report.ixi, ignore this setting.
neighborBPort=1338

// Insert the IP or domain of your neighbor C.
neighborCHost=
// Insert the additional port of the Report.ixi application of your neighbor C (report port). 
// If your neighbor didn't install Report.ixi, ignore this setting.
neighborCPort=1338

// A unique identifier that has been generated upon initial start of
// the Report.ixi application. Don't change the uuid by yourself.
uuid=61c134d5-915e-457b-999e-e91fd2aa8fe3
```

## Run Report.ixi

### Step 1: Stop your Ict Client

### Step 2: Start Report.ixi

```shell
java -jar report.ixi-{VERSION}.jar
```
If your **report.ixi.cfg** file is not in the same directory, you can set the path as argument:
```shell
java -jar report.ixi-{VERSION}.jar ../report.ixi.cfg
```

### Step 3: Start your Ict Client

```shell
java -jar ict-{VERSION}.jar
```

The Report.ixi application should print this line if the Ict is connected successfully:

```
Ict '{name}' connected
```
