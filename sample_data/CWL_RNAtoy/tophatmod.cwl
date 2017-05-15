cwlVersion: v1.0
class: CommandLineTool
baseCommand: tophat2

requirements:
  - class: InlineJavascriptRequirement
  - class: InitialWorkDirRequirement
    listing:
      - class: File
        path: /home/ksayers/cwlconversion_work/cwl2nxf/sample_data/CWL_RNAtoy/genome.index.1.bt2
      - class: File
        path: /home/ksayers/cwlconversion_work/cwl2nxf/sample_data/CWL_RNAtoy/genome.index.2.bt2
      - class: File
        path: /home/ksayers/cwlconversion_work/cwl2nxf/sample_data/CWL_RNAtoy/genome.index.3.bt2
      - class: File
        path: /home/ksayers/cwlconversion_work/cwl2nxf/sample_data/CWL_RNAtoy/genome.index.4.bt2
      - class: File
        path: /home/ksayers/cwlconversion_work/cwl2nxf/sample_data/CWL_RNAtoy/genome.index.rev.1.bt2
      - class: File
        path: /home/ksayers/cwlconversion_work/cwl2nxf/sample_data/CWL_RNAtoy/genome.index.rev.2.bt2

inputs:
  gtffile:
    type: File
    inputBinding:
      position: 1
      prefix: --GTF]
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

