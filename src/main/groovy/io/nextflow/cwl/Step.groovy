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
/**
 * @author Kevin Sayers <sayerskt@gmail.com>
 */
package io.nextflow.cwl

import com.fasterxml.jackson.databind.node.ArrayNode
class Step{
	String cmdString
	String id
	def inputs
	def outputs
	def wfouts //These are the final files which are kept
	def jsEvaluator = new Cwl2nxfJS()
    def hints



	/** Only for testing purpose */
	protected Step() {

	}

	protected Step(testid, wfout, String id, ins, outs, cmds){
		this.cmdString = cmds
		this.id = id
		this.inputs = ins
		this.outputs = outs
		this.wfouts = wfout
	}

	Step(stepdata, id, wfdata, stepins, ymldata){

		this.cmdString = extractCommandString(stepdata, stepins)
		this.id = id
        this.jsEvaluator.setJSInputs(stepdata, wfdata, stepins, ymldata)

        if('hints' in stepdata.keySet()){
            this.hints = extractHints(stepdata)
        }

		this.inputs = extractInputs(stepdata, wfdata, stepins, ymldata)
		this.outputs = extractOutputs(stepdata,wfdata, stepins, ymldata)
		this.wfouts = extractWfouts(wfdata)



	}


    def extractHints(stepdata){
        def hintsReturn = []

        stepdata['hints'].each{
            if(it.getClass() == LinkedHashMap){
                def keys = it.keySet()
                if(it['class'] == 'ResourceRequirement'){
                    this.jsEvaluator.setJS(['runtime':it])
                    if('outdirMin' in keys){
                        hintsReturn.add("disk \'${this.jsEvaluator.evaluateJS('runtime.outdirMin')} MB\'")
                    }
                    if('ramMin' in keys){
                        hintsReturn.add("memory \'${this.jsEvaluator.evaluateJS('runtime.outdirMin')} MB\'")
                    }
                    if('coresMin' in keys && !('coresMax' in keys)){
                        hintsReturn.add("cpus ${this.jsEvaluator.evaluateJS('runtime.coresMin')}")
                    }
                    if('coresMax' in keys && !('coresMin' in keys)){
                        hintsReturn.add("cpus ${this.jsEvaluator.evaluateJS('runtime.coresMax')}")
                    }
                    if('coresMin' in keys && 'coresMax' in keys){
                        hintsReturn.add("cpus ${this.jsEvaluator.evaluateJS('runtime.coresMax')}")
                    }
                } else if (it['class'] == 'DockerRequirement'){
                    hintsReturn.add("container ${it['dockerPull']}")

                } else{
                    throw new IllegalArgumentException("An unsupported hint is present in the ${this.id} step")
                }
            }
            if(it.getClass() == LinkedHashMap$Entry){
                if(it.key == 'DockerRequirement'){
                    hintsReturn.add("container ${it.value['dockerPull']}")
                }
            }

        }


        return hintsReturn
    }

	def extractCommandString(Map cwldata, stepins){
		int counter = 0
		String cmdstr = ''

		//Deal with if the base command is a string or a list of commands
		def baseCommand = cwldata.baseCommand
		if ( baseCommand instanceof String){
			cmdstr += baseCommand
		}
		else if( baseCommand instanceof List ) {
			cmdstr += baseCommand.join(" ")
		}
		else {
			throw new IllegalArgumentException("Not a valid `baseCommand`: $baseCommand [${baseCommand?.getClass()}]")
		}

		cmdstr = cmdstr + extractArguments(cwldata)
		cwldata['inputs'].keySet().each{
            String tmpcmdstr =''

			//First check if the input from the step is also in the workflow
			if(it in stepins.keySet() && cwldata['inputs'][it].getClass() != String) {
				Map inputBinding = null

				//Find the inputBinding this should work for standard binding and array type
				if('inputBinding' in cwldata['inputs'][it].keySet()){
					inputBinding = cwldata['inputs'][it]['inputBinding']
				}
				if(cwldata['inputs'][it]['type'].getClass() == LinkedHashMap &&'inputBinding' in cwldata['inputs'][it]['type'].keySet()){
					inputBinding = cwldata['inputs'][it]['type']['inputBinding']
				}

				if ('prefix' in inputBinding) {
					String prefixStripped = inputBinding['prefix'].toString().stripIndent()
					if(prefixStripped.contains("=")){
						tmpcmdstr = tmpcmdstr + prefixStripped
					}
					else{
						tmpcmdstr = tmpcmdstr + prefixStripped + ' '
					}
				}


                //Check if the input has an actual commandline position
                if(inputBinding != null) {
                    tmpcmdstr = tmpcmdstr + '${invar_' + counter + '}'
                }

                if(cwldata['inputs'][it]['type'].getClass() == LinkedHashMap && cwldata['inputs'][it]['type']['items'].getClass() == ArrayList){
                    //${invar_9 != NULL_FILE ? "reference__bwa__indexes= ${invar_9}" : ''}
                    cmdstr = cmdstr + " \${invar_${counter} != NULL_FILE ? \"${tmpcmdstr}\" : ''}"
                }
                else{
                    cmdstr = cmdstr + ' ' + tmpcmdstr
                }

				counter += 1

			}
			else if(it in stepins.keySet() && cwldata['inputs'][it].getClass() == String) {
				//this checks for inputs that are just a Directory or other string without any
				//additional attributes.
				if(cwldata['inputs'][it] != 'Directory'){
					throw new IllegalArgumentException("Unsupported input from string")
				}
			}
			else{
				//Check if the step input has a default value if it does include it in the command string
				if('default' in cwldata['inputs'][it].keySet()){
					if ('prefix' in cwldata['inputs'][it]['inputBinding']) {
						cmdstr = cmdstr + ' ' + cwldata['inputs'][it]['inputBinding']['prefix']
					}
					cmdstr = cmdstr + ' ' + cwldata['inputs'][it]['default']
					//counter += 1
                    //This counter shouldn't be incremented as the default is not coming from
                    //an invar
				}

			}

		}
	
		return cmdstr

	}
	def extractArguments(cwldata){
		def tmplist = ['',]
		if ('arguments' in cwldata.keySet()){
			cwldata['arguments'].each{
				if(it.getClass() == LinkedHashMap){
					if('valueFrom' in it.keySet()){
						it = it['valueFrom']
					}
				}
				if(this.jsEvaluator.checkForJSPattern(it) == true){
					String jsEval = it
					Map jsMapping = this.jsEvaluator.evaluateJSExpression(it)
					jsMapping.keySet().each{
						jsEval = jsEval.replace(it,jsMapping[it].toString())

					}

					tmplist.add(jsEval)

				}
				else{
					tmplist.add(it)
				}
			}

			return tmplist.join(" ")
		}

		else{
			return ''
		}
	}

