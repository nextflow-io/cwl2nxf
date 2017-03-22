inp = Channel.fromPath("hello.tar")
ex = Channel.from("Hello.java")

process tar_param{ 
	input: 
	file invar_0 from inp
	val invar_1 from ex
	output: 
	file "Hello.java" into example_out
	"""
	tar xf ${invar_0} ${invar_1}
	"""
}

process arguments{ 
	publishDir './', mode:'copy' 
	input: 
	file invar_0 from example_out
	output: 
	file "*.class" into classfile
	"""
	javac -d ./ ${invar_0}
	"""
}
