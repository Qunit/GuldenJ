/*
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
import org.guldenj.params.Networks;
import org.guldenj.params.TestNet3Params;
import org.guldenj.script.Script;
import org.guldenj.script.ScriptBuilder;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.guldenj.core.Utils.HEX;
import static org.junit.Assert.*;

public class AddressTest {
    static final NetworkParameters testParams = TestNet3Params.get();
    static final NetworkParameters mainParams = MainNetParams.get();

    @Test
    public void testJavaSerialization() throws Exception {
        Address testAddress = Address.fromBase58(testParams, "gbmb8AWregJKrCgw2RhisgAsxKuQisrGhH");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        new ObjectOutputStream(os).writeObject(testAddress);
        VersionedChecksummedBytes testAddressCopy = (VersionedChecksummedBytes) new ObjectInputStream(
                new ByteArrayInputStream(os.toByteArray())).readObject();
        assertEquals(testAddress, testAddressCopy);

        Address mainAddress = Address.fromBase58(mainParams, "GSZAFrTk92iErjAka55BhmN42nN7hXVJef");
        os = new ByteArrayOutputStream();
        new ObjectOutputStream(os).writeObject(mainAddress);
        VersionedChecksummedBytes mainAddressCopy = (VersionedChecksummedBytes) new ObjectInputStream(
                new ByteArrayInputStream(os.toByteArray())).readObject();
        assertEquals(mainAddress, mainAddressCopy);
    }

    @Test
    public void stringification() throws Exception {
        // Test a testnet address.
        Address a = new Address(testParams, HEX.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"));
        assertEquals("gYt79LGbiMU1Qjb7d75tegQg57EEoGWpeu", a.toString());
        assertFalse(a.isP2SHAddress());

        Address b = new Address(mainParams, HEX.decode("4a22c3c4cbb31e4d03b15550636762bda0baf85a"));
        assertEquals("GQbv4pPK7XfSLjDv9w6mZA5UJqme96Rkie", b.toString());
        assertFalse(b.isP2SHAddress());
    }
    
    @Test
    public void decoding() throws Exception {
        Address a = Address.fromBase58(testParams, "gbmb8AWregJKrCgw2RhisgAsxKuQisrGhH");
        assertEquals("69cf9635486299e150fd73a103db36fdd95c984f", Utils.HEX.encode(a.getHash160()));

        Address b = Address.fromBase58(mainParams, "GSZAFrTk92iErjAka55BhmN42nN7hXVJef");
        assertEquals("5f8d9d250e5d9603ce7d7bdc6bb8ed1e2a77e2ab", Utils.HEX.encode(b.getHash160()));
    }
    
    @Test
    public void errorPaths() {
        // Check what happens if we try and decode garbage.
        try {
            Address.fromBase58(testParams, "this is not a valid address!");
            fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the empty case.
        try {
            Address.fromBase58(testParams, "");
            fail();
        } catch (WrongNetworkException e) {
            fail();
        } catch (AddressFormatException e) {
            // Success.
        }

        // Check the case of a mismatched network.
        try {
            Address.fromBase58(testParams, "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL");
            fail();
        } catch (WrongNetworkException e) {
            // Success.
            assertEquals(e.verCode, MainNetParams.get().getAddressHeader());
            assertTrue(Arrays.equals(e.acceptableVersions, TestNet3Params.get().getAcceptableAddressCodes()));
        } catch (AddressFormatException e) {
            fail();
        }
    }

    @Test
    public void getNetwork() throws Exception {
        NetworkParameters params = Address.getParametersFromAddress("GSZAFrTk92iErjAka55BhmN42nN7hXVJef");
        assertEquals(MainNetParams.get().getId(), params.getId());
        params = Address.getParametersFromAddress("gbmb8AWregJKrCgw2RhisgAsxKuQisrGhH");
        assertEquals(TestNet3Params.get().getId(), params.getId());
    }

    @Test
    public void getAltNetwork() throws Exception {
        // An alternative network
        class AltNetwork extends MainNetParams {
            AltNetwork() {
                super();
                id = "alt.network";
                addressHeader = 48;
                p2shHeader = 5;
                acceptableAddressCodes = new int[] { addressHeader, p2shHeader };
            }
        }
        AltNetwork altNetwork = new AltNetwork();
        // Add new network params
        Networks.register(altNetwork);
        // Check if can parse address
        NetworkParameters params = Address.getParametersFromAddress("LLxSnHLN2CYyzB5eWTR9K9rS9uWtbTQFb6");
        assertEquals(altNetwork.getId(), params.getId());
        // Check if main network works as before
        params = Address.getParametersFromAddress("GSZAFrTk92iErjAka55BhmN42nN7hXVJef");
        assertEquals(MainNetParams.get().getId(), params.getId());
        // Unregister network
        Networks.unregister(altNetwork);
        try {
            Address.getParametersFromAddress("LLxSnHLN2CYyzB5eWTR9K9rS9uWtbTQFb6");
            fail();
        } catch (AddressFormatException e) { }
    }
    
    @Test
    public void p2shAddress() throws Exception {
        // Test that we can construct P2SH addresses
        Address mainNetP2SHAddress = Address.fromBase58(MainNetParams.get(), "35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU");
        assertEquals(mainNetP2SHAddress.version, MainNetParams.get().p2shHeader);
        assertTrue(mainNetP2SHAddress.isP2SHAddress());
        Address testNetP2SHAddress = Address.fromBase58(TestNet3Params.get(), "2MuVSxtfivPKJe93EC1Tb9UhJtGhsoWEHCe");
        assertEquals(testNetP2SHAddress.version, TestNet3Params.get().p2shHeader);
        assertTrue(testNetP2SHAddress.isP2SHAddress());

        // Test that we can determine what network a P2SH address belongs to
        NetworkParameters mainNetParams = Address.getParametersFromAddress("35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU");
        assertEquals(MainNetParams.get().getId(), mainNetParams.getId());
        NetworkParameters testNetParams = Address.getParametersFromAddress("2MuVSxtfivPKJe93EC1Tb9UhJtGhsoWEHCe");
        assertEquals(TestNet3Params.get().getId(), testNetParams.getId());

        // Test that we can convert them from hashes
        byte[] hex = HEX.decode("2ac4b0b501117cc8119c5797b519538d4942e90e");
        Address a = Address.fromP2SHHash(mainParams, hex);
        assertEquals("35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU", a.toString());
        Address b = Address.fromP2SHHash(testParams, HEX.decode("18a0e827269b5211eb51a4af1b2fa69333efa722"));
        assertEquals("2MuVSxtfivPKJe93EC1Tb9UhJtGhsoWEHCe", b.toString());
        Address c = Address.fromP2SHScript(mainParams, ScriptBuilder.createP2SHOutputScript(hex));
        assertEquals("35b9vsyH1KoFT5a5KtrKusaCcPLkiSo1tU", c.toString());
    }

    @Test
    public void p2shAddressCreationFromKeys() throws Exception {
        // import some keys from this example: https://gist.github.com/gavinandresen/3966071
        ECKey key1 = DumpedPrivateKey.fromBase58(mainParams, "Rfpv4Xq78pKdDqoR62XDoobvhA6v11sD13RzVPatEWPxbbs7YX2r").getKey();
        key1 = ECKey.fromPrivate(key1.getPrivKeyBytes());
        ECKey key2 = DumpedPrivateKey.fromBase58(mainParams, "RaH51RERGj2vRT95NcTkn1VZDQvJq9qnXWsPQ5CWYMkapAZEup8g").getKey();
        key2 = ECKey.fromPrivate(key2.getPrivKeyBytes());
        ECKey key3 = DumpedPrivateKey.fromBase58(mainParams, "Rgz1vKxL6eJTdsZ9DC8F29WP4jXyKngJjqkUiFsp4wSYY9hQZrZa").getKey();
        key3 = ECKey.fromPrivate(key3.getPrivKeyBytes());

        List<ECKey> keys = Arrays.asList(key1, key2, key3);
        Script p2shScript = ScriptBuilder.createP2SHOutputScript(2, keys);
        Address address = Address.fromP2SHScript(mainParams, p2shScript);
        assertEquals("3C27FNfd4NCW1YgFJ1ea93pH9QDgEM7Eos", address.toString());
    }

    @Test
    public void cloning() throws Exception {
        Address a = new Address(testParams, HEX.decode("fda79a24e50ff70ff42f7d89585da5bd19d9e5cc"));
        Address b = a.clone();

        assertEquals(a, b);
        assertNotSame(a, b);
    }

    @Test
    public void roundtripBase58() throws Exception {
        String base58 = "17kzeh4N8g49GFvdDzSf8PjaPfyoD1MndL";
        assertEquals(base58, Address.fromBase58(null, base58).toBase58());
    }

    @Test
    public void comparisonCloneEqualTo() throws Exception {
        Address a = Address.fromBase58(mainParams, "GHy1PBNR6C8vamVaXzRsiJmx8Hmg2cB9BJ");
        Address b = a.clone();

        int result = a.compareTo(b);
        assertEquals(0, result);
    }

    @Test
    public void comparisonEqualTo() throws Exception {
        Address a = Address.fromBase58(mainParams, "1Dorian4RoXcnBv9hnQ4Y2C1an6NJ4UrjX");
        Address b = a.clone();

        int result = a.compareTo(b);
        assertEquals(0, result);
    }

    @Test
    public void comparisonLessThan() throws Exception {
        Address a = Address.fromBase58(mainParams, "GHy1PBNR6C8vamVaXzRsiJmx8Hmg2cB9BJ");
        Address b = Address.fromBase58(mainParams, "GSZAFrTk92iErjAka55BhmN42nN7hXVJef");

        int result = a.compareTo(b);
        assertTrue(result < 0);
    }

    @Test
    public void comparisonGreaterThan() throws Exception {
        Address a = Address.fromBase58(mainParams, "GSZAFrTk92iErjAka55BhmN42nN7hXVJef");
        Address b = Address.fromBase58(mainParams, "GHy1PBNR6C8vamVaXzRsiJmx8Hmg2cB9BJ");

        int result = a.compareTo(b);
        assertTrue(result > 0);
    }

    @Test
    public void comparisonBytesVsString() throws Exception {
        // TODO: To properly test this we need a much larger data set
        Address a = Address.fromBase58(mainParams, "GHy1PBNR6C8vamVaXzRsiJmx8Hmg2cB9BJ");
        Address b = Address.fromBase58(mainParams, "GSZAFrTk92iErjAka55BhmN42nN7hXVJef");

        int resultBytes = a.compareTo(b);
        int resultsString = a.toString().compareTo(b.toString());
        assertTrue( resultBytes < 0 );
        assertTrue( resultsString < 0 );
    }
}
