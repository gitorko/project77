package com.demo.project77;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

public class Application {
    public static void main(String[] args) {

        NotifyListener notifyListener = new NotifyListener();
        notifyListener.registerObserver(new ShippedEventObserver());

        State state = new BeginState(notifyListener);
        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.ADD_ITEM);
        System.out.println("After State: " + state.nextState(ShoppingCartEvent.ADD_ITEM));
        System.out.println();

        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.MAKE_PAYMENT);
        try {
            System.out.println("After State: " + state.nextState(ShoppingCartEvent.MAKE_PAYMENT));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println();

        state = new ShoppingState(notifyListener);
        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.ADD_ITEM);
        System.out.println("After State: " + state.nextState(ShoppingCartEvent.ADD_ITEM));
        System.out.println();
        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.MAKE_PAYMENT);
        System.out.println("After State: " + state.nextState(ShoppingCartEvent.MAKE_PAYMENT));
        System.out.println();

        state = new PaymentState(notifyListener);
        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.PAYMENT_SUCESS);
        System.out.println("After State: " + state.nextState(ShoppingCartEvent.PAYMENT_SUCESS));
        System.out.println();
        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.PAYMENT_FAIL);
        System.out.println("After State: " + state.nextState(ShoppingCartEvent.PAYMENT_FAIL));
        System.out.println();

        state = new ShippedState(notifyListener);
        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.DISPATCHED);
        System.out.println("After State: " + state.nextState(ShoppingCartEvent.DISPATCHED));
        System.out.println();

        state = new CompletedState(notifyListener);
        System.out.println("Current State: " + state);
        System.out.println("Event: " + ShoppingCartEvent.MAKE_PAYMENT);
        try {
            System.out.println("After State: " + state.nextState(ShoppingCartEvent.MAKE_PAYMENT));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println();
    }
}

enum ShoppingCartEvent {
    ADD_ITEM,
    MAKE_PAYMENT,
    PAYMENT_SUCESS,
    PAYMENT_FAIL,
    DISPATCHED
}

enum ShoppingCart {
    BEGIN_STATE,
    SHOPPING_STATE,
    PAYMENT_STATE,
    SHIPPED_STATE,
    COMPLETED_STATE;
}

interface State {
    ShoppingCart nextState(ShoppingCartEvent event);
}

@AllArgsConstructor
@Data
class BeginState implements State {
    NotifyListener notifyListener;
    final ShoppingCart currentState = ShoppingCart.BEGIN_STATE;

    @Override
    public ShoppingCart nextState(ShoppingCartEvent event) {
        switch (event) {
        case ADD_ITEM:
            return ShoppingCart.SHOPPING_STATE;
        default:
            throw new UnsupportedOperationException("Not Supported!");
        }
    }
}

@AllArgsConstructor
@Data
class ShoppingState implements State {
    NotifyListener notifyListener;
    final ShoppingCart currentState = ShoppingCart.SHOPPING_STATE;

    @Override
    public ShoppingCart nextState(ShoppingCartEvent event) {
        switch (event) {
        case ADD_ITEM:
            return ShoppingCart.SHOPPING_STATE;
        case MAKE_PAYMENT:
            return ShoppingCart.PAYMENT_STATE;
        default:
            throw new UnsupportedOperationException("Not Supported!");
        }
    }
}

@AllArgsConstructor
@Data
class PaymentState implements State {
    NotifyListener notifyListener;
    final ShoppingCart currentState = ShoppingCart.PAYMENT_STATE;

    @Override
    public ShoppingCart nextState(ShoppingCartEvent event) {
        switch (event) {
        case PAYMENT_SUCESS:
            notifyListener.notifyObservers("SHIPPED_STATE: Shipped product after purchase!");
            return ShoppingCart.SHIPPED_STATE;
        case PAYMENT_FAIL:
            return ShoppingCart.SHOPPING_STATE;
        default:
            throw new UnsupportedOperationException("Not Supported!");
        }
    }
}

@AllArgsConstructor
@Data
class ShippedState implements State {
    NotifyListener notifyListener;
    final ShoppingCart currentState = ShoppingCart.SHIPPED_STATE;

    @Override
    public ShoppingCart nextState(ShoppingCartEvent event) {
        switch (event) {
        case DISPATCHED:
            return ShoppingCart.COMPLETED_STATE;
        default:
            throw new UnsupportedOperationException("Not Supported!");
        }
    }
}

@AllArgsConstructor
@Data
class CompletedState implements State {
    NotifyListener notifyListener;
    final ShoppingCart currentState = ShoppingCart.COMPLETED_STATE;

    @Override
    public ShoppingCart nextState(ShoppingCartEvent event) {
        throw new UnsupportedOperationException("Not Supported!");
    }
}

interface Observer {
    public void notify(String message);
}

class ShippedEventObserver implements Observer {
    @Override
    public void notify(String message) {
        if (message.startsWith(ShoppingCart.SHIPPED_STATE.toString())) {
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
