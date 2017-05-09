/*
 * Copyright (c) 2013-2017, Centre for Genomic Regulation (CRG).
 * Copyright (c) 2013-2017, Paolo Di Tommaso and the respective authors.
 *
 *   This file is part of 'Nextflow'.
 *
 *   Nextflow is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Nextflow is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Nextflow.  If not, see <http://www.gnu.org/licenses/>.
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