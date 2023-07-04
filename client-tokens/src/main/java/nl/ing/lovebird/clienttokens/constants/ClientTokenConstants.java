package nl.ing.lovebird.clienttokens.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientTokenConstants {
    public static final String JWT_REGEX = "^[a-zA-Z0-9\\-_]*\\.[a-zA-Z0-9\\-_]*\\.[a-zA-Z0-9\\-_]*$";
    public static final String EXTRA_CLAIM_ISSUED_FOR = "isf";
    public static final String EXTRA_CLAIM_CLIENT_ID = "client-id";
    public static final String EXTRA_CLAIM_CLIENT_NAME = "client-name";
    public static final String EXTRA_CLAIM_CLIENT_USER_ID = "client-user-id";
    public static final String EXTRA_CLAIM_USER_ID = "user-id";
    public static final String SUBJECT = "sub";
    public static final String EXTRA_CLAIM_CLIENT_GROUP_ID = "client-group-id";
    public static final String EXTRA_CLAIM_CLIENT_GROUP_NAME = "client-group-name";
    public static final String CLIENT_TOKEN_HEADER_NAME = "client-token";
    public static final String CLAIM_CLIENT_USERS_KYC_PRIVATE_INDIVIDUALS = "client-users-kyc-private-individuals";
    public static final String CLAIM_CLIENT_USERS_KYC_ENTITIES = "client-users-kyc-entities";
    public static final String CLAIM_PSD2_LICENSED = "psd2-licensed";
    public static final String CLAIM_AIS = "ais";
    public static final String CLAIM_PIS = "pis";
    public static final String CLAIM_CAM = "cam";
    public static final String CLAIM_DATA_ENRICHMENT_MERCHANT_RECOGNITION = "data_enrichment_merchant_recognition";
    public static final String CLAIM_DATA_ENRICHMENT_CATEGORIZATION = "data_enrichment_categorization";
    public static final String CLAIM_DATA_ENRICHMENT_CYCLE_DETECTION = "data_enrichment_cycle_detection";
    public static final String CLAIM_DATA_ENRICHMENT_LABELS = "data_enrichment_labels";
    public static final String CLAIM_DELETED = "deleted";
    public static final String CLAIM_ONE_OFF_AIS = "one_off_ais";
    public static final String CLAIM_CONSENT_STARTER = "consent_starter";
    public static final String CLAIM_RISK_INSIGHTS = "risk_insights";

    public static final String[] CLIENT_TOKEN_REQUIRED_CLAIMS = {
            SUBJECT,
            EXTRA_CLAIM_ISSUED_FOR,
            EXTRA_CLAIM_CLIENT_GROUP_ID
    };
}
