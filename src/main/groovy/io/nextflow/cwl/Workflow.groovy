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

import org.codehaus.groovy.runtime.NullObject

import java.nio.file.Path
import java.nio.file.Paths

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml

/**
 * @author Kevin Sayers <sayerskt@gmail.com>
 */
class Workflow{
    private List<String> channels
    private JsonNode cwlJson
    private JsonNode ymlJson
    private Path workingDir
    private List<Step> stepList
    private String docker = null
    public Map ymlmapping = [:]

    //refactor these
    private String cwl
    private String yml


    Workflow(){

    }

    Workflow(String cwl, String yml, String workingDir ) {
        this(cwl,yml, Paths.get(workingDir))
    }

    Workflow(String cwl, String yml, Path workingDir){
        //refactor
        this.cwl = cwl
        this.yml = yml

        //Remove this? ymlmapping no longer seems to be used
/*        this.ymlmapping = extractYmlMapping()

        println(this.ymlmapping)*/


        this.cwlJson = parseYml(cwl)
        this.ymlJson = parseYml(yml)
        this.workingDir = workingDir
        this.stepList = buildSteps()
        this.channels = extractChannels()
        this.docker = extractDocker()

    }

    private JsonNode parseYml(String ymlData){
        Yaml parser = new Yaml()
        ObjectMapper jsonMapper = new ObjectMapper()
        return jsonMapper.valueToTree(parser.load(ymlData))

    }
    private List<String> extractChannels(){
        List<String> channelList = []

        //Refactor this. It is used to handle null file inputs by making
        //a fake file. This was done so that files and null can be mixed in
        //a channel.
        channelList.add('NULL_FILE = java.nio.file.Paths.get(\'.NULL\')')
        channelList.add('Channel.from(NULL_FILE).set { data_ch }')

        Yaml parser = new Yaml()
        def ymldata = parser.load(this.yml)
        Map ymlmapping = [:]
        ymldata.keySet().each {
            if (ymldata[it].getClass() == String) {
                this.ymlmapping.put(it, ymldata[it])
                channelList.add(new String(it + ' = "' + ymldata[it]) + '"')
            }
            if (ymldata[it].getClass() == LinkedHashMap) {
                this.ymlmapping.put(it, ymldata[it]['path'])
                channelList.add("${it} = file('${ymldata[it]['path']}')")
            }
            if (ymldata[it].getClass() == ArrayList){
                def templist = []
                ymldata[it].each{
                    if(it.getClass() == String){
                        templist.add("'${it}'")
                    }
                    if(it.getClass() == Integer){
                        templist.add(it)
                    }
                    if(it.getClass() == NullObject){
                        templist.add('NULL_FILE')
                    }
                    if(it.getClass() == LinkedHashMap){
                        if('class' in it.keySet() && 'path' in it.keySet()){
                            if(it['class'] == 'File'){
                                templist.add("file('${it['path']}')")
                            }
                        }
                    }
                    if(it.getClass() == ArrayList){
                        it.each{
                            if('class' in it.keySet() && 'path' in it.keySet()){
                                if(it['class'] == 'File'){
                                    templist.add("file('${it['path']}')")
                                }
                            }
                        }
                    }
                }
                String tmpstring = new String(templist.join(","))
                this.ymlmapping.put(it, tmpstring)
                channelList.add("${it} = Channel.from(${tmpstring})")
            }

        }
        return channelList

    }

    private Map extractYmlMapping(){
        Yaml parser = new Yaml()
        def ymldata = parser.load(this.yml)
        Map ymlmapping = [:]

        ymldata.keySet().each {
            if (ymldata[it].getClass() == String) {
                ymlmapping.put(it, ymldata[it])
            }
            if (ymldata[it].getClass() == LinkedHashMap) {
                ymlmapping.put(it, ymldata[it]['path'])
            }

        }
        return ymlmapping
    }
    public JsonNode getCwlJson(){
        return this.cwlJson
    }
    public JsonNode getYmlJson(){
        return this.ymlJson
    }
    public JsonNode getSteps(){
        return this.cwlJson.get('steps')
    }
    public List<String> getChannels(){
        return this.channels
    }
    public List<Step> getSteplist(){
        return this.stepList
    }
    public Path getWorkingDir(){
        return this.workingDir
    }
    public String getDocker(){
        return this.docker
    }
    private List<Step> buildSteps(){
        List<Step> stepList = []

        //refactor
        Yaml parser = new Yaml()
        def data = parser.load(this.cwl)

        data['steps'].keySet().each {
            def stepins = data['steps'][it]['in']
            def stepFilename = data['steps'][it]['run']
            def stepID = stepFilename.replace('.cwl', '')

            if (stepID.contains('-')) {
                stepID = stepID.replace('-', '_')
            }
            def stepFile = workingDir.resolve(stepFilename).text
            def stepcwl = parser.load(stepFile)

            stepList.add(new Step(stepcwl, stepID, data, stepins, this.ymlJson))
        }



/*        getSteps().each{
            String stepFilename = new String(it.run.asText())
            String stepFile = new File(this.workingDir,stepFilename).text
        }*/
        return stepList

    }
    private String extractDocker(){
        if(this.cwlJson.hints != null){
            if(this.cwlJson.hints."DockerRequirement" != null){
                return this.cwlJson.hints."DockerRequirement"."dockerPull".asText()
            }
        }
        else{
            return null
        }

    }



}