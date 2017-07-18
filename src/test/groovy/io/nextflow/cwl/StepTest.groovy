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

package io.nextflow.cwl

import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 *
 */
class StepTest extends Specification {

    def 'should extract the command string' () {

        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build

        inputs:
          indexfile:
            type: File
            inputBinding:
              position: 1
          doing:
            type: string
            inputBinding:
              position: 2
        '''
        .stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()
        Map stepinsTest = ['indexfile':'','doing':'']

        when:
        def cmd = step.extractCommandString(cwl, stepinsTest)
        then:
        cmd == 'bowtie2-build ${invar_0} ${invar_1}'

    }
    def 'check that extractCommandString handles defaults correctly' () {

        given:
        def text = '''
        baseCommand: bowtie2-build

        inputs:
          indexfile:
            type: File
            inputBinding:
              position: 1
          doing:
            type: string
            default: "default value"
            inputBinding:
              position: 2


        '''
                .stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()
        Map stepinsTest = ['indexfile':'']

        when:
        def cmd = step.extractCommandString(cwl, stepinsTest)
        then:
        cmd == 'bowtie2-build ${invar_0} default value'

    }
    def 'should extract arguments' (){
        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build
        arguments: ["-d", $(runtime.outdir)]
        
        inputs:
          indexfile:
            type: File
            inputBinding:
              position: 1
          doing:
            type: string
            inputBinding:
              position: 2
        '''.stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()

        when:
        def argsreturn = step.extractArguments(cwl)
        then:
        argsreturn == ' -d ./'
    }
    def 'check type returns' (){
        given:
        def step = new Step()

        when:
        def typeTest = step.cwlTypeConversion('File')
        then:
        typeTest == 'file'
        when:
        def typeTest2 = step.cwlTypeConversion('int')
        then:
        typeTest2 == 'val'
    }
    def 'check type return error handling' (){
        given:
        def step = new Step()

        when:
        def typeTest = step.cwlTypeConversion('Notvalid')
        then:
        thrown IllegalArgumentException

    }
    def 'secondary file formatting' (){
        given:
        def step = new Step()
        def secondaryFiles = ['../atestpath1','../atestpath2']

        when:
        def formatTest = step.formatSecondaryFiles(secondaryFiles)
        then:
        formatTest == ["file secondary_0 from file(../atestpath1)", "file secondary_1 from file(../atestpath2)"]
    }
    def 'check prefix parsing' (){
        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build
        
        inputs:
          gtffile:
            type: File
            inputBinding:
              position: 1
              prefix: --GTF
        '''.stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()

        when:
        def stepins = ['gtffile':'gtf']
        def cmdreturn = step.extractCommandString(cwl,stepins)
        then:
        cmdreturn == 'bowtie2-build --GTF ${invar_0}'


    }
    def 'check prefix parsing with = in' (){
        given:
        def text = '''
        cwlVersion: v1.0
        class: CommandLineTool
        baseCommand: bowtie2-build
        
        inputs:
          gtffile:
            type: File
            inputBinding:
              position: 1
              prefix: test=
        '''.stripIndent()

        def cwl = (Map)new Yaml().load(text)
        def step = new Step()

        when:
        def stepins = ['gtffile':'gtf']
        def cmdreturn = step.extractCommandString(cwl,stepins)
        then:
        cmdreturn == 'bowtie2-build test=${invar_0}'
    }

    def 'check getProcessBlock' (){

        given:
        def wfoutputs = ["[classfile]"]
        String id = "tar_param"
        def outputs = ["file \"Hello.java\" into example_out"]
        def inputs = ["file invar_0 from inp", "val invar_1 from ex"]
        def cmdstr = "tar xf \${invar_0} \${invar_1}"

        def correctResult = '''
        process tar_param{ 
        \tinput: 
        \tfile invar_0 from inp
        \tval invar_1 from ex
        \toutput: 
        \tfile "Hello.java" into example_out
        \t"""
        \ttar xf ${invar_0} ${invar_1}
        \t"""
        }\n'''.stripIndent()

        when:
        def step = new Step(null, wfoutputs, id, inputs, outputs, cmdstr)
        def testResult = step.getProcessBlock().stripMargin()
        then:
        correctResult == testResult


    }



    def 'check JS argument evaluation' (){

        given:
        def arg = [arguments:['-d', '\$(runtime.outdir)']]

        when:
        def step = new Step()
        def testResult = step.extractArguments(arg)
        then:
        testResult == " -d ./"


    }

    def 'test hints error for non ResourceRequirement' (){
        given:
        def stepdata = [hints:[[class:'NotResourceRequirement', coresMin:2, outdirMin:1024, ramMin:4096]]]

        def step = new Step()

        when:
        def tst = step.extractHints(stepdata)
        then:
        thrown IllegalArgumentException
    }

    def 'test hints processing' () {
        given:
        def stepdata = [hints:[[class:'ResourceRequirement', coresMin:2, outdirMin:1024, ramMin:4096]]]
        def stepdata2 = [hints:[[class:'ResourceRequirement', coresMax:4, outdirMin:1024, ramMin:4096]]]
        def stepdata3 = [hints:[[class:'ResourceRequirement', coresMax:4, coresMin:2, outdirMin:1024, ramMin:4096]]]

        def step = new Step()

        when:
        def tst = step.extractHints(stepdata)
        def tst2 = step.extractHints(stepdata2)
        def tst3 = step.extractHints(stepdata3)
        then:
        tst == ["disk '1024 MB'", "memory '1024 MB'", "cpus 2"]
        tst2 == ["disk '1024 MB'", "memory '1024 MB'", "cpus 4"]
        tst3 == ["disk '1024 MB'", "memory '1024 MB'", "cpus 4"]
    }
    def 'test hints for non class version' (){
        given:
        def stepdata = [hints:[DockerRequirement:[dockerPull:'combinelab/salmon']]]
        def step = new Step()

        when:
        def testresult = step.extractHints(stepdata)

        then:
        testresult == ['container \'combinelab/salmon\'']


    }

}
