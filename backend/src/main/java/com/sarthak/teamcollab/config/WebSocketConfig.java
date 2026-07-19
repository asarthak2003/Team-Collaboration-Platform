package com.sarthak.teamcollab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to send messages to clients on
        // destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        // Prefix used to filter messages targeting application-annotated methods
        // (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint that clients will use to connect to our WebSocket server
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // Allow cross-origin requests
                .withSockJS(); // Fallback options for browsers that don't support WebSockets
    }

    @Override
    public void configClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(new ChannelInterceptor()){
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel){
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if(accessor != null){
                    //authenticate the user during STOMP connection
                    if(StompCommand.CONNECT.equals(accessor.getCommand())){
                        String authHeader  = accessor.getFirstNativeHeader("Authorization");
                        if(authHeader  != null && authHeader .startsWith("Bearer ")){
                            String token = authHeader.substring(7);
                            if(jwtTokenProvider.validateToken(token)){
                                String email = jwtTokenProvider.getEmailFromToken(token);
                                String role = jwtTokenProvider.getRoleFromToken(token);
                                
                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken (email, null, Collections.singleton(new SimpleGrantedAuthority(role)));
                                accessor.setUser(authentication);
                            }
                        }
                    }
                    //prevent users from subscribing to channels that aren't their own
                    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
                        String destination = accessor.getDestination();
                        java.security.Principal principal = accessor.getUser();
                        if (destination != null && destination.startsWith("/topic/notifications/")){
                            String targetEmail = destination.substring("/topic/notifications/".length());
                            if (principal == null || !principal.getName().equalsIgnoreCase(targetEmail)){
                                throw new AccessDeniedException("Unauthorized subscription target");
                            }
                        }
                    }
                }
            }
        }
    }
}
