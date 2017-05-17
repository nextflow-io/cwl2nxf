cwlVersion: v1.0
class: CommandLineTool
baseCommand: bowtie2-build

inputs:
  indexfile:
    type: File
    inputBinding:
      position: 1
  doing:
    type: string
    inputBinding:
      position: 2


outputs: 
  indexout:
    type: File[]
    outputBinding:
      glob: "*"
