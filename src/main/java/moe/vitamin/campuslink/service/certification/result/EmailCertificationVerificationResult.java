package moe.vitamin.campuslink.service.certification.result;

public enum EmailCertificationVerificationResult {

    NOT_IN_PROGRESS,
    INVALID_CODE,
    EXPIRED,
    INTERNAL_ERROR,
    TIMEOUT,
    SUCCESS

}
