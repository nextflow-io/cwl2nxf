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
import sun.awt.image.ImageWatched

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
