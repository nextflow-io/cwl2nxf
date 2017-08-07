cwlVersion: v1.0
class: CommandLineTool

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
baseCommand: bowtie2-build