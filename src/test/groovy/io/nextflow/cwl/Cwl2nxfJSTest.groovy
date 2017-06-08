package io.nextflow.cwl

import spock.lang.Specification

/**
 * @author Kevin Sayers <sayerskt@gmail.com>
 */
class Cwl2nxfJSTest extends Specification {
    def 'check that regex correctly matches' () {

        given:
        def jsevaluator = new Cwl2nxfJS()
        def jsTestString = '$(runtime.coresMin)'

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
        testresult == 1
    }
    def 'check setting of runtime values' (){
        given:
        def jsevaluator = new Cwl2nxfJS()
        def jsTestString = '$(runtime.coresMin)'

        when:
        jsevaluator.setJS(['runtime':['coresMin': 3]])
        def testresult = jsevaluator.evaluateJSExpression(jsTestString)
        then:
        testresult == 3
    }
}
