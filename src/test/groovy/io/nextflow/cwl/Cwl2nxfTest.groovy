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
