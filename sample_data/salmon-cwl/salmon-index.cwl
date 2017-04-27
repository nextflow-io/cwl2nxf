cwlVersion: v1.0
class: CommandLineTool
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
  index:
    type: Directory
    outputBinding:
      glob: "*"
