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

        when:
        def cmd = step.extractCommandString(cwl)
        then:
        cmd == 'bowtie2-build ${invar_0} ${invar_1}'

    }

}
