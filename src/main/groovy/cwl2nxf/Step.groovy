#!/usr/bin/env groovy
package cwl2nxf

class Step{
	String cmdString
	String id
	def inputs
	def outputs
	def wfouts //These are the final files which are kept


	/** Only for testing purpose */
	protected Step() {

	}

	Step(stepdata, id, wfdata, stepins, ymldata){
		this.cmdString = extractCommandString(stepdata)
		this.id = id
		this.inputs = extractInputs(stepdata, wfdata, stepins)
		this.outputs = extractOutputs(stepdata,wfdata, stepins, ymldata)
		this.wfouts = extractWfouts(wfdata)


	}

	def extractCommandString(Map cwldata){
		def counter = 0 
		def cmdstr = ''

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
			if('prefix' in cwldata['inputs'][it]['inputBinding']){
				cmdstr = cmdstr + ' ' + cwldata['inputs'][it]['inputBinding']['prefix']
			}
			cmdstr = cmdstr + ' ${invar_' + counter + '}'
			counter += 1

		}
	
		return cmdstr

	}
	def extractArguments(cwldata){
		Map cwl_vals = ['$(runtime.outdir)':'./']
		def tmplist = ['',]
		if ('arguments' in cwldata.keySet()){
			cwldata['arguments'].each{
				if(cwl_vals.containsKey(it)){
					tmplist.add(cwl_vals[it])
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
	def extractInputs(cwldata, wfdata, stepins){
		def typemap = ['File':'file', 'string':'val','int?':'val','int':'val']
		def inputsreturn = []
		int counter = 0


		cwldata['inputs'].keySet().each{

			def intype = typemap[cwldata['inputs'][it]['type']]
			def from = stepins[it]
			if(from.getClass() == String){
				if(from.contains('/')){
					from = from.split('/')[-1]
				}
			}
			if(from.getClass() == LinkedHashMap){
				from = from[from.keySet()[0]]
			}

			inputsreturn.add(new String(intype + ' invar_' + counter + ' from ' + from))
			counter += 1

		}

        //Add in inputs that come directly from another step
/*        println(cwldata['inputs'])
        println(stepins)*/

		return inputsreturn

	}
	def extractOutputs(cwldata, wfdata, stepins, ymldata){
		def typemap = ['File':'file', 'string':'val','int?':'val','int':'val']
		def outputs = []
		def glob = ''
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
					outType = typemap[cwldata['outputs'][it]['type']]
				}
				else{
					outType = typemap[cwldata['outputs'][it]['type']['items']]
				}

				def into = it
				def keycheck = cwldata['outputs'][it].keySet()

				if(keycheck.contains('outputBinding')){
					glob =cwldata['outputs'][it]['outputBinding']['glob']

					if (glob.contains('$(inputs')){
						glob = glob.replace("\$(inputs.",'')
						glob = glob.replace(")",'')
						glob = ymldata[stepins[glob]]
					}
					outputs.add(new String(outType + ' "' + glob + '" into ' + into ))

				}
				if(keycheck.size() == 1){
					outType = typemap[cwldata['outputs'][it]['type']]
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
			outputs.add('')
		}

		return outputs
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
		if(this.outputs.size() > 0){
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
		processString += '\toutput: \n'
		this.outputs.each{
			processString += '\t' + it + '\n'
		}

		processString += '\t"""\n'
		processString += '\t' + this.cmdString + '\n'
		processString += '\t"""\n'
		processString += '}'



		return processString
	}
}
