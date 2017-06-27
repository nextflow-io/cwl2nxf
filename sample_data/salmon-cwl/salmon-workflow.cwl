cwlVersion: v1.0
class: Workflow

hints:
  - class:DockerRequirement:
    dockerPull: combinelab/salmon

inputs:
  index: string
  transcripts: File
  inf1: File[]
  inf2: File[]
  samplename: string[]
requirements:
  - class: ScatterFeatureRequirement

outputs:
  outindex:
    type: Directory
    outputSource: quasiindex/outindex
  outquantdir:
    type: Directory[]
    outputSource: quant/outquantdir

steps:
  quasiindex:
    run: salmon-index.cwl
    in:
      index: index
      transcripts: transcripts
    out: [outindex]
  quant:
    run: salmon-quant.cwl
    in:
      index: quasiindex/outindex
      inf1: inf1
      inf2: inf2
      quantdir: samplename
    out: [outquantdir]
    scatter:
      - inf1
      - inf2
      - quantdir
    scatterMethod: dotproduct
