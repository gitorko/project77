package com.demo.project77.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

public class Application {

    @SneakyThrows
    public static void main(String[] args) throws RuntimeException {

        NotifyListener notifyListener = new NotifyListener();
        notifyListener.registerObserver(new ShippedEventObserver());

        StateMachineContext stateMachine = StateMachineContext.builder()
                .state(new BeginState())
                .notifyListener(notifyListener)
                .build();
        stateMachine.sendEvent(ShoppingCartEvent.ADD_ITEM);
        if (stateMachine.getId() != ShoppingCartState.SHOPPING_STATE) throw new RuntimeException("ERROR");
        stateMachine.sendEvent(ShoppingCartEvent.ADD_ITEM);
        if (stateMachine.getId() != ShoppingCartState.SHOPPING_STATE) throw new RuntimeException("ERROR");
        stateMachine.sendEvent(ShoppingCartEvent.MAKE_PAYMENT);
        if (stateMachine.getId() != ShoppingCartState.PAYMENT_STATE) throw new RuntimeException("ERROR");
        stateMachine.sendEvent(ShoppingCartEvent.PAYMENT_FAIL);
        if (stateMachine.getId() != ShoppingCartState.SHOPPING_STATE) throw new RuntimeException("ERROR");
        stateMachine.sendEvent(ShoppingCartEvent.MAKE_PAYMENT);
        stateMachine.sendEvent(ShoppingCartEvent.PAYMENT_SUCESS);
        if (stateMachine.getId() != ShoppingCartState.SHIPPED_STATE) throw new RuntimeException("ERROR");

    }
}

@Data
@Builder
class StateMachineContext {
    State state;
    ShoppingCartState id;
    NotifyListener notifyListener;

    public void sendEvent(ShoppingCartEvent event) {
        state.nextState(this, event);
        notifyListener.notifyObservers(this.state.getClass().getSimpleName());
    }
}

enum ShoppingCartState {
    BEGIN_STATE,
    SHOPPING_STATE,
    PAYMENT_STATE,
    SHIPPED_STATE;
}

enum ShoppingCartEvent {
    ADD_ITEM,
    MAKE_PAYMENT,
    PAYMENT_SUCESS,
    PAYMENT_FAIL;
}


interface State {
    void nextState(StateMachineContext stateMachine, ShoppingCartEvent event);
}

@Data
class BeginState implements State {
    public ShoppingCartState id = ShoppingCartState.BEGIN_STATE;

    @Override
    public void nextState(StateMachineContext stateMachine, ShoppingCartEvent event) {
        switch (event) {
            case ADD_ITEM: {
                ShoppingState nextState = new ShoppingState();
                stateMachine.setState(nextState);
                stateMachine.setId(nextState.id);
                break;
            }
            default:
                throw new UnsupportedOperationException("Not Supported!");
        }
    }
}

@Data
class ShoppingState implements State {
    ShoppingCartState id = ShoppingCartState.SHOPPING_STATE;

    @Override
    public void nextState(StateMachineContext stateMachine, ShoppingCartEvent event) {
        switch (event) {
            case ADD_ITEM: {
                ShoppingState nextState = new ShoppingState();
                stateMachine.setState(nextState);
                stateMachine.setId(nextState.id);
                break;
            }
            case MAKE_PAYMENT: {
                PaymentState nextState = new PaymentState();
                stateMachine.setState(nextState);
                stateMachine.setId(nextState.id);
                break;
            }
            default:
                throw new UnsupportedOperationException("Not Supported!");
        }
    }
}

@Data
class PaymentState implements State {
    ShoppingCartState id = ShoppingCartState.PAYMENT_STATE;

    @Override
    public void nextState(StateMachineContext stateMachine, ShoppingCartEvent event) {
        switch (event) {
            case PAYMENT_SUCESS: {
                ShippedState nextState = new ShippedState();
                stateMachine.setState(nextState);
                stateMachine.setId(nextState.id);
                break;
            }
            case PAYMENT_FAIL:
                ShoppingState nextState = new ShoppingState();
                stateMachine.setState(nextState);
                stateMachine.setId(nextState.id);
                break;
            default:
                throw new UnsupportedOperationException("Not Supported!");
        }
    }
}

@Data
class ShippedState implements State {
    ShoppingCartState id = ShoppingCartState.SHIPPED_STATE;

    @Override
    public void nextState(StateMachineContext stateMachine, ShoppingCartEvent event) {
        throw new UnsupportedOperationException("Not Supported!");
    }
}

interface Observer {
    public void notify(String message);
}

class ShippedEventObserver implements Observer {
    @Override
    public void notify(String message) {
        if (message.startsWith("ShippedState")) {
            //This observer is interested only in shipped events.
            System.out.println("ShippedEventObserver got Message: " + message);
        }
    }
}

interface Subject {
    public void registerObserver(Observer observer);

    public void notifyObservers(String tick);
}

class NotifyListener implements Subject {
    List<Observer> notifyList = new ArrayList<>();

    @Override
    public void registerObserver(Observer observer) {
        notifyList.add(observer);
    }

    @Override
    public void notifyObservers(String message) {
        notifyList.forEach(e -> e.notify(message));
    }
}
