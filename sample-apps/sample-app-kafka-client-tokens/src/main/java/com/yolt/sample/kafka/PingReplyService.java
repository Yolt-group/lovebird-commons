package com.yolt.sample.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PingReplyService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "ping")
    public void consume(
            @Header(value = ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME) final ClientUserToken clientUserToken,
            @Payload MessageDto ping) throws Exception {

        log.info("Received {} for {}", ping.getContents(), clientUserToken.getUserIdClaim());

        MessageDto pong = new MessageDto("pong");

        Message<MessageDto> message = MessageBuilder
                /*
                 * The payload will be serialized to json. See
                 * YoltKafkaAutoConfiguration for details.
                 */
                .withPayload(pong)
                /*
                 * For simplicities' sake the reply topic has been hardcoded.
                 * For legacy reasons topics on the ycs namespace are prefixed
                 * with _ycs, so it would be prudent to use a property for this.
                 */
                .setHeader(KafkaHeaders.TOPIC, "pong")
                /*
                 * Partition user related messages by userId. This will ensure that
                 * consumers consume all messages for a particular user in order.
                 */
                .setHeader(KafkaHeaders.MESSAGE_KEY, clientUserToken.getUserIdClaim().toString())
                /*
                 * The client token must be propagated as a string. Always use
                 * ClientToken.getSerialized to create this string.
                 */
                .setHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientUserToken.getSerialized())
                .build();

        kafkaTemplate
                .send(message)
                /*
                 * Invoke .get to block until the message is produced.
                 */
                .get();
    }

}