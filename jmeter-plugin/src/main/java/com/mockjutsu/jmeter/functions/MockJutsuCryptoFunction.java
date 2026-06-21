package com.mockjutsu.jmeter.functions;
import com.mockjutsu.jmeter.MockJutsuBaseFunction;
public final class MockJutsuCryptoFunction extends MockJutsuBaseFunction {
    @Override public String getReferenceKey() { return "__mockjutsu_crypto"; }
    @Override protected String typeDescription() {
        return "btc_address | eth_address | crypto_address | tx_hash | block_hash | mnemonic | nft_token_id | gas_price | gas_limit | defi_protocol_name | blockchain_network | wallet_label | defi_position_type | cryptocurrency_name | liquidity_pool_id | liquidity_pool_id_masked";
    }
}
