/**
 * Copyright 2015 Peti Koch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.petikoch.examples.performance;

import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

/**
 * Where is the code that burns CPU cycles?
 */
public class BurningCpuCyclesIssue {

	public static void main(String[] args) throws InterruptedException {
		System.out.println(ManagementFactory.getRuntimeMXBean().getName());

		int numberOfThreads = Runtime.getRuntime().availableProcessors() ;

		final AtomicBoolean shouldRun = new AtomicBoolean(true);

		final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		IntStream.rangeClosed(1, numberOfThreads).forEach(value -> {
			executorService.submit(() -> {
				while (shouldRun.get()) {
					int methodNumber = ThreadLocalRandom.current().nextInt(3);
					switch (methodNumber){
						case 0:
							method0();
							break;
						case 1:
							method1();
							break;
						case 2:
							method2();
							break;
						default:
							throw new IllegalStateException("Not implemented");
					}
				}
			});
		});

		TimeUnit.MINUTES.sleep(1);

		shouldRun.set(false);
		executorService.shutdown();
	}

	private static void method0() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void method1() {
		IntStream.rangeClosed(1, 1000).forEach(value -> {
			LockSupport.parkNanos(1);
			//https://github.com/LMAX-Exchange/disruptor/blob/master/src/main/java/com/lmax/disruptor/SleepingWaitStrategy.java
		});
	}

	private static void method2() {
		long stopTimeNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
		while(System.nanoTime() < stopTimeNanos){
		}
	}

}
