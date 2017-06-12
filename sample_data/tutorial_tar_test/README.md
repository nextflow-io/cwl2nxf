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
2. combination of lists of command line arguments in both tar-param.cwl and arguments.cwl
3. Docker conversion as demonstrated in arguments.cwl

Running with cwltool
-----------
```
cwltool wf.cwl wf.yml
```
Converting to Nextflow
-----------
The output file name of 'tar.nf' is specified on the commandline
```
./launch.sh -o tar.nf sample_data/tutorial_tar_test/wf.cwl sample_data/tutorial_tar_test/wf.yml
```
Resulting Nextflow
-----------
```
NULL_FILE = java.nio.file.Paths.get('.NULL')
Channel.from(NULL_FILE).set { data_ch }
inp = file('hello.tar')
ex = "Hello.java"

process tar_param{ 
	input: 
	file invar_0 from inp
	val invar_1 from ex
	output: 
	file ""Hello.java"" into example_out
	"""
	tar xf ${invar_0} ${invar_1}
	"""
}

process arguments{ 
	publishDir './', mode:'copy' 
	input: 
	file invar_0 from example_out
	output: 
	file "*.class" into classfile
	"""
	javac -d ./ ${invar_0}
	"""
}

```

Running in Nextflow
-----------
```
nextflow run tar.nf
```