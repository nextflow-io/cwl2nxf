cwlVersion: v1.0
class: Workflow

hints:
  DockerRequirement:
    dockerPull: nextflow/rnatoy
    
inputs:
  infile: File
  act: string
  gtf: File
  pairone: File
  pairtwo: File

outputs:
  cuffs:
    type: File
    outputSource: cuff/cuffout

steps:
  index:
    run: bowtiemod.cwl
    in:
      indexfile: infile
      doing: act
    out: [indexout]
  tophat:
    run: tophatmod.cwl
    in:
      indexs: index/indexout
      gtffile: gtf
      doing: act
      pair1: pairone
      pair2: pairtwo
    out: [tophatout]
  cuff:
    run: cufflinksmod.cwl
    in:
      bamfile: tophat/tophatout
      gtf: gtf
    out: [cuffout]