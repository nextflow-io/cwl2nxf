cwlVersion: v1.0
class: Workflow

inputs:
  firstfile: File


outputs: 
  testout:
    type: File
    outputSource: test/outfile


steps:
  test:
    run: test.cwl
    in:
      first: firstfile
    out: [outfile]
