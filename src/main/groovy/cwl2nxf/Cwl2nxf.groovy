#!/usr/bin/env groovy
package cwl2nxf

class Cwl2nxf {

    Cwl2nxf() {

    }

    void run(String... args) {
        if( !args ) {
            println "Missing input CWL workflow files"
            println "usage: cwl2nxf <workflow.cwl> <input.yml>"
            System.exit 1
        }

        String inpath = args[0]
        String ymlpath = args[1]
        String workingDir = (inpath.replace(inpath.split('/')[-1],''))
        String infile = new File(inpath).text
        String ymlfile = new File(ymlpath).text


        Workflow wf = new Workflow(infile, ymlfile, workingDir)


        String fileName = new Date().getTime() + '.nf'
        def outfile = new File(workingDir,fileName)
        wf.getChannels().each {
            outfile.append(it + '\n')
        }
        wf.getSteplist().each {
            println(it.getProcessBlock())
            outfile.append(it.getProcessBlock() + '\n')
        }
        if(wf.getDocker() != null){
            def configFile = new File(workingDir,'nextflow.config')
            configFile.append("process.container = '${wf.getDocker()}'\n")
            configFile.append("docker.enabled = true\n")

        }

    }

    static void main(String[] args) {
       new Cwl2nxf().run(args)
    }
}