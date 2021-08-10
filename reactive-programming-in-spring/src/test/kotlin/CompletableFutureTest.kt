import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.util.StopWatch
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CompletableFutureTest {
    private val sw = StopWatch()
    private val log = LoggerFactory.getLogger(this::class.java)
    private val cachedEs = Executors.newCachedThreadPool()
    private val singleEs = Executors.newSingleThreadExecutor()

    @Test
    fun `비동기 가격조회1`() {
        val future1 = getPriceAsync1()
        log.info("future called")
        future1.join()
        log.info("future end")

        val future2 = getPriceAsync2()
        log.info("future called")
        future2.join()
        log.info("future end")
    }

    @Test
    fun thenAccept() {
        val future = getPriceAsync2()
            .thenAccept { log.info("then accept: {}", it) }
        log.info("future called")
        log.info("future end: {}", future.join())
    }

    @Test
    fun thenApply() {
        val future = getPriceAsync2()
            .thenApply {
                log.info("then apply: {}", it)
                it + 1
            }
        log.info("future called")
        log.info("future end: {}", future.join())
    }

    @Test
    fun thenAcceptApplyAsync() {
        val future = getPriceAsync2()
            .thenApplyAsync {
                log.info("then apply: {}", it)
                it + 1
            }
            .thenAcceptAsync {
                log.info("then accept: {}", it)
            }
        log.info("future called")
        log.info("future end: {}", future.join())
    }

    @Test
    fun thenAcceptApplyAsyncWithExecutor() {
        val future = getPriceAsync2()
            .thenApplyAsync({
                log.info("then apply: {}", it)
                it + 1
            }, cachedEs)
            .thenAcceptAsync({
                log.info("then accept: {}", it)
            }, cachedEs)
        log.info("future called")
        log.info("future end: {}", future.join())
    }

    @Test
    fun thenCombine() {
        sw.start()
        val future1 = getPriceAsync2()
        val future2 = getPriceAsync2()

        val future = future1.thenCombine(future2) { f1, f2 -> f1 + f2 }
        log.info("future end: {}", future.join()) // 2초밖에 안걸림
        sw.stop()

        log.info("ms: {}", sw.totalTimeMillis)
    }

    @Test
    fun thenCombineSingleThread() {
        sw.start()
        val future1 = getPriceAsync2(singleEs)
        val future2 = getPriceAsync2(singleEs)

        val future = future1.thenCombine(future2) { f1, f2 -> f1 + f2 }
        log.info("future end: {}", future.join()) // 4초 걸림
        sw.stop()

        log.info("ms: {}", sw.totalTimeMillis)
    }

    @Test
    fun allOf() {
        sw.start()
        val future1 = getPriceAsync2()
        val future2 = getPriceAsync2()
        val future3 = getPriceAsync2()

        val future = CompletableFuture.allOf(future1, future2, future3)
            .thenApply {
                listOf(future1.join(), future2.join(), future3.join())
            }
        log.info("future end: {}", future.join().sum()) // 3개를 병렬로 실행했으므로 2초 걸림
        sw.stop()

        log.info("ms: {}", sw.totalTimeMillis)
    }

    fun getPrice(): Int {
        try {
            Thread.sleep(2000)
        } catch (_: Exception) { }
        return 10
    }

    fun getPriceAsync1(): CompletableFuture<Int> {
        val future = CompletableFuture<Int>()
        cachedEs.execute {
            log.info("getPriceAsync in")
            future.complete(getPrice())
        }
        return future
    }


    fun getPriceAsync2(es: ExecutorService = cachedEs): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync({
            log.info("getPriceAsync2 in")
            getPrice()
        }, es)
    }

}


