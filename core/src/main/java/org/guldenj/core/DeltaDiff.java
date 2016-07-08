package org.guldenj.core;
import org.guldenj.store.BlockStore;
import java.math.BigInteger;
import org.guldenj.store.BlockStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class DeltaDiff extends OldDiff {
public static
long GetNextWorkRequired_DELTA (StoredBlock pindexLast, Block block, int nPowTargetSpacing, long nPowLimit, long nFirstDeltaBlock
,final BlockStore blockStore
)
{
    try{
    long nRetargetTimespan = nPowTargetSpacing;
    long nProofOfWorkLimit = nPowLimit;
    long nLastBlock = 1;
    long nShortFrame = 3;
    long nMiddleFrame = 24;
    long nLongFrame = 576;
    long nLBWeight = 64;
    long nShortWeight = 8;
    long nMiddleWeight = 2;
    long nLongWeight = 1;
    long nLBMinGap = nRetargetTimespan / 6;
    long nLBMaxGap = nRetargetTimespan * 6;
    long nQBFrame = nShortFrame + 1;
    long nQBMinGap = (nRetargetTimespan * 100 / 120 ) * nQBFrame;
    long nBadTimeLimit = 0;
    long nBadTimeReplace = nRetargetTimespan / 10;
    long nLowTimeLimit = nRetargetTimespan * 90 / 100;
    long nFloorTimeLimit = nRetargetTimespan * 65 / 100;
    long nLongTimeLimit = 2 * 16 * 60;
    long nLongTimeStep = 15 * 60;
    long nMinimumAdjustLimit = (long)nRetargetTimespan * 75 / 100;
    long nMaximumAdjustLimit = (long)nRetargetTimespan * 150 / 100;
    long nDeltaTimespan = 0;
    long nLBTimespan = 0;
    long nShortTimespan = 0;
    long nMiddleTimespan = 0;
    long nLongTimespan = 0;
    long nQBTimespan = 0;
    long nWeightedSum = 0;
    long nWeightedDiv = 0;
    long nWeightedTimespan = 0;
    StoredBlock pindexFirst = pindexLast;
    if (pindexLast == null)
        return nProofOfWorkLimit;
    if (pindexLast.getHeight() <= nQBFrame)
        return nProofOfWorkLimit;
    pindexFirst = blockStore.get(pindexLast.getHeader().getPrevBlockHash());
    nLBTimespan = pindexLast.getHeader().getTimeSeconds() - pindexFirst.getHeader().getTimeSeconds();
    if (nLBTimespan > nBadTimeLimit && nLBTimespan < nLBMinGap)
        nLBTimespan = nLBTimespan * 50 / 100;
    if (nLBTimespan <= nBadTimeLimit)
        nLBTimespan = nBadTimeReplace;
    if (nLBTimespan > nLBMaxGap)
        nLBTimespan = nLBTimespan * 150 / 100;
    pindexFirst = pindexLast;
    for (long i = 1; pindexFirst != null && i <= nQBFrame; i++)
    {
        nDeltaTimespan = pindexFirst.getHeader().getTimeSeconds() - blockStore.get(pindexFirst.getHeader().getPrevBlockHash()).getHeader().getTimeSeconds();
        if (nDeltaTimespan <= nBadTimeLimit)
            nDeltaTimespan = nBadTimeReplace;
        if (i<= nShortFrame)
            nShortTimespan += nDeltaTimespan;
        nQBTimespan += nDeltaTimespan;
        pindexFirst = blockStore.get(pindexFirst.getHeader().getPrevBlockHash());
    }
    if (pindexLast.getHeight() - nFirstDeltaBlock <= nMiddleFrame)
    {
        nMiddleWeight = nMiddleTimespan = 0;
    }
    else
    {
        pindexFirst = pindexLast;
        for (long i = 1; pindexFirst != null && i <= nMiddleFrame; i++)
        {
            nDeltaTimespan = pindexFirst.getHeader().getTimeSeconds() - blockStore.get(pindexFirst.getHeader().getPrevBlockHash()).getHeader().getTimeSeconds();
            if (nDeltaTimespan <= nBadTimeLimit)
                nDeltaTimespan = nBadTimeReplace;
            nMiddleTimespan += nDeltaTimespan;
            pindexFirst = blockStore.get(pindexFirst.getHeader().getPrevBlockHash());
        }
    }
    if (pindexLast.getHeight() - nFirstDeltaBlock <= nLongFrame)
    {
        nLongWeight = nLongTimespan = 0;
    }
    else
    {
        pindexFirst = pindexLast;
        for (long i = 1; pindexFirst != null && i <= nLongFrame; i++)
            pindexFirst = blockStore.get(pindexFirst.getHeader().getPrevBlockHash());
        nLongTimespan = pindexLast.getHeader().getTimeSeconds() - pindexFirst.getHeader().getTimeSeconds();
    }
    if ( (nQBTimespan > nBadTimeLimit) && (nQBTimespan < nQBMinGap) && (nLBTimespan < nRetargetTimespan * 80 / 100) )
    {
        nMiddleWeight = nMiddleTimespan = nLongWeight = nLongTimespan = 0;
    }
    nWeightedSum = (nLBTimespan * nLBWeight) + (nShortTimespan * nShortWeight);
    nWeightedSum += (nMiddleTimespan * nMiddleWeight) + (nLongTimespan * nLongWeight);
    nWeightedDiv = (nLastBlock * nLBWeight) + (nShortFrame * nShortWeight);
    nWeightedDiv += (nMiddleFrame * nMiddleWeight) + (nLongFrame * nLongWeight);
    nWeightedTimespan = nWeightedSum / nWeightedDiv;
    if (Math.abs(nLBTimespan - nRetargetTimespan) < nRetargetTimespan * 10 / 100)
    {
        nMinimumAdjustLimit = (long)nRetargetTimespan * 90 / 100;
        nMaximumAdjustLimit = (long)nRetargetTimespan * 110 / 100;
    }
    else if (Math.abs(nLBTimespan - nRetargetTimespan) < nRetargetTimespan * 20 / 100)
    {
        nMinimumAdjustLimit = (long)nRetargetTimespan * 80 / 100;
        nMaximumAdjustLimit = (long)nRetargetTimespan * 120 / 100;
    }
    if (nWeightedTimespan < nMinimumAdjustLimit)
        nWeightedTimespan = nMinimumAdjustLimit;
    if (nWeightedTimespan > nMaximumAdjustLimit)
        nWeightedTimespan = nMaximumAdjustLimit;
    BigInteger bnNew;
    bnNew = Utils.decodeCompactBits(pindexLast.getHeader().getDifficultyTarget());
    bnNew = bnNew.multiply(BigInteger.valueOf(nWeightedTimespan));
    bnNew = bnNew.divide(BigInteger.valueOf(nRetargetTimespan));
    nLBTimespan = pindexLast.getHeader().getTimeSeconds() - blockStore.get(pindexLast.getHeader().getPrevBlockHash()).getHeader().getTimeSeconds();
    BigInteger bnComp;
    bnComp = Utils.decodeCompactBits(pindexLast.getHeader().getDifficultyTarget());
    if (nLBTimespan > 0 && nLBTimespan < nLowTimeLimit && (bnNew.compareTo(bnComp) == 1))
    {
        if (nLBTimespan < nFloorTimeLimit)
        {
            bnNew = Utils.decodeCompactBits(pindexLast.getHeader().getDifficultyTarget());
            bnNew = bnNew.multiply(BigInteger.valueOf(95));
            bnNew = bnNew.divide(BigInteger.valueOf(100));
        }
        else
        {
            bnNew = Utils.decodeCompactBits(pindexLast.getHeader().getDifficultyTarget());
        }
    }
    if ((block.getTimeSeconds() - pindexLast.getHeader().getTimeSeconds()) > nLongTimeLimit)
    {
        long nNumMissedSteps = ((block.getTimeSeconds() - pindexLast.getHeader().getTimeSeconds()) / nLongTimeStep);
        bnNew = bnNew.multiply(BigInteger.valueOf(nNumMissedSteps));
    }
    bnComp = Utils.decodeCompactBits(nProofOfWorkLimit);
    if ((bnNew.compareTo(bnComp) == 1))
        bnNew = Utils.decodeCompactBits(nProofOfWorkLimit);
    return Utils.encodeCompactBits(bnNew);
    }
    catch (BlockStoreException e)
    {
            throw new RuntimeException(e);
    }
}
}
