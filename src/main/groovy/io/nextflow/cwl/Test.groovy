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
