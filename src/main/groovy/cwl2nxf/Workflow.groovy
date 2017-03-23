package cwl2nxf

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml

/**
 * Created by kevin on 23/03/17.
 */
class Workflow{
    private List<String> channels
    private JsonNode cwlJson
    private JsonNode ymlJson
    private String workingDir
    private List<Step> stepList
    private String docker = null

    //refactor these
    private String cwl
    private String yml




    public Workflow(String cwl, String yml, String workingDir){
        //refactor
        this.cwl = cwl
        this.yml = yml

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

        Yaml parser = new Yaml()
        def data = parser.load(this.cwl)
        def ymldata = parser.load(this.yml)
        Map ymlmapping = [:]

        ymldata.keySet().each {
            if (ymldata[it].getClass() == String) {
                ymlmapping.put(it, ymldata[it])
                channelList.add(new String(it + ' = Channel.from("' + ymldata[it]) + '")')
            }
            if (ymldata[it].getClass() == LinkedHashMap) {
                ymlmapping.put(it, ymldata[it]['path'])
                channelList.add("${it} = Channel.fromPath('${ymldata[it]['path']}')")
            }

        }
        return channelList

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
    public String getWorkingDir(){
        return this.workingDir
    }
    private List<Step> buildSteps(){
        List<Step> stepList = []

        //refactor
        Yaml parser = new Yaml()
        def data = parser.load(this.cwl)
        def ymldata = parser.load(this.yml)
        Map ymlmapping = [:]
        def channelList = []
        Map wfinputs = [:]

        data['steps'].keySet().each {
            def stepins = data['steps'][it]['in']
            def stepFilename = data['steps'][it]['run']
            def stepID = stepFilename.replace('.cwl', '')

            if (stepID.contains('-')) {
                stepID = stepID.replace('-', '_')
            }
            def stepFile = new File(this.workingDir,stepFilename).text
            def stepcwl = parser.load(stepFile)
            stepList.add(new Step(stepcwl, stepID, data, stepins, ymlmapping))
        }


/*        getSteps().each{
            String stepFilename = new String(it.run.asText())
            String stepFile = new File(this.workingDir,stepFilename).text
        }*/
        return stepList

    }
    private String extractDocker(){
        return this.cwlJson.hints."DockerRequirement"."dockerPull".asText()

    }



}