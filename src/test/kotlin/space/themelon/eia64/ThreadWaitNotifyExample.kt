package space.themelon.eia64

import java.util.*

object ThreadWaitNotifyExample {
    private val list: MutableList<String> = LinkedList()

    @JvmStatic
    fun main(args: Array<String>) {
        val consumer = Thread(Consumer(), "Consumer Thread")
        val producer = Thread(Producer(), "Producer Thread")

        consumer.start()
        producer.start()
    }

    internal class Consumer : Runnable {
        override fun run() {
            while (true) {
                synchronized(list) {
                    while (list.isEmpty()) {
                        try {
                            println(Thread.currentThread().name + " is waiting for data...")
                            (list as Object).wait()
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            println("Thread interrupted")
                        }
                    }
                    val data = list.removeAt(0)
                    println(Thread.currentThread().name + " consumed: " + data)
                }
            }
        }
    }

    internal class Producer : Runnable {
        override fun run() {
            var counter = 0
            while (true) {
                synchronized(list) {
                    val data = "Data-" + counter++
                    list.add(data)
                    println(Thread.currentThread().name + " produced: " + data)
                    (list as Object).notify()
                }
                try {
                    Thread.sleep(1000) // Simulate time taken to produce data
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    println("Thread interrupted")
                }
            }
        }
    }
}
