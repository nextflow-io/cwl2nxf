echo true

atest = Channel.from('a','b','c')

process first{
	input:
	val invar0 from atest

	"""
	echo ${invar0}
	"""
}

process second{
	input:
	val invar1 from atest

	"""
	echo ${invar1}
	"""
}