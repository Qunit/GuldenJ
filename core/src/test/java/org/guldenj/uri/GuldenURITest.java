/*
 * Copyright 2012, 2014 the original author or authors.
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
 *
 */

package org.guldenj.uri;

import org.guldenj.core.Address;
import org.guldenj.params.MainNetParams;
import org.guldenj.params.TestNet3Params;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.guldenj.core.Coin.*;
import static org.junit.Assert.*;

public class GuldenURITest {
    private GuldenURI testObject = null;

    private static final String MAINNET_GOOD_ADDRESS = "GbMFoq3q1KyrcTFzCi255rEzKJDQ5HULkj";

    @Test
    public void testConvertToGuldenURI() throws Exception {
        Address goodAddress = new Address(MainNetParams.get(), MAINNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("12.34"), "Hello World", "Mess & age + hope"));

        // no amount, label present, message present
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?label=Hello&message=glory", GuldenURI.convertToGuldenURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("0.1"), null, "glory"));
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("12.34"), "Hello", null));
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=1000", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("1000"), null, null));
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?amount=1000", GuldenURI.convertToGuldenURI(goodAddress, parseCoin("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?label=Hello", GuldenURI.convertToGuldenURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", GuldenURI.convertToGuldenURI(goodAddress, null, null, "Agatha"));
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", GuldenURI.convertToGuldenURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS, GuldenURI.convertToGuldenURI(goodAddress, null, null, null));
        assertEquals("gulden:" + MAINNET_GOOD_ADDRESS, GuldenURI.convertToGuldenURI(goodAddress, null, "", ""));
    }

    @Test
    public void testGood_Simple() throws GuldenURIParseException {
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
        assertNotNull(testObject);
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash160().length);
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new GuldenURI(MainNetParams.get(), "blimpcoin:" + MAINNET_GOOD_ADDRESS);
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + "|" + MAINNET_GOOD_ADDRESS);
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "\\");
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":");
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME);
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new GuldenURI(TestNet3Params.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws GuldenURIParseException {
        // Test the decimal parsing
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210.12345678");
        assertEquals("654321012345678", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=.12345678");
        assertEquals("12345678", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210");
        assertEquals("654321000000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws GuldenURIParseException {
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws GuldenURIParseException {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = GuldenURI.encodeURLString(testString);
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithRussian() throws GuldenURIParseException {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = GuldenURI.encodeURLString(moscowString); 
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws GuldenURIParseException {
/*        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());*/
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws GuldenURIParseException {
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "GuldenURI['amount'='654321000000000','label'='Hello World','message'='Be well','address'='GbMFoq3q1KyrcTFzCi255rEzKJDQ5HULkj']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws GuldenURIParseException {
        // Missing
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    @Test
    public void testEmpty_Label() throws GuldenURIParseException {
        assertNull(new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=").getLabel());
    }

    @Test
    public void testEmpty_Message() throws GuldenURIParseException {
        assertNull(new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=").getMessage());
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws GuldenURIParseException {
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    @Test
    public void testGood_ManyEquals() throws GuldenURIParseException {
        assertEquals("aardvark=zebra", new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":"
                + MAINNET_GOOD_ADDRESS + "?label=aardvark=zebra").getLabel());
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws GuldenURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws GuldenURIParseException {
        // Unknown not required field
        testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("GuldenURI['aardvark'='true','address'='GbMFoq3q1KyrcTFzCi255rEzKJDQ5HULkj']", testObject.toString());

        assertEquals("true", testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("no separator"));
        }

        // Unknown and required field
        try {
            testObject = new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting GuldenURIParseException");
        } catch (GuldenURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws GuldenURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "gulden://GbMFoq3q1KyrcTFzCi255rEzKJDQ5HULkj?amount=0.01000000";
        GuldenURI uri = new GuldenURI(str);
        assertEquals("GbMFoq3q1KyrcTFzCi255rEzKJDQ5HULkj", uri.getAddress().toString());
        assertEquals(CENT, uri.getAmount());
    }

    @Test(expected = GuldenURIParseException.class)
    public void testBad_AmountTooPrecise() throws GuldenURIParseException {
        new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=0.123456789");
    }

    @Test(expected = GuldenURIParseException.class)
    public void testBad_NegativeAmount() throws GuldenURIParseException {
        new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=-1");
    }

    @Test(expected = GuldenURIParseException.class)
    public void testBad_TooLargeAmount() throws GuldenURIParseException {
        new GuldenURI(MainNetParams.get(), GuldenURI.BITCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=90000000000");
    }

    @Test
    public void testPaymentProtocolReq() throws Exception {
        // Non-backwards compatible form ...
        GuldenURI uri = new GuldenURI(TestNet3Params.get(), "gulden:?r=https%3A%2F%2Fbitcoincore.org%2F%7Egavin%2Ff.php%3Fh%3Db0f02e7cea67f168e25ec9b9f9d584f9");
        assertEquals("https://bitcoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://bitcoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9"),
                uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }

    @Test
    public void testMultiplePaymentProtocolReq() throws Exception {
        GuldenURI uri = new GuldenURI(MainNetParams.get(),
                "gulden:?r=https%3A%2F%2Fbitcoincore.org%2F%7Egavin&r1=bt:112233445566");
        assertEquals(ImmutableList.of("bt:112233445566", "https://bitcoincore.org/~gavin"), uri.getPaymentRequestUrls());
        assertEquals("https://bitcoincore.org/~gavin", uri.getPaymentRequestUrl());
    }

    @Test
    public void testNoPaymentProtocolReq() throws Exception {
        GuldenURI uri = new GuldenURI(MainNetParams.get(), "gulden:" + MAINNET_GOOD_ADDRESS);
        assertNull(uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of(), uri.getPaymentRequestUrls());
        assertNotNull(uri.getAddress());
    }

    @Test
    public void testUnescapedPaymentProtocolReq() throws Exception {
        GuldenURI uri = new GuldenURI(TestNet3Params.get(),
                "gulden:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe");
        assertEquals("https://merchant.com/pay.php?h=2a8628fc2fbe", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://merchant.com/pay.php?h=2a8628fc2fbe"), uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }
}
