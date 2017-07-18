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

import java.nio.file.Path
import java.nio.file.Paths

import spock.lang.Specification
import spock.lang.Unroll
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class Cwl2nxfTest extends Specification {

    static Path PWD = Paths.get('.')

    static Path f(String file) { Paths.get(file) }

    @Unroll
    def 'should parse command line: #args' () {

        given:

        def main = new Cwl2nxf()

        when:
        main.parse(args.tokenize()) == result

        then:
        main.targetFileName == script
        main.targetConfigName == config
        main.ymlFile == yml
        main.cwlFile == cwl
        main.workDir == work

        where:
        args                                    | result    | script                 | config                           | work              | cwl           | yml
        'foo.cwl'                               | false     | null                   | null                             | null              | null          | null
        'foo.cwl foo.yml'                       | true      | null                   | null                             | PWD               | f('foo.cwl')  | f('foo.yml')
        '-o bar.nf foo.cwl foo.yml '            |  true     | f('bar.nf')            | f('nextflow.config')             | PWD               | f('foo.cwl')  | f('foo.yml')
        '-o /some/path/bar.nf foo.cwl foo.yml'  |  true     | f('/some/path/bar.nf') | f('/some/path/nextflow.config')  | PWD               | f('foo.cwl')  | f('foo.yml')

    }

}
