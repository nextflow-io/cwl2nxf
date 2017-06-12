package io.nextflow.cwl
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

class Cwl2nxfJS {
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("nashorn");

    SimpleBindings bindings = new SimpleBindings(['runtime':['coresMin': 1,'coresMax': 1,'ramMin': 1024,
                                                             'ramMax': 1024,'tmpdirMin': 1024,'tmpdirMax': 1024,
                                                             'outdirMin': 1024,'outdirMax': 1024,'outdir':'./',
                                                             'cores':1, 'ram':1]])
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
        def jsRegex = (jsString =~ /^\$\((.*?)\)$/)
        return jsRegex.matches()
    }
    def evaluateJSExpression(String jsString){
        def jsRegex = (jsString =~ /^\$\((.*?)\)$/)
        try{
            jsRegex.matches()
            evaluateJS(jsRegex.group(1))
            return result
        }
        catch (Exception jsExpression){
            println('Error: no valid JS expression found')
            return null
        }


    }
    static void main(String[] args) {
        def test = new Cwl2nxfJS()

        Map testmap = [:]
/*
        test.setJS(['runtime':['coresMin': 3]])
        println(test.evaluateJS('runtime.coresMin'))
        test.setJS(['runtime':['coresMin': 25]])
        println(test.evaluateJS('runtime.coresMin'))*/


        String testreg = 'sentinel_runtime=cores,$(runtime[\'cores\']),ram,$(runtime[\'ram\'])'
        test.evaluateJSExpression(testreg)




    }
}
