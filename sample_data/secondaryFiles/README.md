secondaryFiles
========
This is a basic CWL test that demonstrates the conversion of some basic features as described below. 

Conversion
-----------
* ResourceRequirement: If a hint is present cwl2nxf evalutes if the class is ResourceRequirement. Parsing for cpus, RAM, and outdir are supported. These values map to the Nextflow equivalents of cpus, memory, and disk. As there is no min/max for Nextflow specified options are always set as the max. 
* secondaryFiles (output): This also demonstrates how secondaryFiles from outputs are handled. Currently cwl2nxf evalutes the secondary glob (e.g "^.bai") and creates a new output channel based on this. The implementation of bundling the files is still being explored. 