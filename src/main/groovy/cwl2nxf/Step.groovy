#!/usr/bin/env groovy
package cwl2nxf

class Step{
	String cmd_string
	String id
	def inputs
	def outputs
	def wfouts //These are the final files which are kept


	Step(stepdata, id, wfdata, stepins, ymldata){
		this.cmd_string = extract_command_string(stepdata)
		this.id = id
		this.inputs = extract_inputs(stepdata, wfdata, stepins)
		this.outputs = extract_outputs(stepdata,wfdata, stepins, ymldata)
		this.wfouts = extract_wfouts(wfdata)


	}
	def extract_command_string(cwldata){
		def counter = 0 
		def cmdstr = ''
		//Deal with if the base command is a string or a list of commands
		if (cwldata['baseCommand'].getClass() == String){
			cmdstr += cwldata['baseCommand']
		}
		else{
			cmdstr += cwldata['baseCommand'].join(" ")

		}
		cmdstr = cmdstr + extract_arguments(cwldata)

		cwldata['inputs'].keySet().each{
			if('prefix' in cwldata['inputs'][it]['inputBinding']){
				cmdstr = cmdstr + ' ' + cwldata['inputs'][it]['inputBinding']['prefix']
			}
			cmdstr = cmdstr + ' ${invar_' + counter + '}'
			counter += 1

		}
	
		return cmdstr

	}
	def extract_arguments(cwldata){
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
	def extract_inputs(cwldata, wfdata, stepins){
		def typemap = ['File':'file', 'string':'val']
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
		return inputsreturn

	}
	def extract_outputs(cwldata, wfdata, stepins, ymldata){
		def typemap = ['File':'file', 'string':'val']
		def outputs = []
		def glob = ''
		def outType = ''
/*		println(cwldata.keySet())
*/		

		if(cwldata.keySet().contains('stdout')){
			if(cwldata['stdout'].contains('.')){
				typemap.put('stdout','file')
			}
		}

		cwldata['outputs'].keySet().each{
			outType = typemap[cwldata['outputs'][it]['type']]
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
				if(this.cmd_string.contains('gunzip -c')){
					if(!this.cmd_string.contains('>')){
						this.cmd_string += ' > ' + glob
					}
				}
			}


		}

		return outputs
	}
	def extract_wfouts(cwldata){
		//efficiency of this step could be improved it currently parses
		//the same thing for each step. 
		def outs = []

		cwldata['outputs'].keySet().each{
			String outsrc = cwldata['outputs'][it]['outputSource']
			if(outsrc.contains('/')){
				outs.add(outsrc.split('/')[-1])
			}
		}

		return outs
	}
	def get_cmd_string(){
		return this.cmd_string
	}
	def get_id(){
		return this.id
	}
	def get_inputs(){
		return this.inputs
	}
	def get_outputs(){
		return this.outputs
	}
	def get_process_block(){
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
		processString += '\t' + this.cmd_string + '\n'
		processString += '\t"""\n'
		processString += '}'



		return processString
	}
}
