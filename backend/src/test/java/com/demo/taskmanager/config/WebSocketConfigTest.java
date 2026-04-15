package com.demo.taskmanager.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.SockJsServiceRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    private WebSocketConfig webSocketConfig;

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;

    @Mock
    private SockJsServiceRegistration sockJsServiceRegistration;

    @BeforeEach
    void setUp() {
        webSocketConfig = new WebSocketConfig();
    }

    @Test
    void configureMessageBroker_ShouldConfigureBrokerAndPrefix() {
        // Given
        when(messageBrokerRegistry.enableSimpleBroker(any())).thenReturn(null);

        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry).enableSimpleBroker("/topic");
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/api");
    }

    @Test
    void registerStompEndpoints_ShouldAddEndpointWithSockJS() {
        // Given
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns("*")).thenReturn(endpointRegistration);
        when(endpointRegistration.withSockJS()).thenReturn(sockJsServiceRegistration);

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(stompEndpointRegistry).addEndpoint("/ws");
        verify(endpointRegistration).setAllowedOriginPatterns("*");
        verify(endpointRegistration).withSockJS();
    }

    @Test
    void registerStompEndpoints_ShouldAddEndpointWithCorrectPath() {
        // Given
        when(stompEndpointRegistry.addEndpoint("/ws")).thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns("*")).thenReturn(endpointRegistration);
        when(endpointRegistration.withSockJS()).thenReturn(sockJsServiceRegistration);

        // When
        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        // Then
        verify(stompEndpointRegistry).addEndpoint("/ws");
    }

    @Test
    void configureMessageBroker_ShouldSetCorrectApplicationDestinationPrefix() {
        // Given
        when(messageBrokerRegistry.enableSimpleBroker(any())).thenReturn(null);

        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/api");
    }

    @Test
    void configureMessageBroker_ShouldEnableSimpleBrokerWithCorrectTopic() {
        // Given
        when(messageBrokerRegistry.enableSimpleBroker("/topic")).thenReturn(null);

        // When
        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        // Then
        verify(messageBrokerRegistry).enableSimpleBroker("/topic");
    }
}
