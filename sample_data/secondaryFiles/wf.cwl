cwlVersion: v1.0
class: Workflow
inputs:
  bam: File

requirements:
- class: MultipleInputFeatureRequirement

outputs:
  indexed:
    type: File
    outputSource: [bamindex/indexout]

steps:
  bamindex:
    run: bamindex.cwl
    in:
      bamfile: bam
    out: [indexout]
