This is an example Common Workflow Language (CWL) pipeline for running a super
fast RNA-seq experiment using Salmon to do quasialignments and quantitation.

1) install cwltool
```bash
conda install -c bioconda cwltool
```

2) extract test data
```bash
tar zxvf sample_data.tgz
```

3) run workflow
```bash
cwltool salmon-workflow.cwl workflow-test.yaml
```
