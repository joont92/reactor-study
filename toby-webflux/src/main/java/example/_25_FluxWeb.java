package example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@SpringBootApplication
@RestController
public class _25_FluxWeb {
    public static void main(String[] args) {
        SpringApplication.run(_25_FluxWeb.class, args);
    }

    @GetMapping("/events1")
    public Mono<List<Event>> events1() {
        // list element에 flux 함수들을 적용할 수 없음
        return Mono.just(Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2")));
    }

    @GetMapping("/events2")
    public Flux<Event> events2() {
        // map등 flux 함수 적용 가능
        return Flux.fromIterable(Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2")));
    }

    @GetMapping(value = "/events3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events3() {
        return Flux.fromIterable(Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2")));
    }

    @GetMapping(value = "/events4", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events4() {
        return Flux.fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), "value")))
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @GetMapping(value = "/events5", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events5() {
        return Flux
                .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @GetMapping(value = "/events6", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events6() {
        return Flux
                .<Event, Long>generate(() -> 1L, (id, sink) -> {
                    sink.next(new Event(id, "value"));
                    return id + 1;
                })
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @GetMapping(value = "/events7", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> events7() {
        var generate = Flux.<String>generate(sink -> sink.next("value"));
        var interval = Flux.interval(Duration.ofSeconds(1));

        // 2개의 flux를 합친다(generate 1개 생성 후 interval 1초 지나야 다음 flux로 넘어감)
        // e.g. 회원정보 얻어오는 flux, 회원 스코어 얻어오는 flux를 만들어 zip으로 묶은 뒤, 회원정보 + 스코어 객체를 만들어 내려줄 수 있다
        return Flux.zip(generate, interval)
                .map(tu -> new Event(tu.getT2(), tu.getT1()))
                .take(10);
    }

    @Data @AllArgsConstructor
    static class Event {
        private Long id;
        private String value;
    }
}
