package com.demo.project77.spring;

import java.util.EnumSet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner testStateMachine(StateMachineFactory<ShoppingCartState, ShoppingCartEvent> stateMachineFactory) {
        return args -> {
            StateMachine<ShoppingCartState, ShoppingCartEvent> stateMachine = stateMachineFactory.getStateMachine(
                    "mymachine");
            stateMachine.sendEvent(getEventMessage(ShoppingCartEvent.ADD_ITEM)).subscribe();
            if (!(stateMachine.getState().getId().equals(ShoppingCartState.SHOPPING_STATE)))
                throw new RuntimeException("ERROR");
            stateMachine.sendEvent(getEventMessage(ShoppingCartEvent.ADD_ITEM)).subscribe();
            if (!(stateMachine.getState().getId().equals(ShoppingCartState.SHOPPING_STATE)))
                throw new RuntimeException("ERROR");
            stateMachine.sendEvent(getEventMessage(ShoppingCartEvent.MAKE_PAYMENT)).subscribe();
            if (!(stateMachine.getState().getId().equals(ShoppingCartState.PAYMENT_STATE)))
                throw new RuntimeException("ERROR");
            stateMachine.sendEvent(getEventMessage(ShoppingCartEvent.PAYMENT_FAIL)).subscribe();
            if (!(stateMachine.getState().getId().equals(ShoppingCartState.SHOPPING_STATE)))
                throw new RuntimeException("ERROR");
            stateMachine.sendEvent(getEventMessage(ShoppingCartEvent.MAKE_PAYMENT)).subscribe();
            if (!(stateMachine.getState().getId().equals(ShoppingCartState.PAYMENT_STATE)))
                throw new RuntimeException("ERROR");
            stateMachine.sendEvent(getEventMessage(ShoppingCartEvent.PAYMENT_SUCESS)).subscribe();
            if (!(stateMachine.getState().getId().equals(ShoppingCartState.SHIPPED_STATE)))
                throw new RuntimeException("ERROR");
            log.info("Final State: {}", stateMachine.getState().getId());
        };
    }

    private Mono<Message<ShoppingCartEvent>> getEventMessage(ShoppingCartEvent event) {
        return Mono.just(MessageBuilder.withPayload(event).build());
    }
}

enum ShoppingCartEvent {
    ADD_ITEM,
    MAKE_PAYMENT,
    PAYMENT_SUCESS,
    PAYMENT_FAIL
}

enum ShoppingCartState {
    BEGIN_STATE,
    SHOPPING_STATE,
    PAYMENT_STATE,
    SHIPPED_STATE;
}

@Configuration
@EnableStateMachineFactory
@Slf4j
class ShoppingStateMachineConfig extends EnumStateMachineConfigurerAdapter<ShoppingCartState, ShoppingCartEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<ShoppingCartState, ShoppingCartEvent> states) throws Exception {
        states
                .withStates()
                .initial(ShoppingCartState.BEGIN_STATE)
                .end(ShoppingCartState.SHIPPED_STATE)
                .states(EnumSet.allOf(ShoppingCartState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ShoppingCartState, ShoppingCartEvent> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(ShoppingCartState.BEGIN_STATE)
                .target(ShoppingCartState.SHOPPING_STATE)
                .event(ShoppingCartEvent.ADD_ITEM)
                .action(initAction())
                .and()
                .withExternal()
                .source(ShoppingCartState.SHOPPING_STATE)
                .target(ShoppingCartState.SHOPPING_STATE)
                .event(ShoppingCartEvent.ADD_ITEM)
                .and()
                .withExternal()
                .source(ShoppingCartState.SHOPPING_STATE)
                .target(ShoppingCartState.PAYMENT_STATE)
                .event(ShoppingCartEvent.MAKE_PAYMENT)
                .and()
                .withExternal()
                .source(ShoppingCartState.PAYMENT_STATE)
                .target(ShoppingCartState.SHIPPED_STATE)
                .event(ShoppingCartEvent.PAYMENT_SUCESS)
                .and()
                .withExternal()
                .source(ShoppingCartState.PAYMENT_STATE)
                .target(ShoppingCartState.SHOPPING_STATE)
                .event(ShoppingCartEvent.PAYMENT_FAIL);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<ShoppingCartState, ShoppingCartEvent> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(new GlobalStateMachineListener());
    }

    @Bean
    public Action<ShoppingCartState, ShoppingCartEvent> initAction() {
        log.info("init action called!");
        return ctx -> log.info("Id: {}", ctx.getTarget().getId());
    }
}

@Slf4j
class GlobalStateMachineListener extends StateMachineListenerAdapter<ShoppingCartState, ShoppingCartEvent> {
    @Override
    public void stateChanged(State<ShoppingCartState, ShoppingCartEvent> from, State<ShoppingCartState, ShoppingCartEvent> to) {
        log.info("State changed to : {}", to.getId());
    }
}