	/*
	cwlTypeConversion: takes in a type from CWL and returns the
	corresponding Nextflow type.
	 */
	def cwlTypeConversion(cwltype){
		def typemap = ['File':'file',
					   'string':'val',
					   'int?':'val',
					   'int':'val',
					   'Directory':'file',
					   'File[]':'file',
					   'long':'val',
					   'null':'val']
		if(typemap[cwltype] != null){
			return typemap[cwltype]
		}
		else{
			throw new IllegalArgumentException("${cwltype} is not a supported data type")
		}
	}
	def extractInputs(cwldata, wfdata, stepins, yml){
		def inputsreturn = []
		def secondaryFiles = []
		int counter = 0

		cwldata['inputs'].keySet().each{
			def intype = null

			//This is a bit of a hack. It handles things like an input which
			//is just a directory defined as inputvar: Directory
			if(cwldata['inputs'][it].getClass() == String){
				cwldata['inputs'][it] = ['type':cwldata['inputs'][it]]
			}
			if(cwldata['inputs'][it]['type'].getClass() == String){
				intype = cwlTypeConversion(cwldata['inputs'][it]['type'])
			}
			if(cwldata['inputs'][it]['type'].getClass() == LinkedHashMap){
				if(cwldata['inputs'][it]['type']['items'].getClass() == LinkedHashMap){
					intype = cwlTypeConversion(cwldata['inputs'][it]['type']['items']['items'])

				}
				else{
                   /*This operates under the assumption that an ArrayList for items will always
                    be a case of File and null. The type conversion is just done for File then*/
                    if(cwldata['inputs'][it]['type']['items'].getClass() == ArrayList){
                        if('File' in cwldata['inputs'][it]['type']['items']){
                            intype = cwlTypeConversion('File')
                        }
                    }
                    else{
                        intype = cwlTypeConversion(cwldata['inputs'][it]['type']['items'])
                    }

				}
			}

			def from = stepins[it]
			if(from.getClass() == String){
				if(from.contains('/')){
					from = from.split('/')[-1]
				}
			}
			if(from.getClass() == LinkedHashMap){
				from = from[from.keySet()[0]]
			}
			//Check if an input is present in the workflow so inputs that are not in the workflow are ignored
			if(it in stepins.keySet()){
				inputsreturn.add(new String(intype + ' invar_' + counter + ' from ' + from))
				counter += 1
                def tmpSecondaryFiles = extractSecondaryFiles(it, yml)
				if(tmpSecondaryFiles){
					secondaryFiles.addAll(tmpSecondaryFiles)
				}

			}

		}

		if(secondaryFiles){
			inputsreturn.add("\n\n\t//Secondary Files from CWL")
			inputsreturn.addAll(formatSecondaryFiles(secondaryFiles))
		}
		return inputsreturn

	}

	def formatSecondaryFiles(secondaryFiles){
		int counter=0
		secondaryFiles.unique().collect {
			"file secondary_${counter++} from file(${it})"
		}
	}

