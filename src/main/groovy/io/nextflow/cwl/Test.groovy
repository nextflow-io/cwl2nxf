package io.nextflow.cwl

/**
 * Created by ksayers on 4/05/17.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml


class Test {

    static void main(String[] args) {
        String fileContentsd = new File('/home/ksayers/cwlconversion_work/Experimental_WFS/hellod3.cwl').text
        String fileContentsv = new File('/home/ksayers/cwlconversion_work/Experimental_WFS/hellov1.cwl').text
        Yaml parser = new Yaml()
        ObjectMapper jsonMapper = new ObjectMapper()
        println(jsonMapper.valueToTree(parser.load(fileContentsd)))
        println(jsonMapper.valueToTree(parser.load(fileContentsv)))


    }
}
