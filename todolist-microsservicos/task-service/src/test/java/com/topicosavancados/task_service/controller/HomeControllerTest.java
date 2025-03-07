package com.topicosavancados.task_service.controller;

import com.topicosavancados.task_service.model.Task;
import com.topicosavancados.task_service.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    // Mocks para *teste da lógica principal (getUserInfo, getTodayTasks)*
    // e para o *teste adicional* do fetchQuoteFromFavQs()
    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private TaskService taskService;

    // Mocks específicos para o WebClient chain
    @Mock
    private WebClient webClient; // result de webClientBuilder.build()

    // Em vez de usar generics complicados, usamos tipos raw:
    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec requestUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    // Controller principal usado nos testes de getUserInfo() e getTodayTasks()
    private HomeController homeControllerSpy;

    // Controller sem "spy" que testará diretamente o método fetchQuoteFromFavQs()
    private HomeController homeControllerReal;

    @BeforeEach
    void setUp() {
        // Toda vez que chamamos baseUrl(...), retorna o próprio builder mock
        when(webClientBuilder.baseUrl("https://favqs.com/api")).thenReturn(webClientBuilder);

        // Quando chamamos build(), retorna webClient (mock)
        when(webClientBuilder.build()).thenReturn(webClient);

        // Controller real
        HomeController realController = new HomeController(webClientBuilder, taskService);
        homeControllerSpy = spy(realController);

        // **Outra instância** real para testar fetchQuoteFromFavQs() sem spy
        // (poderíamos usar a mesma, mas aqui separamos para clareza).
        homeControllerReal = new HomeController(webClientBuilder, taskService);
    }

    @Test
    void getUserInfo_WithAuthenticationAdmin() {
        doReturn("Testing is awesome!").when(homeControllerSpy).fetchQuoteFromFavQs();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("adminUser");
        when(auth.getAuthorities()).thenAnswer(inv ->
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // Chamamos o método real (que chamará o spy no fetchQuoteFromFavQs)
        Map<String, Object> result = homeControllerSpy.getUserInfo(auth);

        assertNotNull(result);
        assertEquals("adminUser", result.get("username"));
        assertEquals("Testing is awesome!", result.get("idea"));
        assertEquals("ADMIN", result.get("role"));
    }

    @Test
    void getUserInfo_WithoutAuthentication_ThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                homeControllerSpy.getUserInfo(null)
        );
    }

    @Test
    void getTodayTasks_WithAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("someUser");

        Task task1 = new Task(); task1.setTitle("Task 1");
        Task task2 = new Task(); task2.setTitle("Task 2");
        List<Task> mockTasks = List.of(task1, task2);

        when(taskService.getTasksByDueDate("someUser", LocalDate.now())).thenReturn(mockTasks);

        List<Task> result = homeControllerSpy.getTodayTasks(auth);
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getTitle());
        assertEquals("Task 2", result.get(1).getTitle());
    }

    @Test
    void testFetchQuoteFromFavQs_Success() {
        // Monta a cadeia de mocks do WebClient:
        when(webClient.get()).thenReturn(requestUriSpec);
        when(requestUriSpec.uri("/qotd")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Precisamos retornar um Mono<FavQuoteResponse> real para
        // permitir .map(...).block() sem complicações.
        HomeController.FavQuoteResponse mockResponse = new HomeController.FavQuoteResponse();
        HomeController.FavQuoteResponse.Quote quoteObj = new HomeController.FavQuoteResponse.Quote();
        quoteObj.setBody("Here is a nice quote.");
        mockResponse.setQuote(quoteObj);

        // Retornamos um Mono.just(...) ao bodyToMono(...):
        when(responseSpec.bodyToMono(HomeController.FavQuoteResponse.class))
                .thenReturn(Mono.just(mockResponse));

        // Chama o método real
        String result = homeControllerReal.fetchQuoteFromFavQs();

        // Verifica se obtemos a citação do objeto
        assertEquals("Here is a nice quote.", result);
    }

    @Test
    void testFetchQuoteFromFavQs_Exception() {
        // Simular que ao chamar webClient.get() (ou em qualquer passo) vai lançar
        // uma RuntimeException. Assim, cai no catch e retorna "Failed to load..."
        when(webClient.get()).thenThrow(new RuntimeException("Simulated error"));

        String result = homeControllerReal.fetchQuoteFromFavQs();
        assertEquals("Failed to load ideas. Please try again later.", result);
    }

}