    def extractSecondaryFiles(stepID, yml){
		//extracts Secondary Files that are associated with a primary file
		def secondaryFiles = []

        if(yml[stepID].getClass() == ArrayNode){
            yml[stepID].each{
                if(it.getClass() == com.fasterxml.jackson.databind.node.ObjectNode){
                    it["secondaryFiles"].each{
						secondaryFiles.add(it["path"])
					}
                }
            }
        }
		return secondaryFiles
    }

	def extractOutputs(cwldata, wfdata, stepins, ymldata){
		def outputs = []
		def secondaryFiles = []
		def outType = ''

		if(cwldata['outputs'] != []){
			if(cwldata.keySet().contains('stdout')){
				if(cwldata['stdout'].contains('.')){
					typemap.put('stdout','file')
				}
			}

			cwldata['outputs'].keySet().each{
				//This checks for the outputs being an array
				if(cwldata['outputs'][it]['type'].getClass() == String){
					outType = cwlTypeConversion(cwldata['outputs'][it]['type'])
				}
				else{
					outType = cwlTypeConversion(cwldata['outputs'][it]['type']['items'])
				}

				def into = it
				def keycheck = cwldata['outputs'][it].keySet()

				if(keycheck.contains('outputBinding')){
					outputs.addAll(processOutputbinding(cwldata,into,outType, ymldata,stepins,it))
				}
				if(keycheck.size() == 1){
					outType = cwlTypeConversion(cwldata['outputs'][it]['type'])
					glob = cwldata['stdout']
					outputs.add(new String(outType + ' "' + glob + '" into ' + into ))

					//This covers a specific case where no file is output as named
					//normally this would cause nextflow to crash.
					if(this.cmdString.contains('gunzip -c')){
						if(!this.cmdString.contains('>')){
							this.cmdString += ' > ' + glob
						}
					}
				}
		}

		}
		else{
			outputs.add(null)
		}

		return outputs
	}
    def processOutputbinding(cwldata,into,outType, ymldata,stepins,out){
		def glob = ''
		def returns = []
		boolean secondaryFilesFound = false

		if('glob' in cwldata['outputs'][out]['outputBinding'].keySet()) {
			glob = cwldata['outputs'][out]['outputBinding']['glob']
			if(glob == '.'){
				glob = '*'
			}
/*        if(this.jsEvaluator.checkForJSPattern(glob, into)){
            println(glob)
        }*/
			if (glob.contains('$(inputs')){
				glob = glob.replace("\$(inputs.",'')
				glob = glob.replace(")",'')
				glob = ymldata[stepins[glob]]
			}
			returns.add("${outType} \"${glob}\" into ${into}")
			if('secondaryFiles' in cwldata['outputs'][out].keySet()) {
				secondaryFilesFound = true
				int counter = 0
				cwldata['outputs'][out]['secondaryFiles'].each{
					if((glob =~ /\*\.(.*)/).find() && (it =~ /\^\.(.*)/)){
						String secondaryGlob = (it.replace('^','*'))
						returns.add("${outType} \"${secondaryGlob}\" into temp${counter}")
						counter++
					}

				}
			}
		}
		else{
			throw new IllegalArgumentException("outputBinding can only be of type 'glob'")
		}
		if(secondaryFilesFound == true){
			returns = secondaryFilesProcessing(returns, into)
		}
		return returns
    }

	def secondaryFilesProcessing(returns,into){
		String newChannel = "${into} = Channel.create()"
		println(into)
		return returns

	}
	def extractWfouts(cwldata){
		//efficiency of this step could be improved it currently parses
		//the same thing for each step. 
		def outs = []

		if(cwldata['outputs'] != []){
			cwldata['outputs'].keySet().each{
				String outsrc = cwldata['outputs'][it]['outputSource']
				if(outsrc.contains('/')){
					outs.add(outsrc.split('/')[-1])
				}
			}

		}
		else{
			outs.add('')
		}

		return outs
	}
	def getCmdString(){
		return this.cmdString
	}
	def getId(){
		return this.id
	}
	def getInputs(){
		return this.inputs
	}
	def getOutputs(){
		return this.outputs
	}
	def getProcessBlock(){
		String processString = ''
		processString += '\n'
		processString += 'process ' + this.id + '{ \n'
        if(this.hints){
            this.hints.each{
                processString += "\t${it}\n"
            }
        }
		if(this.outputs.size() > 0 && this.outputs != [null]){
			this.outputs.each{
				if(this.wfouts.contains(it.split(' ')[-1])){
					processString += "\tpublishDir './', mode:'copy' \n"

				}
			}
		}


		processString += '\tinput: \n'
		this.inputs.each{
			processString += '\t' + it + '\n'
		}
        if(this.outputs != [null]){
            processString += '\toutput: \n'
            this.outputs.each{
                processString += '\t' + it + '\n'
            }
        }

		processString += '\t"""\n'
		processString += '\t' + this.cmdString + '\n'
		processString += '\t"""\n'
		processString += '}\n'

		return processString
	}
}
