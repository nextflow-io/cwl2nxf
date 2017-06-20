cwlVersion: v1.0
class: CommandLineTool
baseCommand: cat
inputs:
  first:
    type: File
    inputBinding:
      position: 1

outputs:
  outfile:
    type: File
    outputBinding:
      glob: $(inputs.first)