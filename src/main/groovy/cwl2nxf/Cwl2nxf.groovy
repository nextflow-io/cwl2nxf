#!/usr/bin/env groovy
package cwl2nxf

class Cwl2nxf {
    static void main(String[] args) {
        String inpath = args[0]
        String ymlpath = args[1]
        String workingDir = (inpath.replace(inpath.split('/')[-1],''))
        String infile = new File(inpath).text
        String ymlfile = new File(ymlpath).text


        Workflow wf = new Workflow(infile, ymlfile, workingDir)


        String fileName = new Date().getTime() + '.nf'
        def outfile = new File(workingDir,fileName)
        wf.getChannels().each {
            //println(it)
            outfile.append(it + '\n')
        }
        wf.getSteplist().each {
            //println(it.getProcessBlock())
            outfile.append(it.getProcessBlock() + '\n')
        }
        if(wf.getDocker() != null){
            def configFile = new File(workingDir,'nextflow.config')
            configFile.write("process.container = '${wf.getDocker()}'")
            configFile.write("docker.enabled = true")

        }

    }
}