package counters;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Group)
@Fork(1)
public class CountersBenchmark {
	private Counter counter;

	@Param
	CounterFactory.CounterType counterType;

	@Setup
	public void buildMeCounterHearty() {
		counter = CounterFactory.build(counterType);
	}

	@Benchmark
	@Group("rw")
	@GroupThreads(8)
	public void inc() {
		counter.inc();
	}

	@Benchmark
	@Group("rw")
	@GroupThreads(1)
	public long get() {
		return counter.get();
	}
}
