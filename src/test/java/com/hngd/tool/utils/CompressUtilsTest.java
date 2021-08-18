package com.hngd.tool.utils;


import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class CompressUtilsTest {

	public static void main(String []args) throws RunnerException {
		Options opts=new OptionsBuilder()
				.include(CompressUtilsTest.class.getSimpleName())
				.forks(1)
				.build();
		new Runner(opts).run();
	}
	
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void testCompress() {
		File root = new File("/work/workspaces/build-tools/ntservice-demo/target/ntservice-demo");
		File zipFile = new File("test-output/target.zip");
		CompressUtils.compress(zipFile, root);
	}
	
	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public void testCompressWithBuffer() {
		File root = new File("/work/workspaces/build-tools/ntservice-demo/target/ntservice-demo");
		File zipFile = new File("test-output/target.zip");
		CompressUtils.compressWithBuffer(zipFile, root);
	}
}
