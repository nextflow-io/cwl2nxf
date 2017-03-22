#!/usr/bin/env groovy
package cwl2nf

/*
@Grab(group='org.yaml', module='snakeyaml', version='1.18')
*/

import org.yaml.snakeyaml.Yaml


class Cwl2nxf{
    static void main(String[] args) {
		String workingDir = System.getProperty("user.dir")
		String infile = ''
		String ymlfile = ''
		String inpath = args[0]
		String ymlpath = args[1]
		Yaml parser = new Yaml()
		infile = new File(inpath).text
		ymlfile = new File(ymlpath).text

		def data = parser.load(infile)
		def ymldata = parser.load(ymlfile)



		def stepList = []
		Map ymlmapping = [:]
		def channelList = []
		Map wfinputs = [:]


		ymldata.keySet().each{
			if (ymldata[it].getClass() == String){
				ymlmapping.put(it, ymldata[it])
				channelList.add(new String(it + ' = Channel.from("' + ymldata[it]) + '")')
			}
			if (ymldata[it].getClass() == LinkedHashMap){
				ymlmapping.put(it, ymldata[it]['path'])
				channelList.add(new String(it + ' = Channel.fromPath("' +ymldata[it]['path'] + '")' ))
			}

		}


		data['steps'].keySet().each{
			def stepins = data['steps'][it]['in']
			def stepFilename = data['steps'][it]['run']
			def stepID = stepFilename.replace('.cwl','')
			stepFilename = inpath.replace(inpath.split('/')[-1], stepFilename)

			if(stepID.contains('-')){
				stepID = stepID.replace('-','_')
			}
			def stepFile = new File(stepFilename).text
			def stepcwl = parser.load(stepFile)
			stepList.add(new Step(stepcwl, stepID, data, stepins, ymlmapping))
		}


		String fileName = new Date().getTime() + '.nf'
		def outfile = new File(fileName)
		channelList.each{
			println(it)
			outfile.append(it + '\n')
		}
		stepList.each{
			println(it.get_process_block())
			outfile.append(it.get_process_block() + '\n')
		}

		//println(new String("nextflow run " + fileName).execute().text)

    }
}

