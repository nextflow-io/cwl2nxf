package io.nextflow.cwl

import org.yaml.snakeyaml.Yaml
import sun.awt.image.ImageWatched

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
/**
 * @author Kevin Sayers <sayerskt@gmail.com>
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager
import javax.script.*
import com.fasterxml.jackson.databind.ObjectMapper;

class Cwl2nxfJS {
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("nashorn");

    SimpleBindings bindings = new SimpleBindings(['runtime':['coresMin': 1,'coresMax': 1,'ramMin': 1024,
                                                             'ramMax': 1024,'tmpdirMin': 1024,'tmpdirMax': 1024,
                                                             'outdirMin': 1024,'outdirMax': 1024,'outdir':'./',
                                                             'cores':1, 'ram':1024]])
    Cwl2nxfJS(){

    }
    def evaluateJS(String jsString){
        if('runtime.cores' in jsString){
            jsString.replace('runtime.cores','runtime.coresMax')
        }
        return this.engine.eval(jsString, this.bindings)
    }

    def setJS(Map map, def bind=null){
        map.keySet().each{
            if(it in this.bindings.keySet()){
                setJS(map[it],this.bindings[it])
            }
            else{
                bind[it] = map[it]
            }
        }

    }
    def checkForJSPattern(String jsString){
        def jsRegex = (jsString =~ /^*\$\((.*?)\)*$/).find()
        return jsRegex
    }
    def evaluateJSExpression(String jsString){
        Map results = [:]
        def jsRegex = (jsString =~ /\$\([^\)]+\)/).findAll()
        jsRegex.each{
            try{
                def subRegex = (it =~ /^\$\((.*?)\)$/)
                subRegex.matches()
                results[it] = evaluateJS(subRegex.group(1))
            }
            catch (Exception jsExpression){
                throw new Exception('Error: no valid JS expression found')
            }
        }
        return results
    }

    def setJSInputs(cwldata, wfdata, stepins, ymldata){
        Map inputsTemp = [:]
        ObjectMapper mapper = new ObjectMapper()
        Map<String, Object> converted = mapper.convertValue(ymldata, Map.class)


        cwldata['inputs'].keySet().each{
            if(stepins[it] in wfdata['inputs'].keySet()){
                if(converted[stepins[it]].getClass() != LinkedHashMap){
                    inputsTemp.put(it, converted[stepins[it]])
                }
                if(converted[stepins[it]].getClass() == LinkedHashMap){
                    if(converted[stepins[it]]['class'] == 'File'){
                        inputsTemp.put(it, parseFileObject(converted[stepins[it]]))
                    }
                }
            }


        }
        if(inputsTemp){
            this.bindings.putAll([inputs: inputsTemp])
        }
    }

    def parseFileObject(fileMap){
        Map tempMap = [:]
        String tempPath = fileMap['path']
        tempMap.put('path', tempPath)
        tempMap.put('location', tempPath)
        tempMap.put('basename', tempPath.split('/')[-1])
        tempMap.put('nameext', tempPath.split(/\./)[-1])
        tempMap.put('nameroot', tempPath.split('/')[-1].split(/\./)[0])

        return tempMap


    }
    static void main(String[] args) {
        def test = new Cwl2nxfJS()

        Map testmap = [:]

/*        test.setJS(['runtime':['test':['foo':20,'bar':5]]])
        println(test.evaluateJS('runtime.test.bar'))*/


        //String testreg = 'sentinel_runtime=cores,$(runtime[\'cores\']),ram,$(runtime[\'ram\'])'
        String testreg = '$(runtime.cores)'
        println(test.evaluateJSExpression(testreg))
/*        String jsString = '$(runtime[\'cores\'])'
        println(test.checkForJSPattern(jsString))*/





    }
}
