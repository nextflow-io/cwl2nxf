cwlVersion: v1.0
class: CommandLineTool
baseCommand: cufflinks

arguments: ["--no-update-check","-q"]
inputs:
  gtf:
    type: File
    inputBinding:
      position: 1
      prefix: -G
  bamfile:
    type: File
    inputBinding:
      position: 2
outputs: 
  cuffout:
    type: File
    outputBinding:
      glob: "transcripts.gtf"
