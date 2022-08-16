package com.bloxbean.cardano.yaci.core.model;

import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnsignedInteger;
import com.bloxbean.cardano.client.address.util.AddressUtil;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import lombok.*;

import java.math.BigInteger;
import java.util.Objects;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
public class Withdrawal {
    /**
     * Bech32 reward address
     */
    private String rewardAddress;
    private BigInteger coin;

    public static Withdrawal deserialize(Map withdrawalMap, DataItem addrKey) throws CborDeserializationException {
        Objects.requireNonNull(withdrawalMap);
        Objects.requireNonNull(addrKey);

        String rewardAddress;
        try {
            rewardAddress = AddressUtil.bytesToAddress(((ByteString) addrKey).getBytes());
        } catch (Exception e) {
            throw new CborDeserializationException("Bytes cannot be converted to bech32 address", e);
        }

        UnsignedInteger coinDI = (UnsignedInteger) withdrawalMap.get(addrKey);

        return new Withdrawal(rewardAddress,
                coinDI != null ? coinDI.getValue() : null);
    }

    /**
     * Add Withdrawal to Withdrawal map
     *
     * @param withdrawalMap
     * @throws AddressExcepion
     */
    public void serialize(Map withdrawalMap) throws AddressExcepion {
        Objects.requireNonNull(withdrawalMap);

        byte[] addressBytes = AddressUtil.addressToBytes(rewardAddress);
        withdrawalMap.put(new ByteString(addressBytes), new UnsignedInteger(coin));
    }
}
