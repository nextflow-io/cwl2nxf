cwlVersion: v1.0
class: CommandLineTool


hints:
  DockerRequirement:
    dockerPull: combinelab/salmon

baseCommand: salmon
arguments: [quant]
inputs:
  index:
    type: Directory
    inputBinding:
      prefix: --index
  inf1:
    type: File
    inputBinding:
      prefix: --mates1
  inf2:
    type: File
    inputBinding:
      prefix: --mates2
  quantdir:
    type: string
    inputBinding:
      prefix: --output
  numBootstraps:
    type: int
    default: 30
    inputBinding:
      prefix: --numBootstraps
  libType:
    type: string
    default: A
    inputBinding:
      prefix: --libType
  runThreadN:
    type: int?
    inputBinding:
      prefix: --runThreadN
    doc: |
      1
      int: number of threads to run Salmon
outputs:
  outquantdir:
    type: Directory
    outputBinding:
      glob: .
