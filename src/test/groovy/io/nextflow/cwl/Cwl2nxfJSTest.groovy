package io.nextflow.cwl

import spock.lang.Specification

/**
 * @author Kevin Sayers <sayerskt@gmail.com>
 */
class Cwl2nxfJSTest extends Specification {
    def 'test the evaluateJS function' (){
        given:
        def jsevaluator = new Cwl2nxfJS()
        def jsTestString = 'runtime.coresMin'
        def jsTestoutdir = 'runtime.outdir'

        when:
        def testresult = jsevaluator.evaluateJS(jsTestString)
        def outdirresult = jsevaluator.evaluateJS(jsTestoutdir)
        then:
        testresult == 1
        outdirresult == "./"

    }
    def 'check that regex correctly matches' () {

        given:
        def jsevaluator = new Cwl2nxfJS()
        def jsTestString = 'sentinel_runtime=cores,$(runtime[\'cores\']),ram,$(runtime[\'ram\'])'

        when:
        def testresult = jsevaluator.checkForJSPattern(jsTestString)
        then:
        testresult == true

    }
    def 'check that JS evaluation returns correct string'(){
        given:
        def jsevaluator = new Cwl2nxfJS()
        def jsTestString = '$(runtime.coresMin)'

        when:
        def testresult = jsevaluator.evaluateJSExpression(jsTestString)
        then:
        testresult['$(runtime.coresMin)'] == 1
    }
    def 'check setting of runtime values' (){
        given:
        def jsevaluator = new Cwl2nxfJS()
        def jsTestString = '$(runtime.coresMin)'

        when:
        jsevaluator.setJS(['runtime':['coresMin': 3]])
        def testresult = jsevaluator.evaluateJSExpression(jsTestString)
        then:
        testresult['$(runtime.coresMin)'] == 3
    }
    def 'test updating a JS value and accesing it again' (){
        given:
        def jsevaluator = new Cwl2nxfJS()

        when:
        jsevaluator.setJS(['runtime':['coresMin': 3]])
        def newaccess = jsevaluator.evaluateJS('runtime.coresMin')

        then:
        newaccess == 3
    }
    def 'test conversion of runtime.cores' (){
        given:
        def jsevaluator = new Cwl2nxfJS()

        when:
        jsevaluator.setJS(['runtime':['coresMax': 3]])

        def newaccess = jsevaluator.evaluateJS('runtime.cores')
        println(newaccess)

        then:
        newaccess == 1
    }
    def 'test multiple inline JS expressions' (){
        given:
        def jsevaluator = new Cwl2nxfJS()

        when:
        String test = 'sentinel_runtime=cores,$(runtime[\'cores\']),ram,$(runtime[\'ram\'])'
        def newaccess = jsevaluator.evaluateJSExpression(test)
        println(newaccess)

        Map correct = ["\$(runtime['cores'])":1, "\$(runtime['ram'])":1024]
        then:
        newaccess == correct
    }

}
