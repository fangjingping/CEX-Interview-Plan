package com.cex.exchange.risk;

import java.util.Objects;

public class RiskResult {
    private final boolean ok;
    private final RiskErrorCode code;
    private final String message;
    private final FrozenRecord frozenRecord;

    private RiskResult(boolean ok, RiskErrorCode code, String message, FrozenRecord frozenRecord) {
        this.ok = ok;
        this.code = Objects.requireNonNull(code, "code");
        this.message = message;
        this.frozenRecord = frozenRecord;
    }

    public static RiskResult ok(FrozenRecord record) {
        return new RiskResult(true, RiskErrorCode.OK, "OK", record);
    }

    public static RiskResult reject(RiskErrorCode code, String message) {
        return new RiskResult(false, code, message, null);
    }

    public boolean isOk() {
        return ok;
    }

    public RiskErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public FrozenRecord getFrozenRecord() {
        return frozenRecord;
    }
}
