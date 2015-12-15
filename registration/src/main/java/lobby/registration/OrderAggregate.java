package lobby.registration;

import lobby.contracts.common.OrderId;
import lobby.registration.order.Order;
import lobby.registration.service.OrderPricingService;
import org.spine3.server.Entity;
import org.spine3.server.aggregate.Aggregate;

/**
 * The order aggregate root.
 *
 * @author Alexander Litus
 */
@SuppressWarnings("TypeMayBeWeakened") // not in handlers
public class OrderAggregate extends Aggregate<OrderId, Order> {

    private OrderPricingService orderPricingService;

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     * @see Entity
     */
    public OrderAggregate(OrderId id) {
        super(id);
    }

    /**
     * Sets the pricing service to use to calculate a price of an order.
     *
     * @param orderPricingService the pricing service implementation
     */
    public void setOrderPricingService(OrderPricingService orderPricingService) {
        this.orderPricingService = orderPricingService;
    }

    @Override
    protected Order getDefaultState() {
        return Order.getDefaultInstance();
    }
}
