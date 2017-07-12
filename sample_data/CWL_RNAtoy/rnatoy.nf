NULL_FILE = java.nio.file.Paths.get('.NULL')
Channel.from(NULL_FILE).set { data_ch }
infile = file('data/ggal/ggal_1_48850000_49020000.Ggal71.500bpflank.fa')
act = "genome.index"
gtf = file('data/ggal/ggal_1_48850000_49020000.bed.gff')
pairone = file('data/ggal/ggal_gut_1.fq')
pairtwo = file('data/ggal/ggal_gut_2.fq')

process bowtiemod{ 
	input: 
	file invar_0 from infile
	val invar_1 from act
	output: 
	file "*" into indexout
	"""
	bowtie2-build ${invar_0} ${invar_1}
	"""
}


process tophatmod{ 
	input: 
	file invar_0 from indexout
	file invar_1 from gtf
	val invar_2 from act
	file invar_3 from pairone
	file invar_4 from pairtwo
	output: 
	file "./tophat_out/accepted_hits.bam" into tophatout
	"""
	tophat2  --GTF ${invar_1} ${invar_2} ${invar_3} ${invar_4}
	"""
}


process cufflinksmod{ 
	publishDir './', mode:'copy' 
	input: 
	file invar_0 from gtf
	file invar_1 from tophatout
	output: 
	file "transcripts.gtf" into cuffout
	"""
	cufflinks --no-update-check -q -G ${invar_0} ${invar_1}
	"""
}