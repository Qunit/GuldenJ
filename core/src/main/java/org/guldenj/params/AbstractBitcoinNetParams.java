/*
 * Copyright 2013 Google Inc.
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

import org.guldenj.core.Block;
import org.guldenj.core.Coin;
import org.guldenj.core.NetworkParameters;
import org.guldenj.core.StoredBlock;
import org.guldenj.core.Transaction;
import org.guldenj.core.Utils;
import org.guldenj.utils.MonetaryFormat;
import org.guldenj.core.VerificationException;
import org.guldenj.store.BlockStore;
import org.guldenj.store.BlockStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.guldenj.core.CommonDiff;

/**
 * Parameters for Bitcoin-like networks.
 */
public abstract class AbstractBitcoinNetParams extends NetworkParameters {
    /**
     * Scheme part for Bitcoin URIs.
     */
    public static final String BITCOIN_SCHEME = "gulden";

    private static final Logger log = LoggerFactory.getLogger(AbstractBitcoinNetParams.class);

    public AbstractBitcoinNetParams() {
        super();
    }

    /** 
     * Checks if we are at a difficulty transition point. 
     * @param storedPrev The previous stored block 
     * @return If this is a difficulty transition point 
     */
    protected boolean isDifficultyTransitionPoint(StoredBlock storedPrev) {
        return ((storedPrev.getHeight() + 1) % this.getInterval()) == 0;
    }

    @Override
    public void checkDifficultyTransitions(final StoredBlock storedPrev, final Block nextBlock,
    	final BlockStore blockStore) throws VerificationException, BlockStoreException {

	if (storedPrev.getHeight() > CommonDiff.nOldDiffSwitchoverBlock)
        {
        	int countPrev = 0;
		StoredBlock prev = storedPrev;
		try
		{
	                while(countPrev < 600)
			{
				prev = blockStore.get(prev.getHeader().getPrevBlockHash());
				countPrev++;
			}
		}
		catch (Exception e)
		{
			//SKip calculation.
			return;
		}
        }

        long newTargetCompact = CommonDiff.GetNextWorkRequired(storedPrev, nextBlock, this.TARGET_SPACING, Utils.encodeCompactBits(this.getMaxTarget()), blockStore);
        long receivedTargetCompact = nextBlock.getDifficultyTarget();

        if (newTargetCompact != receivedTargetCompact)
            throw new VerificationException("Network provided difficulty bits do not match what was calculated: " +
                    newTargetCompact + " vs " + receivedTargetCompact);
    }

    @Override
    public Coin getMaxMoney() {
        return MAX_MONEY;
    }

    @Override
    public Coin getMinNonDustOutput() {
        return Transaction.MIN_NONDUST_OUTPUT;
    }

    @Override
    public MonetaryFormat getMonetaryFormat() {
        return new MonetaryFormat();
    }

    @Override
    public String getUriScheme() {
        return BITCOIN_SCHEME;
    }

    @Override
    public boolean hasMaxMoney() {
        return true;
    }
}
