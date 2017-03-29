cwlVersion: v1.0
class: Workflow
inputs:
  infile: string

outputs: []


steps:
  index:
    run: hello.cwl
    in:
      message: infile
    out: []
