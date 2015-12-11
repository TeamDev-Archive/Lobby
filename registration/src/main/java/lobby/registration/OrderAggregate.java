package lobby.registration;

import lobby.contracts.common.OrderId;
import lobby.contracts.registration.order.OrderConfirmed;
import lobby.contracts.registration.order.OrderPlaced;
import lobby.registration.order.Order;
import lobby.registration.order.Order.Status;
import lobby.registration.order.RegisterToConference;
import lobby.registration.order.RejectOrder;
import lobby.registration.service.OrderPricingService;
import org.spine3.base.CommandContext;
import org.spine3.server.Assign;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.Apply;

import static com.google.common.base.Preconditions.checkState;
import static lobby.registration.order.Order.Status.*;

/**
 * The order aggregate root.
 *
 * @author Alexander Litus
 */
@SuppressWarnings("TypeMayBeWeakened") // not in handlers
public class OrderAggregate extends Aggregate<OrderId, Order> {

    private OrderPricingService orderPricingService;

    public OrderAggregate(OrderId id) {
        super(id);
    }

    public void setOrderPricingService(OrderPricingService orderPricingService) {
        this.orderPricingService = orderPricingService;
    }

    @Override
    protected Order getDefaultState() {
        return Order.getDefaultInstance();
    }

    @Assign
    public OrderPlaced handle(RegisterToConference command, CommandContext context) {
        validateCommand(command);
        final OrderPlaced result = OrderPlaced.newBuilder()
                .setOrderId(command.getOrderId())
                .setConferenceId(command.getConferenceId())
                .addAllSeat(command.getSeatList())
                .build();
        return result;
    }

    @Apply
    private void event(OrderPlaced event) {
        final Order newState = Order.newBuilder(getState())
                .setId(event.getOrderId())
                .setConferenceId(event.getConferenceId())
                .addAllSeat(event.getSeatList())
                .setStatus(CREATED)
                .build();
        validate(newState);
        incrementState(newState);
    }

    @Apply
    private void event(OrderConfirmed event) {
        final Order newState = Order.newBuilder(getState())
                .setId(event.getOrderId())
                .setStatus(CONFIRMED)
                .build();
        validate(newState);
        incrementState(newState);
    }

    private void validateCommand(RegisterToConference command) {
        if (command.getOrderId().getUuid().isEmpty()) {
            throw noOrderIdException();
        }
        if (command.getConferenceId().getUuid().isEmpty()) {
            throw new IllegalArgumentException("No conference ID in the order.");
        }
        if (command.getSeatCount() == 0) {
            throw new IllegalArgumentException("No seats in the order.");
        }
        final Status status = getState().getStatus();
        checkState(status == UNRECOGNIZED, status);
    }

    private void validateCommand(RejectOrder command) {
        if (command.getOrderId().getUuid().isEmpty()) {
            throw noOrderIdException();
        }
        final Status status = getState().getStatus();
        checkState(status == CREATED, status);
    }

    private static IllegalArgumentException noOrderIdException() {
        return new IllegalArgumentException("No order ID.");
    }
}
