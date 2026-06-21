package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuMetaFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_meta"; }
    @Override protected String typeDescription() {
        return "uuid | requestid | correlationid | sessionid | idempotencykey | deviceid | ipv4 | ipv6 | public_ip | private_ip | browser_name | browser_version | browser_engine | useragent | timestamp | timestamp_iso | clientversion | bearertoken | signature[:secret|payload] | apppassword | jwt | hash[:md5|sha1|sha256|sha384|sha512|sha3-256|sha3-512|crc32|adler32|crc16] | mac_address | domain | url | color[:hex|rgb|hsl|name] | api_key | totp_code | webhook_signature | transaction_id | slug | http_method | http_status_code | port_number | hostname | tld | uri_path";
    }
}
