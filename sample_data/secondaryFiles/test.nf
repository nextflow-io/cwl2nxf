bamfile = Channel.fromPath('data/test.bam')

process test{
	input:
	file notbam from bamfile

	output:

	file("*.bam") includeInputs true into temp1
	file("*.bai") into temp2
	


	"""
	samtools index ${notbam} test.bai
	"""



}
outfiles = Channel.create()
temp1.merge(temp2) {t1, t2 -> [t1,t2]}.into(outfiles)

process check{
	echo true
	input:
	set file(x), file(y) from outfiles

	"""
	ls
	"""
}