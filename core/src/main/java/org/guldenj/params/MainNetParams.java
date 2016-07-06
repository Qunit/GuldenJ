/*
 * Copyright 2013 Google Inc.
 * Copyright 2015 Andreas Schildbach
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

import org.guldenj.core.*;
import org.guldenj.net.discovery.*;

import java.net.*;

import static com.google.common.base.Preconditions.*;

/**
 * Parameters for the main production network on which people trade goods and services.
 */
public class MainNetParams extends AbstractBitcoinNetParams {
    public MainNetParams() {
        super();
        interval = INTERVAL;
        targetTimespan = TARGET_TIMESPAN;
        maxTarget = Utils.decodeCompactBits(0x1f3fffffL);
        dumpedPrivateKeyHeader = 166;
        addressHeader = 38;
        p2shHeader = 5;
        acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
        port = 9231;
        packetMagic = 0xfcfef7e0L;
        bip32HeaderPub = 0x0488B21E; //The 4 byte header that serializes in base58 to "xpub".
        bip32HeaderPriv = 0x0488ADE4; //The 4 byte header that serializes in base58 to "xprv"

        genesisBlock.setDifficultyTarget(0x1e0ffff0L);
        genesisBlock.setTime(1009843200L);
        genesisBlock.setNonce(2200095L);
        id = ID_MAINNET;
        subsidyDecreaseBlockCount = 840000;
        spendableCoinbaseDepth = 100;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("6c5d71a461b5bff6742bb62e5be53978b8dec5103ce52d1aaab8c6a251582f92"),
                genesisHash);

        // This contains (at a minimum) the blocks which are not BIP30 compliant. BIP30 changed how duplicate
        // transactions are handled. Duplicated transactions could occur in the case where a coinbase had the same
        // extraNonce and the same outputs but appeared at different heights, and greatly complicated re-org handling.
        // Having these here simplifies block connection logic considerably.
        checkpoints.put(      0, Sha256Hash.wrap("6c5d71a461b5bff6742bb62e5be53978b8dec5103ce52d1aaab8c6a251582f92"));
        checkpoints.put(   1000, Sha256Hash.wrap("77676cde325930f1a2f3bdabf34e54f06445e7dfd8b85a6aab372f60a222fa30"));
        checkpoints.put(   2000, Sha256Hash.wrap("9732e5f8b9fec4f62f83171eaa033cffa11714ba56dbb1dd60df681b358c9dd2"));
        checkpoints.put(  10000, Sha256Hash.wrap("25a619632ea07771156d61791245e7b3497ae987ef6be5348c41380291848974"));
        checkpoints.put(  15000, Sha256Hash.wrap("944e0468c38392c5f32818f8f50c10aa6deb5986d85a72e9aaddfe94acc74a5c"));
        checkpoints.put(  19000, Sha256Hash.wrap("2a9d91e8b61dc77b79ea43befeb72a1a8c89af3e8a40dbdba5b3a6b5f7510e91"));
        checkpoints.put(  30300, Sha256Hash.wrap("6340483a4bdd4e3a519a292ae4bc424dc12b8c72ef8f3cf3762347afc0a699c0"));
        checkpoints.put(  35000, Sha256Hash.wrap("e14bac6cfea31014bb057500160fb5a962e492ce16652b14fa07314fd9e523ff"));
        checkpoints.put(  45000, Sha256Hash.wrap("97b4cff99eda714dbff09881e339d1159e5558486e31198affd712ca806f0b1d"));
        checkpoints.put(  86600, Sha256Hash.wrap("9e3e0388b4712f2787cd443a7dbeeda12e90b98e909877cf814e7d5a60fc4b85"));
        checkpoints.put( 100000, Sha256Hash.wrap("5e831ed155d05f6ac7f17635022dbc348bf73942309ac403c6f8c2990e2e0af1"));
        checkpoints.put( 125000, Sha256Hash.wrap("ee27d0f4b6596f302eb591072136ae196bb318d776c16625b23cc7383052b564"));
        checkpoints.put( 150000, Sha256Hash.wrap("97fdb21189d5a958d42fcb58f8d300e737a20fad91878dabdd925d11fc614013"));
        checkpoints.put( 175000, Sha256Hash.wrap("da6aa09113ddd62d871e9aacad6131831d5841a26968f1665a9b829fd30a29e3"));
        checkpoints.put( 200000, Sha256Hash.wrap("4e80313f4eb23093a63218f3736379084d1eeae46c4343668f3cdc9c0c5ca260"));
        checkpoints.put( 212000, Sha256Hash.wrap("1301ebdd83f6a9c224de33817d69e3fa339769acfd4401cbc3c3c88202c3dbdb"));
        checkpoints.put( 225000, Sha256Hash.wrap("c9a5c5226d8f103972ffee38c31c3508189b694e0d4f93a394ccea2cac82ce49"));
        checkpoints.put( 233500, Sha256Hash.wrap("7b16385152001b51c25004e04b1f62906088027d8753449bc36db88ef540aaaa"));
        checkpoints.put( 250000, Sha256Hash.wrap("a6635e1dbce15cfb4be7f3f464f612205dd13ba96828535000b99ce04648500d"));
        checkpoints.put( 260000, Sha256Hash.wrap("42c2254ffd8be411386b9089fec985fe3a06d5fc386ff0bd494b5a3aa292f107"));
        checkpoints.put( 280350, Sha256Hash.wrap("f95b3e7f97a41db38a872bdd15d985aae252c5ab497a51319e5bd50161a48d18"));

        dnsSeeds = new String[] {
              "seed-000.gulden.com",
              "seed-001.gulden.blue",
              "seed-002.gulden.network",
              "seed-003.gulden.com",
              "seed-004.gulden.blue",
              "seed-005.gulden.network"
        };
        httpSeeds = new HttpDiscovery.Details[] {
                // Mike Hearn
                new HttpDiscovery.Details(
                        ECKey.fromPublicOnly(Utils.HEX.decode("027a79143a4de36341494d21b6593015af6b2500e720ad2eda1c0b78165f4f38c4")),
                        URI.create("http://main.seed.vinumeris.com/peers")
                ),
                // Andreas Schildbach
                new HttpDiscovery.Details(
                        ECKey.fromPublicOnly(Utils.HEX.decode("0238746c59d46d5408bf8b1d0af5740fe1a6e1703fcb56b2953f0b965c740d256f")),
                        URI.create("http://httpseed.bitcoin.schildbach.de/peers")
                )
        };

        addrSeeds = new int[] {
            0x3BF04580, 0xF2DD77D5, 0x4AA4175E, 0x3509BB25, 0x81097786, 0xF0F1AA6B
        };
    }

    private static MainNetParams instance;
    public static synchronized MainNetParams get() {
        if (instance == null) {
            instance = new MainNetParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_MAINNET;
    }
}
