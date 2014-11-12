package counters;

/**
 * @author Andrey Lomakin <a href="mailto:lomakin.andrey@gmail.com">Andrey Lomakin</a>
 * @since 03/11/14
 */
public class CounterFactory {
	public enum CounterType {
		ATOMIC, RAW_MAP, TL_HASH_MAP, MAP_NOT_LAZY, MAP_LAZY, MAP_GLOBAL
	}

	public static Counter build(CounterType type) {
		switch (type) {
			case ATOMIC:
				return new AtomicCounter();
			case RAW_MAP:
				return new RawMapCounter();
			case TL_HASH_MAP:
				return new MapCounterWithTLHash();
			case MAP_NOT_LAZY:
				return new MapCounterNotLazy();
			case MAP_LAZY:
				return new MapCounterLazy();
			case MAP_GLOBAL:
				return new MapCounterGlobal();
			default:
				throw new IllegalArgumentException();
		}
	}
}

//		Benchmark                     (counterType)   Mode  Samples          Score          Error  Units
//		c.CountersBenchmark.rw               ATOMIC  thrpt       20   60337822.991 ±  1012934.194  ops/s
//		c.CountersBenchmark.rw:get           ATOMIC  thrpt       20    8102684.830 ±  1133715.435  ops/s
//		c.CountersBenchmark.rw:inc           ATOMIC  thrpt       20   52235138.161 ±   507255.524  ops/s
//		c.CountersBenchmark.rw              RAW_MAP  thrpt       20   68039686.746 ±  2440483.098  ops/s
//		c.CountersBenchmark.rw:get          RAW_MAP  thrpt       20    2188930.057 ±   332506.635  ops/s
//		c.CountersBenchmark.rw:inc          RAW_MAP  thrpt       20   65850756.689 ±  2587503.154  ops/s
//		c.CountersBenchmark.rw          TL_HASH_MAP  thrpt       20   72565571.421 ±  4075654.562  ops/s
//		c.CountersBenchmark.rw:get      TL_HASH_MAP  thrpt       20    2778826.175 ±   394362.999  ops/s
//		c.CountersBenchmark.rw:inc      TL_HASH_MAP  thrpt       20   69786745.246 ±  4074206.145  ops/s
//		c.CountersBenchmark.rw         MAP_NOT_LAZY  thrpt       20  108747463.387 ± 11451866.194  ops/s
//		c.CountersBenchmark.rw:get     MAP_NOT_LAZY  thrpt       20    3736771.920 ±  1337793.142  ops/s
//		c.CountersBenchmark.rw:inc     MAP_NOT_LAZY  thrpt       20  105010691.467 ± 10576902.026  ops/s
//		c.CountersBenchmark.rw             MAP_LAZY  thrpt       20  265020649.141 ± 17124389.503  ops/s
//		c.CountersBenchmark.rw:get         MAP_LAZY  thrpt       20    6265283.953 ±  1989604.693  ops/s
//		c.CountersBenchmark.rw:inc         MAP_LAZY  thrpt       20  258755365.188 ± 17393958.480  ops/s
//		c.CountersBenchmark.rw           MAP_GLOBAL  thrpt       20   42623643.238 ±   686737.228  ops/s
//		c.CountersBenchmark.rw:get       MAP_GLOBAL  thrpt       20    7093925.722 ±  1152562.896  ops/s
//		c.CountersBenchmark.rw:inc       MAP_GLOBAL  thrpt       20   35529717.516 ±   549368.070  ops/s
