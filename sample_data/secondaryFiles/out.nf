NULL_FILE = java.nio.file.Paths.get('.NULL')
Channel.from(NULL_FILE).set { data_ch }
bam = file('./data/test.bam')

process bamindex{ 
	disk '2048 MB'
	memory '2048 MB'
	input: 
	file invar_0 from bam
	output: 
	file "*.bai" into temp0
	"""
	samtools index ${invar_0} test.bai
	"""
}