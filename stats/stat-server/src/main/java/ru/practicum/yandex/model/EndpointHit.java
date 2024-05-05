package ru.practicum.yandex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Представляет сущность хита эндпоинта, отслеживающую доступ к различным эндпоинтам в приложении.
 * Каждый хит записывает приложение, конкретный URI, IP-адрес запросившего пользователя и временную метку доступа.
 */
@Entity
@Table(name = "endpointhits")
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class EndpointHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String app;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false)
    private String ip;

    @Column(name = "created", nullable = false)
    private LocalDateTime timestamp;
}
