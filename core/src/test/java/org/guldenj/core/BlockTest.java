/**
 * Copyright 2011 Google Inc.
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

package org.guldenj.core;

import org.guldenj.params.MainNetParams;
import org.guldenj.params.TestNet2Params;
import org.guldenj.params.UnitTestParams;
import org.guldenj.script.ScriptOpCodes;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import static org.guldenj.core.Utils.HEX;
import static org.junit.Assert.*;

public class BlockTest {
    static final NetworkParameters params = TestNet2Params.get();

    public static final byte[] blockBytes;

    static {
        // Block 00000000a6e5eb79dcec11897af55e90cd571a4335383a3ccfbc12ec81085935
        // One with lots of transactions in, so a good test of the merkle tree hashing.
        blockBytes = HEX.decode("02000000029c033d3f6231aa85954364a3f06430882290a2124f0840951a2578aa44f9407794312ff9c8bfc1e1e034358521cbf05d86472814c5d20266bd7166d2ff13aa9aaa3653ffff0f1e000008560101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0a5a0104062f503253482fffffffff0100e8764817000000232103e768413b8f890083bf8181fdb96d026f6de584a11264ee7fdbfab7f19f4bf60cac00000000");
    }

    @Before
    public void setUp() throws Exception {
        Context context = new Context(params);
    }

    @Test
    public void testWork() throws Exception {
        BigInteger work = params.getGenesisBlock().getWork();
        // This number is printed by the official client at startup as the calculated value of chainWork on testnet:
        //
        // SetBestChain: new best=00000007199508e34a9f  height=0  work=536879104
        assertEquals(BigInteger.valueOf(536879104L), work);
    }

    @Test
    public void testBlockVerification() throws Exception {
/*        Block block = new Block(params, blockBytes);
        block.verify();
        assertEquals("00000000a6e5eb79dcec11897af55e90cd571a4335383a3ccfbc12ec81085935", block.getHashAsString());*/
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testDate() throws Exception {
        Block block = new Block(params, blockBytes);
        assertEquals("29 Mar 2014 11:12:26 GMT", block.getTime().toGMTString());
    }

    @Test
    public void testProofOfWork() throws Exception {
        // This params accepts any difficulty target.
/*        NetworkParameters params = UnitTestParams.get();
        Block block = new Block(params, blockBytes);
        block.setNonce(12346);
        try {
            block.verify();
            fail();
        } catch (VerificationException e) {
            // Expected.
        }
        // Blocks contain their own difficulty target. The BlockChain verification mechanism is what stops real blocks
        // from containing artificially weak difficulties.
        block.setDifficultyTarget(Block.EASIEST_DIFFICULTY_TARGET);
	OldDiff.is_unit_test = new Boolean(false);
        // Now it should pass.
        block.verify();
        // Break the nonce again at the lower difficulty level so we can try solving for it.
        block.setNonce(1);
        try {
            block.verify();
            fail();
        } catch (VerificationException e) {
            // Expected to fail as the nonce is no longer correct.
        }
        // Should find an acceptable nonce.
        block.solve();
        block.verify();
        assertEquals(block.getNonce(), 5);
        OldDiff.is_unit_test = true;*/
    }

    @Test
    public void testBadTransactions() throws Exception {
/*        Block block = new Block(params, blockBytes);
        // Re-arrange so the coinbase transaction is not first.
        Transaction tx1 = block.transactions.get(0);
        Transaction tx2 = block.transactions.get(1);
        block.transactions.set(0, tx2);
        block.transactions.set(1, tx1);
        try {
            block.verify();
            fail();
        } catch (VerificationException e) {
            // We should get here.
        }*/
    }

    @Test
    public void testHeaderParse() throws Exception {
        Block block = new Block(params, blockBytes);
        Block header = block.cloneAsHeader();
        Block reparsed = new Block(params, header.bitcoinSerialize());
        assertEquals(reparsed, header);
    }

    @Test
    public void testBitCoinSerialization() throws Exception {
        // We have to be able to reserialize everything exactly as we found it for hashing to work. This test also
        // proves that transaction serialization works, along with all its subobjects like scripts and in/outpoints.
        //
        // NB: This tests the BITCOIN proprietary serialization protocol. A different test checks Java serialization
        // of transactions.
        Block block = new Block(params, blockBytes);
        assertTrue(Arrays.equals(blockBytes, block.bitcoinSerialize()));
    }

    @Test
    public void testJavaSerialiazation() throws Exception {
        Block block = new Block(params, blockBytes);
        Transaction tx = block.transactions.get(1);

        // Serialize using Java.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(tx);
        oos.close();
        byte[] javaBits = bos.toByteArray();
        // Deserialize again.
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(javaBits));
        Transaction tx2 = (Transaction) ois.readObject();
        ois.close();

        // Note that this will actually check the transactions are equal by doing bitcoin serialization and checking
        // the bytestreams are the same! A true "deep equals" is not implemented for Transaction. The primary purpose
        // of this test is to ensure no errors occur during the Java serialization/deserialization process.
        assertEquals(tx, tx2);
    }
    
    @Test
    public void testUpdateLength() {
        NetworkParameters params = UnitTestParams.get();
        Block block = params.getGenesisBlock().createNextBlockWithCoinbase(new ECKey().getPubKey(), null);
        assertEquals(block.bitcoinSerialize().length, block.length);
        final int origBlockLen = block.length;
        Transaction tx = new Transaction(params);
        // this is broken until the transaction has > 1 input + output (which is required anyway...)
        //assertTrue(tx.length == tx.bitcoinSerialize().length && tx.length == 8);
        byte[] outputScript = new byte[10];
        Arrays.fill(outputScript, (byte) ScriptOpCodes.OP_FALSE);
        tx.addOutput(new TransactionOutput(params, null, Coin.SATOSHI, outputScript));
        tx.addInput(new TransactionInput(params, null, new byte[] {(byte) ScriptOpCodes.OP_FALSE},
                new TransactionOutPoint(params, 0, Sha256Hash.of(new byte[] { 1 }))));
        int origTxLength = 8 + 2 + 8 + 1 + 10 + 40 + 1 + 1;
        assertEquals(tx.bitcoinSerialize().length, tx.length);
        assertEquals(origTxLength, tx.length);
        block.addTransaction(tx);
        assertEquals(block.bitcoinSerialize().length, block.length);
        assertEquals(origBlockLen + tx.length, block.length);
        block.getTransactions().get(1).getInputs().get(0).setScriptBytes(new byte[] {(byte) ScriptOpCodes.OP_FALSE, (byte) ScriptOpCodes.OP_FALSE});
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength + 1);
        block.getTransactions().get(1).getInputs().get(0).setScriptBytes(new byte[] {});
        assertEquals(block.length, block.bitcoinSerialize().length);
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength - 1);
        block.getTransactions().get(1).addInput(new TransactionInput(params, null, new byte[] {(byte) ScriptOpCodes.OP_FALSE},
                new TransactionOutPoint(params, 0, Sha256Hash.of(new byte[] { 1 }))));
        assertEquals(block.length, origBlockLen + tx.length);
        assertEquals(tx.length, origTxLength + 41); // - 1 + 40 + 1 + 1
    }

    @Test
    public void isBIPs() throws Exception {
        final MainNetParams mainnet = MainNetParams.get();
        final Block genesis = mainnet.getGenesisBlock();
        assertFalse(genesis.isBIP34());
        assertFalse(genesis.isBIP66());
        assertFalse(genesis.isBIP65());

        // 227835/00000000000001aa077d7aa84c532a4d69bdbff519609d1da0835261b7a74eb6: last version 1 block
        final Block block227835 = new Block(mainnet,
                ByteStreams.toByteArray(getClass().getResourceAsStream("block227835.dat")));
        assertFalse(block227835.isBIP34());
        assertFalse(block227835.isBIP66());
        assertFalse(block227835.isBIP65());

        // 227836/00000000000000d0dfd4c9d588d325dce4f32c1b31b7c0064cba7025a9b9adcc: version 2 block
        final Block block227836 = new Block(mainnet,
                ByteStreams.toByteArray(getClass().getResourceAsStream("block227836.dat")));
        assertTrue(block227836.isBIP34());
        assertFalse(block227836.isBIP66());
        assertFalse(block227836.isBIP65());

        // 363703/0000000000000000011b2a4cb91b63886ffe0d2263fd17ac5a9b902a219e0a14: version 3 block
        final Block block363703 = new Block(mainnet,
                ByteStreams.toByteArray(getClass().getResourceAsStream("block363703.dat")));
        assertTrue(block363703.isBIP34());
        assertTrue(block363703.isBIP66());
        assertFalse(block363703.isBIP65());

        // 383616/00000000000000000aab6a2b34e979b09ca185584bd1aecf204f24d150ff55e9: version 4 block
        final Block block383616 = new Block(mainnet,
                ByteStreams.toByteArray(getClass().getResourceAsStream("block383616.dat")));
        assertTrue(block383616.isBIP34());
        assertTrue(block383616.isBIP66());
        assertTrue(block383616.isBIP65());
    }
}
