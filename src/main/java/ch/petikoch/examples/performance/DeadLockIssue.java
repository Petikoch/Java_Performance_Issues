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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A typical Deadlock situation
 */
public class DeadlockIssue {

	public static void main(String[] args) {
		System.out.println(ManagementFactory.getRuntimeMXBean().getName());

		final Object lock1 = new Object();
		final Object lock2 = new Object();

		CountDownLatch thread1HasLock1 = new CountDownLatch(1);
		CountDownLatch thread2HasLock2 = new CountDownLatch(1);

		new Thread(() -> {
			sysout("Starting");
			try {
				thread2HasLock2.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (lock1){
				sysout("Got lock1");
				thread1HasLock1.countDown();
				synchronized (lock2){
					sysout("Got lock2");
					try {
						TimeUnit.MINUTES.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "Thread-1").start();

		new Thread(() -> {
			sysout("Starting");
			synchronized (lock2){
				sysout("Got lock2");
				thread2HasLock2.countDown();
				synchronized (lock1){
					sysout("Got lock1");
					try {
						TimeUnit.MINUTES.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "Thread-2").start();
	}

	private static void sysout(String msg){
		System.out.println("[" + Thread.currentThread().getName() + "]: " + msg);
	}
}
