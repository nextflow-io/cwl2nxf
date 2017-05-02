package cwl2nxf

import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
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

}
