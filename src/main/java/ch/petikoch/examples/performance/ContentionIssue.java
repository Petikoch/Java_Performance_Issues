/**
 * Copyright 2015 Peti Koch
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ch.petikoch.examples.performance;

import java.lang.management.ManagementFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * In this example, we have a lot of contention arround the single {@link BlockingQueue} instance plus some small
 * livelock issues.
 * <p>
 * Reason: Too many threads fighting for a single resource which uses lock-based access.
 */
public class ContentionIssue {

	public static void main(String[] args) throws InterruptedException {
		System.out.println(ManagementFactory.getRuntimeMXBean().getName());

		int numberOfThreads = Runtime.getRuntime().availableProcessors() * 2;
		if(numberOfThreads < 4){
			numberOfThreads = 4;
		}
		int queueSize = numberOfThreads / 2;

		BlockingQueue<String> contentionPoint = new LinkedBlockingQueue<>(queueSize);
		IntStream.rangeClosed(1, queueSize).forEach(value -> contentionPoint.add(Integer.toString(value)));

		final AtomicBoolean shouldRun = new AtomicBoolean(true);

		final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		IntStream.rangeClosed(1, numberOfThreads).forEach(value -> {
			executorService.submit(() -> {
				while (shouldRun.get()) {
					try {
						String oneOfTheElements = contentionPoint.poll();
						if (oneOfTheElements != null) {
							contentionPoint.put(oneOfTheElements);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		});

		TimeUnit.MINUTES.sleep(1);

		shouldRun.set(false);
		executorService.shutdown();
	}
}