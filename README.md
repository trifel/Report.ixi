# Report.ixi

IXI plugin for ICT to report node information to external services.
It is suggested to restart your ICT application after installing this IXI module.

## Installation steps

### Download the source code and binaries

Navigate to your ICT installation's `ixi`-folder and clone the Report.ixi git repository.

```
$ cd ixi
$ git clone https://github.com/trifel/Report.ixi.git
```

### Customize report.properties

Copy and configure `report.properties.template` as `report.properties`.

```
reportServerHost = {IP of remote report collector service}
reportServerPort = {Port of remote report collector service}
nodeName = {Choose a nickname for this ICT node}
nodeExternalPort = {The external port this ICT operates on}
```
_The `report.properties` is ignored by VCS and will not be overwritten upon ICT update._
