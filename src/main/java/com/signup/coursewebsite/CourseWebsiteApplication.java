package com.signup.coursewebsite;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

@SpringBootApplication
@Controller
public class CourseWebsiteApplication {

    RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(CourseWebsiteApplication.class, args);
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "queue", defaultValue = "default") String queue,
                        @RequestParam(name = "user_id") Long userId,
                        HttpServletRequest request) {

        var cookies = request.getCookies();
        var cookieName = "user-queue-%s-token".formatted(queue);
        var token = "";
        if(cookies != null) {
            var cookie = Arrays.stream(cookies)
                    .filter(i -> i.getName().equalsIgnoreCase(cookieName))
                    .findFirst();
            token = cookie.orElse(new Cookie(cookieName, "")).getValue();
        }

        var uri = UriComponentsBuilder
                .fromUriString("http://127.0.0.1:9010")
                .path("/api/v1/queue/allowed")
                .queryParam("queue", queue)
                .queryParam("user_id", userId)
                .queryParam("token", token)
                .encode()
                .build()
                .toUri();

        ResponseEntity<AllowedUserRespose> response = restTemplate.getForEntity(uri, AllowedUserRespose.class);
        if(response.getBody() == null || !response.getBody().allowed()) {
            return "redirect:http://127.0.0.1:9010/waiting-room?user_id=%d&redirect_url=%s".formatted(
                    userId,"http://127.0.0.1:9000?user_id=%d".formatted(userId));
        }

        return "index";
    }

    public record AllowedUserRespose(Boolean allowed) {}
}
