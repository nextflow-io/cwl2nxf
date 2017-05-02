cwlVersion: v1.0
class: CommandLineTool

hints:
  DockerRequirement:
    dockerPull: combinelab/salmon

    
baseCommand: salmon
arguments: [index]
inputs:
  index:
    type: string
    inputBinding:
      prefix: --index
  transcripts:
    type: File
    inputBinding:
      prefix: --transcripts
  runThreadN:
    type: int?
    inputBinding:
      prefix: --runThreadN
    doc: |
      1
      int: number of threads to run Salmon
outputs:
  outindex:
    type: Directory
    outputBinding:
       glob: "*"