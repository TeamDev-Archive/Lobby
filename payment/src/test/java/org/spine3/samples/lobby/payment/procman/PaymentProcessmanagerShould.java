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

package org.spine3.samples.lobby.payment.procman;

import com.google.protobuf.Message;
import org.junit.Test;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Identifiers;
import org.spine3.money.Currency;
import org.spine3.money.Money;
import org.spine3.protobuf.AnyPacker;
import org.spine3.samples.lobby.payment.InitializeThirdPartyProcessorPayment;
import org.spine3.samples.lobby.payment.InstantiateThirdPartyProcessorPayment;
import org.spine3.samples.lobby.registration.contracts.OrderTotal;
import org.spine3.samples.lobby.registration.contracts.SeatOrderLine;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;
import org.spine3.server.command.CommandStore;
import org.spine3.server.procman.CommandRouted;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public class PaymentProcessmanagerShould {

    @Test
    public void instantiate_payment_aggregate() throws SecondInstantiationAttempt {
        final BoundedContext boundedContext = Given.boundedContext();
        final PaymentProcessManager procMan = Given.newProcMan(boundedContext);
        assertEquals(0, procMan.getVersion());
        final InstantiateThirdPartyProcessorPayment command = Given.instantiateCommand();

        final CommandRouted routedCommand = procMan.handle(command, CommandContext.getDefaultInstance());
        assertEquals(1, procMan.getVersion());
        final Collection<Command> commands = routedCommand.getProducedList();
        assertEquals(1, commands.size());

        final Command actualRoutedCommand = commands.iterator().next();
        final Message routedCommandMessage = AnyPacker.unpack(actualRoutedCommand.getMessage());
        assertTrue(routedCommandMessage instanceof InitializeThirdPartyProcessorPayment);
    }

    private static class Given {

        private static final PaymentProcessManagerRepository REPOSITORY = new PaymentProcessManagerRepository(boundedContext());
        private static final BoundedContext boundedContext = boundedContext();

        @SuppressWarnings({"NonThreadSafeLazyInitialization", "StaticVariableUsedBeforeInitialization"})
        private static BoundedContext boundedContext() {
            if (boundedContext == null) {
                final StorageFactory storageFactory = InMemoryStorageFactory.getInstance();
                final CommandStore commandStore = new CommandStore(storageFactory.createCommandStorage());
                final CommandBus commandBus = CommandBus.newInstance(commandStore);
                final BoundedContext bc = BoundedContext.newBuilder()
                                                        .setName("Payment-bc")
                                                        .setMultitenant(false)
                                                        .setStorageFactory(storageFactory)
                                                        .setCommandBus(commandBus)
                                                        .build();
                return bc;
            }

            return boundedContext;
        }

        private static PaymentProcessManager newProcMan(BoundedContext bc) {
            final PaymentProcessManagerId id = PaymentProcessManagerId.newBuilder()
                    .setValue(Identifiers.newUuid())
                    .build();
            // Create and pass procman through the whole initialization cycle
            final PaymentProcessManager initialized = REPOSITORY.load(id);
            initialized.setBoundedContext(bc);
            return initialized;
        }

        private static InstantiateThirdPartyProcessorPayment instantiateCommand() {
            final Money price = Money.newBuilder()
                                     .setAmount(1)
                                     .setCurrency(Currency.USD)
                                     .build();
            final SeatOrderLine orderLine = SeatOrderLine.newBuilder()
                                                         .setLineTotal(price)
                                                         .setQuantity(1)
                                                         .setUnitPrice(price)
                                                         .build();
            final OrderTotal total = OrderTotal.newBuilder()
                                               .setTotalPrice(price)
                                               .addOrderLine(orderLine)
                                               .build();
            final InstantiateThirdPartyProcessorPayment command
                    = InstantiateThirdPartyProcessorPayment.newBuilder()
                                                           .setTotal(total)
                                                           .build();
            return command;
        }
    }
}
