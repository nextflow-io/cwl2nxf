cwlVersion: v1.0
class: CommandLineTool
baseCommand: tophat2

requirements:
  - class: InlineJavascriptRequirement
  - class: InitialWorkDirRequirement
    listing:
      - $(inputs.indexs[0])
      - $(inputs.indexs[1])
      - $(inputs.indexs[2])
      - $(inputs.indexs[3])
      - $(inputs.indexs[4])
      - $(inputs.indexs[5])
inputs:
  indexs:
    type: File[]
  gtffile:
    type: File
    inputBinding:
      position: 1
      prefix: --GTF
  doing:
    type: string
    inputBinding:
      position: 2
  pair1:
    type: File
    inputBinding:
      position: 3
  pair2:
    type: File
    inputBinding:
      position: 4
outputs: 
  tophatout:
    type: File
    outputBinding:
      glob: "./tophat_out/accepted_hits.bam"

