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

import spock.lang.Specification

/**
 * Created by ksayers on 27/04/17.
 */
class WorkflowTest extends Specification{

    String cwl = '''
    cwlVersion: v1.0
    class: Workflow
    inputs:
      infile: File
      act: string
      gtf: File
      pairone: File
      pairtwo: File
    
    outputs:
      cuffs:
        type: File
        outputSource: cuff/cuffout
    
    steps:
      index:
        run: bowtiemod.cwl
        in:
          indexfile: infile
          doing: act
        out: [indexout]
      tophat:
        run: tophatmod.cwl
        in:
          index: index/indexout
          gtffile: gtf
          doing: act
          pair1: pairone
          pair2: pairtwo
        out: [tophatout]
      cuff:
        run: cufflinksmod.cwl
        in:
          bamfile: tophat/tophatout
          gtf: gtf
        out: [cuffout]
    '''.stripIndent()

    String yml = '''
    infile:
      class: File
      path: ./data/ggal/ggal_1_48850000_49020000.Ggal71.500bpflank.fa
    act: /home/ksayers/cwlconversion_work/cwl2nxf/sample_data/CWL_RNAtoy/data/ggal/genome.index
    gtf:
      class: File
      path: ./data/ggal/ggal_1_48850000_49020000.bed.gff
    pairone:
      class: File
      path: ./data/ggal/ggal_gut_1.fq
    pairtwo:
      class: File
      path: ./data/ggal/ggal_gut_2.fq
    '''.stripIndent()

    def 'test the parseYaml' (){
        given:
        String text = cwl

        def wf = new Workflow()

        when:
        def yml = wf.parseYml(text).getClass()

        then:
        yml == com.fasterxml.jackson.databind.node.ObjectNode
    }
    def 'test extractChannels' (){
        given:
        def wf = new Workflow(cwl, yml, 'sample_data/CWL_RNAtoy')


        when:
        def channels = wf.getChannels()
        println(channels)
        List<String> correct = ["infile = file('./data/ggal/ggal_1_48850000_49020000.Ggal71.500bpflank.fa')",
                                "pairone = file('./data/ggal/ggal_gut_1.fq')",
                                "pairtwo = file('./data/ggal/ggal_gut_2.fq')"]

        then:
        channels[2] == correct[0]
        channels[5] == correct[1]
        channels[6] == correct[2]
    }
    def 'test buildSteps' (){
        given:
        def wf = new Workflow(cwl, yml, 'sample_data/CWL_RNAtoy')


        when:
        def steps = wf.getSteps()


        then:
        steps['index']['run'].toString() == '"bowtiemod.cwl"'
        steps['tophat']['in']['index'].toString() == '"index/indexout"'
        steps['cuff']['out'].toString() == '["cuffout"]'
    }
    def 'Check inputs are only from workflow' (){
        given:
        def wf = new Workflow(cwl, yml, 'sample_data/CWL_RNAtoy')
        def results = ['NULL_FILE', 'Channel.from(NULL_FILE).set { data_ch }',
                       'infile', 'act', 'gtf', 'pairone', 'pairtwo']


        when:
        def steps = wf.getChannels()
        steps.take(2)




        then:
        steps.each{
            assert(it.split('=')[0].trim() in results)
        }
    }

}
