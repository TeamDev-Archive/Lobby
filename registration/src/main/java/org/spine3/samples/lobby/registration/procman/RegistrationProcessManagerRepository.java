/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.samples.lobby.registration.procman;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Message;
import org.spine3.base.CommandContext;
import org.spine3.base.EventContext;
import org.spine3.samples.lobby.common.OrderId;
import org.spine3.samples.lobby.common.ReservationId;
import org.spine3.samples.lobby.payment.contracts.PaymentCompleted;
import org.spine3.samples.lobby.registration.contracts.OrderConfirmed;
import org.spine3.samples.lobby.registration.contracts.OrderPlaced;
import org.spine3.samples.lobby.registration.contracts.OrderUpdated;
import org.spine3.samples.lobby.registration.seat.availability.SeatsReserved;
import org.spine3.server.BoundedContext;
import org.spine3.server.entity.GetIdByFieldIndex;
import org.spine3.server.entity.IdFunction;
import org.spine3.server.procman.ProcessManagerRepository;
import org.spine3.type.CommandClass;
import org.spine3.type.EventClass;

import java.util.Map;

/**
 * The repository managing {@link RegistrationProcessManager}s.
 *
 * @author Alexander Litus
 */
public class RegistrationProcessManagerRepository
        extends ProcessManagerRepository<ProcessManagerId, RegistrationProcessManager, RegistrationProcess> {

    private final Map<EventClass, IdFunction<ProcessManagerId, ? extends Message, EventContext>> idFromEventFunctions =
            ImmutableMap.<EventClass, IdFunction<ProcessManagerId, ? extends Message, EventContext>>builder()
            .put(EventClass.of(OrderPlaced.class), new GetIdFromEventOrderPlaced())
            .put(EventClass.of(OrderUpdated.class), new GetIdFromEventOrderUpdated())
            .put(EventClass.of(SeatsReserved.class), new GetIdFromEventSeatsReserved())
            .put(EventClass.of(OrderConfirmed.class), new GetIdFromEventOrderConfirmed())
            .put(EventClass.of(PaymentCompleted.class), new GetIdFromEventPaymentCompleted())
            .build();

    private final Map<CommandClass, IdFunction<ProcessManagerId, ? extends Message, CommandContext>> idFromCommandFunctions =
            ImmutableMap.<CommandClass, IdFunction<ProcessManagerId, ? extends Message, CommandContext>>builder()
            .put(
                CommandClass.of(ExpireRegistrationProcess.class),
                new GetIdByFieldIndex<ProcessManagerId, ExpireRegistrationProcess, CommandContext>(0)
            ).build();

    /**
     * Creates a new repository instance.
     *
     * @param boundedContext the bounded context to which this repository belongs
     */
    protected RegistrationProcessManagerRepository(BoundedContext boundedContext) {
        super(boundedContext);
    }

    @Override
    @SuppressWarnings("RefusedBequest") // the default implementation returns null
    public IdFunction<ProcessManagerId, ? extends Message, EventContext> getIdFunction(EventClass eventClass) {
        final IdFunction<ProcessManagerId, ? extends Message, EventContext> func = idFromEventFunctions.get(eventClass);
        return func;
    }

    @Override
    @SuppressWarnings("RefusedBequest") // the default implementation returns null
    public IdFunction<ProcessManagerId, ? extends Message, CommandContext> getIdFunction(CommandClass commandClass) {
        final IdFunction<ProcessManagerId, ? extends Message, CommandContext> func = idFromCommandFunctions.get(commandClass);
        return func;
    }

    private static class GetIdFromEventOrderPlaced implements IdFunction<ProcessManagerId, OrderPlaced, EventContext> {

        @Override
        public ProcessManagerId getId(OrderPlaced message, EventContext context) {
            final OrderId orderId = message.getOrderId();
            final ProcessManagerId result = IdConverter.toProcessManagerId(orderId);
            return result;
        }
    }

    private static class GetIdFromEventOrderUpdated implements IdFunction<ProcessManagerId, OrderUpdated, EventContext> {

        @Override
        public ProcessManagerId getId(OrderUpdated message, EventContext context) {
            final OrderId orderId = message.getOrderId();
            final ProcessManagerId result = IdConverter.toProcessManagerId(orderId);
            return result;
        }
    }

    private static class GetIdFromEventSeatsReserved implements IdFunction<ProcessManagerId, SeatsReserved, EventContext> {

        @Override
        public ProcessManagerId getId(SeatsReserved message, EventContext context) {
            final ReservationId reservationId = message.getReservationId();
            final ProcessManagerId result = IdConverter.toProcessManagerId(reservationId);
            return result;
        }
    }

    private static class GetIdFromEventOrderConfirmed implements IdFunction<ProcessManagerId, OrderConfirmed, EventContext> {

        @Override
        public ProcessManagerId getId(OrderConfirmed message, EventContext context) {
            final OrderId orderId = message.getOrderId();
            final ProcessManagerId result = IdConverter.toProcessManagerId(orderId);
            return result;
        }
    }

    private static class GetIdFromEventPaymentCompleted implements IdFunction<ProcessManagerId, PaymentCompleted, EventContext> {

        @Override
        public ProcessManagerId getId(PaymentCompleted message, EventContext context) {
            final OrderId orderId = message.getOrderId();
            final ProcessManagerId result = IdConverter.toProcessManagerId(orderId);
            return result;
        }
    }

    private static class IdConverter {

        /**
         * The prefix used to get a process manager UUID by prepending it to an order UUID.
         */
        private static final String PROC_MAN_ID_PREFIX = "registration-pm-";

        private static ProcessManagerId toProcessManagerId(OrderId orderId) {
            final String uuid = orderId.getUuid();
            return toProcessManagerId(uuid);
        }

        private static ProcessManagerId toProcessManagerId(ReservationId reservationId) {
            final String uuid = reservationId.getUuid();
            // as ReservationId equals to OrderId, it is possible to use the same method as for OrderId
            return toProcessManagerId(uuid);
        }

        private static ProcessManagerId toProcessManagerId(String uuid) {
            final String processManagerId = PROC_MAN_ID_PREFIX + uuid;
            return ProcessManagerId.newBuilder().setUuid(processManagerId).build();
        }
    }
}
