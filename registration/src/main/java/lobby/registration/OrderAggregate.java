package lobby.registration;

import lobby.registration.command.AcceptOrder;
import lobby.registration.command.MakeOrder;
import lobby.registration.command.RejectOrder;
import lobby.registration.event.OrderAccepted;
import lobby.registration.event.OrderCreated;
import lobby.registration.event.OrderRejected;
import org.spine3.base.CommandContext;
import org.spine3.server.Assign;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.Apply;

import static lobby.registration.Order.Status.*;

/**
 * The order aggregate root.
 *
 * @author Alexander Litus
 */
@SuppressWarnings("TypeMayBeWeakened") // not in handlers
public class OrderAggregate extends Aggregate<OrderId, Order> {

    protected OrderAggregate(OrderId id) {
        super(id);
    }

    @Override
    protected Order getDefaultState() {
        return Order.getDefaultInstance();
    }

    @Assign
    public OrderCreated handle(MakeOrder command, CommandContext context) {
        validateCommand(command);
        final OrderCreated result = OrderCreated.newBuilder()
                .setOrderId(command.getOrderId())
                .build();
        return result;
    }

    @Apply
    private void event(OrderCreated event) {
        final Order newState = Order.newBuilder(getState())
                .setId(event.getOrderId())
                .setStatus(CREATED)
                .build();
        validate(newState);
        incrementState(newState);
    }

    @Assign
    public OrderAccepted handle(AcceptOrder command, CommandContext context) {
        validateCommand(command);
        final OrderAccepted result = OrderAccepted.newBuilder()
                .setOrderId(command.getOrderId())
                .build();
        return result;
    }

    @Apply
    private void event(OrderAccepted event) {
        final Order newState = Order.newBuilder(getState())
                .setId(event.getOrderId())
                .setStatus(ACCEPTED)
                .build();
        validate(newState);
        incrementState(newState);
    }

    @Assign
    public OrderRejected handle(RejectOrder command, CommandContext context) {
        validateCommand(command);
        final OrderRejected result = OrderRejected.newBuilder()
                .setOrderId(command.getOrderId())
                .build();
        return result;
    }

    @Apply
    private void event(OrderRejected event) {
        final Order newState = Order.newBuilder(getState())
                .setId(event.getOrderId())
                .setStatus(REJECTED)
                .build();
        validate(newState);
        incrementState(newState);
    }

    private static void validateCommand(MakeOrder command) {
        if (command.getOrderId().getUuid().isEmpty()) {
            throw noOrderIdException();
        }
        if (command.getConferenceId().getUuid().isEmpty()) {
            throw new IllegalArgumentException("No conference ID in the order.");
        }
        if (command.getSeatCount() == 0) {
            throw new IllegalArgumentException("No seats in the order.");
        }
    }

    private void validateCommand(AcceptOrder command) {
        if (command.getOrderId().getUuid().isEmpty()) {
            throw noOrderIdException();
        }
        if (getState().getStatus() != CREATED) {
            throw invalidOrderStatusException();
        }
    }

    private void validateCommand(RejectOrder command) {
        if (command.getOrderId().getUuid().isEmpty()) {
            throw noOrderIdException();
        }
        if (getState().getStatus() != CREATED) {
            throw invalidOrderStatusException();
        }
    }

    private static IllegalArgumentException noOrderIdException() {
        return new IllegalArgumentException("No order ID.");
    }

    private IllegalStateException invalidOrderStatusException() {
        return new IllegalStateException("Invalid order status: " + getState().getStatus());
    }
}
