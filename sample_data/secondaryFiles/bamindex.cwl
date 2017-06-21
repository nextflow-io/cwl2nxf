cwlVersion: v1.0
class: CommandLineTool
baseCommand: samtools
arguments: [index]

requirements:
  - class: InitialWorkDirRequirement
    listing:
      - $(inputs.bamfile)

inputs:
  bamfile:
    type: File
    inputBinding:
      position: 1
  outbam: 
    type: string
    default: test.bai
    inputBinding:
      position: 2


outputs: 
  indexout:
    type: File[]
    outputBinding:
      glob: "*.bam"
    secondaryFiles:
      - "^.bai"
