package nl.ing.lovebird.clienttokens.requester.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ClientTokenResponseDTO {
    private String clientToken;
    private long expiresIn;
}