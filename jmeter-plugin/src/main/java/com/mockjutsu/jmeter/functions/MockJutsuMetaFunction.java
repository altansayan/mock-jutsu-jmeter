package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuMetaFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_meta"; }
    @Override protected String typeDescription() {
        return "uuid | requestid | correlationid | sessionid | idempotencykey | deviceid | ipv4 | ipv6 | browser_name | browser_version | browser_engine | useragent | timestamp | timestamp_iso | clientversion | bearertoken | signature | apppassword | jwt | hash | mac_address | domain | url | color | api_key | totp_code | webhook_signature | transaction_id | public_ip | private_ip | slug | http_method | http_status_code | port_number | hostname | tld | uri_path";
    }
}
