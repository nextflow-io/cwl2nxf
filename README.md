cwl2nxf
========

Early development work on a CWL to Nextflow converter. This is still a very early prototype, and is most likely only functional for a small number of cases. 

The converter can be built using the following Gradle command. 
```
gradle uberjar
```

Then the below command will execute the newly created jar and process the provided sample data.
```
java -jar build/libs/cwl2nxf-0.1.jar sample_data/wf.cwl sample_data/wf.yml
```
Upon execution a *.nf file will be created in the base folder. The file is currently just named with a timestamp. This file should be then runnable in nextflow. 