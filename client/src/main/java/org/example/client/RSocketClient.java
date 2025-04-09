package org.example.client;

import io.netty.buffer.ByteBufAllocator;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.TaggingMetadataCodec;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.RoutingMetadata;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class RSocketClient {

    private RSocket rSocket;

    public void connect() {
        rSocket = RSocketConnector.create()
                .metadataMimeType(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString())
                .connect(TcpClientTransport.create("localhost", 7000)) // порт сервера Spring RSocket
                .block();
        RSocketRequester requester = RSocketRequester.builder().tcp("localhost", 7000);
    }

    public void sendRequest(String route, String data) {
        // Создание metadata с маршрутом
        ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
        var metadata = CompositeMetadataCodec.encodeAndAddMetadata(
                allocator,
                allocator.buffer(),
                WellKnownMimeType.MESSAGE_RSOCKET_ROUTING,
                TaggingMetadataCodec.createTaggingContent(allocator, route)
        );

        DefaultPayload payload = DefaultPayload.create(data, metadata);

        // Отправить запрос и получить ответ
        rSocket.requestResponse(payload)
                .map(responsePayload -> responsePayload.getDataUtf8())
                .doOnNext(response -> System.out.println("Ответ от сервера: " + response))
                .doOnError(e -> System.err.println("Ошибка: " + e.getMessage()))
                .subscribe();
    }

    public void disconnect() {
        if (rSocket != null) {
            rSocket.dispose();
        }
    }
}