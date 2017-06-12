tutorial_tar_test
========
This test case demonstrates the conversion of the Java untar and compile workflow from the CWL user guide. Everything necessary to run either the CWL or converted Nextfow is included. 

inputs in YAML 
-----------
The wf.yml file contains the inputs for the workflow. 
1. a tar file with Java source code defined in the YAML 'inp' field
2. the Java file to be compiled is defined in the YAML 'ex' field

Notable conversions
-----------
1. basic Javascript evaluation of $(runtime.outdir) in arguments.cwl
2. combindation of lists of command line arguments in both tar-param.cwl and arguments.cwl
3. Docker conversion as demonstrated in arguments.cwl