cwl2nxf
========

Early development work on a CWL to Nextflow converter. This is still a very early prototype, and is most likely only functional for a small number of cases. 

The converter can be built using the following Gradle command. 
```
./gradlew uberjar
```

Then the below command will execute the newly created jar and process the provided sample data.
```
java -jar build/libs/cwl2nxf-*.jar sample_data/tutorial_tar_test/wf.cwl sample_data/tutorial_tar_test/wf.yml 
```
Upon execution a *.nf file will be created in the base folder. The file is currently just named with a timestamp. This file should be then runnable in nextflow. 


Development 
-----------

Compile and run from sources by using the following command:

```
./gradlew jar 
./launch.sh <wf.cwl> <wf.yml>
````

Debug the execution by using a remote debugger (eg. IntelliJ IDEA) as shown below: 

```
./launch.sh -remote-debug <wf.cwl> <wf.yml>
```


Run the code coverage by using the command: 

```
./gradlew cobertura  
```

The generated report is available at the following path: 

```
open ./build/reports/cobertura/index.html
```

Graphs
-----------
A comparison of native CWL graph to the Nextflow generated graph after conversion. 

CWL graph:
![Alt text](docs/CWL_graph.png "CWL workflow graph")

Nextflow graph after conversion:
![Alt text](docs/Nextflow_graph.png "Converted Nextflow graph")

License
-----------
The cwl2nxf is released under the GNU GPLv3 License.
 
