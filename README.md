# Report.ixi

## About

IXI plugin for [Ict](https://github.com/iotaledger/ict) to report node information to external services. 


## Installation

`Ict WebGUI > Manage Modules > Install Third Party Plugin > "trifel/Report.ixi"`

Go to [releases](https://github.com/trifel/Report.ixi/releases) and download `report.ixi-{VERSION}.jar` from the most recent release. 

Alternatively, you can build the .jar file from the source code (advanced users). **Git** and **Gradle** is required for the following steps:
```shell
git clone https://github.com/trifel/Report.ixi
cd Report.ixi
gradle fatJar
```
And move the .jar file into Ict's `modules/`-directory.

## Configure

`Ict WebGUI > IXI Modules > Report.ixi`

 * `Ict REST API Port`: Set the Ict WebGUI port number.
 * `Ict REST API Password`: Set the password needed to access Ict WebGUI.
 * `Name`: Set the name that your node will be identifying itself as, the accepted format is `name (ict-0)`.
   * Replace the `name` with your own personalized name.
   * If you run more than one Ict, increment the integer at the end of the name. 
 * `Neighbors`: You will most likely not need to edit this field as it will automatically get populated when successfully configuring `Ict REST API Port` and `Ict REST API Password`
   * `publicAddress`: It's possible to override the public address associated with a neighbor, but this is uncommon.
 * `Public Address`: Enter your own node's publically known `address:port`