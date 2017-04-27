package cwl2nxf

import org.yaml.snakeyaml.Yaml
import spock.lang.Specification
import com.fasterxml.jackson.databind.JsonNode;


/**
 * Created by ksayers on 27/04/17.
 */
class WorkflowTest extends Specification{
    def 'test the parseYaml' (){
        given:
        String text = '''
        cwlVersion: v1.0
        class: Workflow
        '''
                .stripIndent()


        def wf = new Workflow()

        when:
        def yml = wf.parseYml(text).getClass()

        then:
        yml == com.fasterxml.jackson.databind.node.ObjectNode
    }
}
