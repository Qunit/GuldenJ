/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guldenj.protocols.channels;

import org.guldenj.core.Coin;
import org.guldenj.core.Sha256Hash;
import org.guldenj.net.ProtobufConnection;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import org.bitcoin.paymentchannel.Protos;

import javax.annotation.Nullable;

/**
* A connection-specific event handler that handles events generated by client connections on a
 * {@link PaymentChannelServerListener}
*/
public abstract class ServerConnectionEventHandler {
    private ProtobufConnection<Protos.TwoWayChannelMessage> connectionChannel;
    // Called by ServerListener before channelOpen to set connectionChannel when it is ready to received application messages
    // Also called with null to clear connectionChannel after channelClosed()
    synchronized void setConnectionChannel(@Nullable ProtobufConnection<Protos.TwoWayChannelMessage> connectionChannel) { this.connectionChannel = connectionChannel; }

    /**
     * <p>Closes the channel with the client (will generate a
     * {@link ServerConnectionEventHandler#channelClosed(PaymentChannelCloseException.CloseReason)} event)</p>
     *
     * <p>Note that this does <i>NOT</i> actually broadcast the most recent payment transaction, which will be triggered
     * automatically when the channel times out by the {@link StoredPaymentChannelServerStates}, or manually by calling
     * {@link StoredPaymentChannelServerStates#closeChannel(StoredServerChannel)} with the channel returned by
     * {@link StoredPaymentChannelServerStates#getChannel(org.guldenj.core.Sha256Hash)} with the id provided in
     * {@link ServerConnectionEventHandler#channelOpen(org.guldenj.core.Sha256Hash)}</p>
     */
    @SuppressWarnings("unchecked")
    // The warning 'unchecked call to write(MessageType)' being suppressed here comes from the build()
    // formally returning MessageLite-derived class that cannot be statically guaranteed to be the same MessageType
    // that is used in connectionChannel.
    protected final synchronized void closeChannel() {
        if (connectionChannel == null)
            throw new IllegalStateException("Channel is not fully initialized/has already been closed");
        connectionChannel.write(Protos.TwoWayChannelMessage.newBuilder()
                .setType(Protos.TwoWayChannelMessage.MessageType.CLOSE)
                .build());
        connectionChannel.closeConnection();
    }

    /**
     * Triggered when the channel is opened and application messages/payments can begin
     *
     * @param channelId A unique identifier which represents this channel (actually the hash of the multisig contract)
     */
    public abstract void channelOpen(Sha256Hash channelId);

    /**
     * Called when the payment in this channel was successfully incremented by the client
     *
     * @param by The increase in total payment
     * @param to The new total payment to us (not including fees which may be required to claim the payment)
     * @param info Information about this payment increase, used to extend this protocol.
     * @return acknowledgment information to be sent to the client.
     */
    @Nullable
    public abstract ListenableFuture<ByteString> paymentIncrease(Coin by, Coin to, ByteString info);

    /**
     * <p>Called when the channel was closed for some reason. May be called without a call to
     * {@link ServerConnectionEventHandler#channelOpen(Sha256Hash)}.</p>
     *
     * <p>Note that the same channel can be reopened at any point before it expires if the client reconnects and
     * requests it.</p>
     */
    public abstract void channelClosed(PaymentChannelCloseException.CloseReason reason);
}
