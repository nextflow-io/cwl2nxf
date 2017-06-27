class: CommandLineTool
cwlVersion: v1.0

hints:
- class: ResourceRequirement
  coresMin: 2
  outdirMin: 1024
  ramMin: 4096
- class: DockerRequirement
  dockerPull: java:7-jdk

baseCommand: echo
inputs:
  message:
    type: string
    inputBinding:
      position: 1
outputs: []

