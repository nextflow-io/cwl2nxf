cwlVersion: v1.0
class: CommandLineTool
baseCommand: tophat2

inputs:
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

