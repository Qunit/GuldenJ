/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guldenj.params;

import java.math.BigInteger;
import java.util.Date;

import org.guldenj.core.Block;
import org.guldenj.core.NetworkParameters;
import org.guldenj.core.StoredBlock;
import org.guldenj.core.Utils;
import org.guldenj.core.VerificationException;
import org.guldenj.store.BlockStore;
import org.guldenj.store.BlockStoreException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Parameters for the testnet, a separate public instance of Bitcoin that has relaxed rules suitable for development
 * and testing of applications and new Bitcoin versions.
 */
public class TestNet3Params extends AbstractBitcoinNetParams {
    public TestNet3Params() {
        super();
        id = ID_TESTNET;
        // Genesis hash is 000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943
        packetMagic = 0xfcfef700L;
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1f3fffffL);
        port = 9923;
        addressHeader = 98;
        p2shHeader = 196;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        dumpedPrivateKeyHeader = 226;
        genesisBlock.setTime(1399759200L);
        genesisBlock.setDifficultyTarget(0x1f3fffffL);
        genesisBlock.setNonce(397616);
        spendableCoinbaseDepth = 100;
        subsidyDecreaseBlockCount = 840000;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("bff0fcf9a89d4d4d6e00414e1d67ef495608c6569f7fbb5276cd20a46127f329"));
        alertSigningKey = Utils.HEX.decode("06087071e40ddf2ecbdf1ae40f536fa8f78e9383006c710dd3ecce957a3cb9292038d0840e3be5042a6b863f75dfbe1cae8755a0f7887ae459af689f66caacab52");

        dnsSeeds = new String[] {
                "testseed-00.gulden.blue",
                "testseed-01.gulden.network"
        };
        addrSeeds = null;
        bip32HeaderPub = 0x043587CF;
        bip32HeaderPriv = 0x04358394;
    }

    private static TestNet3Params instance;
    public static synchronized TestNet3Params get() {
        if (instance == null) {
            instance = new TestNet3Params();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_TESTNET;
    }

    // February 16th 2012
    private static final Date testnetDiffDate = new Date(1329264000000L);

    @Override
    public void checkDifficultyTransitions(final StoredBlock storedPrev, final Block nextBlock,
        final BlockStore blockStore) throws VerificationException, BlockStoreException {

       //fixme: GULDEN
    }
}
