/*
 * Copyright 2017 Center for Genomic Regulation (CRG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author Kevin Sayers <sayerskt@gmail.com>
 */
package io.nextflow.cwl

import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.PackageScope

class Cwl2nxf {

    @PackageScope
    CliBuilder cli

    @PackageScope
    Path targetFileName

    @PackageScope
    Path targetConfigName

    @PackageScope
    Path cwlFile

    @PackageScope
    Path ymlFile

    @PackageScope
    Path workDir

    Cwl2nxf() {
        cli = new CliBuilder(usage:'cwl2nxf <workflow.cwl> <input.yml>', posix: false)
        cli.o(args:1, argName: 'nf', 'output nextflow script file name')
    }

    protected boolean parse(List<String> args) {
        parse(args as String[])
    }

    protected boolean parse(String... args) {
        def options = cli.parse(args)

        if( !options.arguments() || options.arguments().size()!=2 ) {
            return false
        }

        cwlFile = Paths.get(options.arguments().get(0))
        ymlFile = Paths.get(options.arguments().get(1))
        workDir =  ymlFile.parent ?: Paths.get('.')
        targetFileName = options.o ? Paths.get(options.o) : null
        targetConfigName = targetFileName ? ( targetFileName.parent ? targetFileName.parent.resolve('nextflow.config') : Paths.get('nextflow.config') )  : null
        true
    }

    void run(String... args) {
        if( !parse(args) ) {
            cli.usage()
            System.exit 1
        }

        def wf = new Workflow(cwlFile.getText(), ymlFile.getText(), workDir)
            saveOutput( wf, targetFileName, targetConfigName )


    }

    protected void saveOutput( Workflow wf, Path targetScriptPath, Path targetConfigPath ) {

        PrintWriter script = targetScriptPath ? targetScriptPath.newPrintWriter() : new PrintWriter(new OutputStreamWriter(System.out))
        try {
            wf.getChannels().each {
                script.append(it + '\n')
            }
            wf.getSteplist().each {
                script.append(it.getProcessBlock() + '\n')
            }
        }
        finally {
            script.flush()
        }


        PrintWriter config = targetConfigPath ? targetConfigPath.newPrintWriter() : new PrintWriter(new OutputStreamWriter(System.out))
        try {
            if(wf.getDocker() != null) {
                if( !targetConfigPath )
                    config.append "======= nextflow.config =======\n"
                config.append("process.container = '${wf.getDocker()}'\n")
                config.append("docker.enabled = true\n")
            }
        }
        finally {
            config.flush()
        }
    }

    static void main(String[] args) {
       new Cwl2nxf().run(args)
    }
}