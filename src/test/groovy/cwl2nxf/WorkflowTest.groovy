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

package cwl2nxf

import org.yaml.snakeyaml.Yaml
import spock.lang.Specification
import com.fasterxml.jackson.databind.JsonNode;


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
        List<String> correct = ["infile = file('./data/ggal/ggal_1_48850000_49020000.Ggal71.500bpflank.fa')",
                                "pairone = file('./data/ggal/ggal_gut_1.fq')",
                                "pairtwo = file('./data/ggal/ggal_gut_2.fq')"]

        then:
        channels[0] == correct[0]
        channels[3] == correct[1]
        channels[4] == correct[2]
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
        def results = ['infile', 'act', 'gtf', 'pairone', 'pairtwo']


        when:
        def steps = wf.getChannels()



        then:
        steps.each{
            assert(it.split('=')[0].trim() in results)
        }
    }

}
