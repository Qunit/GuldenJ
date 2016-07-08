package org.guldenj.core;
import org.guldenj.store.BlockStore;
import java.math.BigInteger;
import org.guldenj.store.BlockStoreException;
public class CommonDiff extends DeltaDiff {
public static int nDeltaSwitchoverBlock = 250000;
public static int nOldDiffSwitchoverBlock = 260000;
public static
long GetNextWorkRequired(StoredBlock indexLast, Block block, long nPowTargetSpacing, long nPowLimit
,final BlockStore blockStore
)
{
    if (indexLast.getHeight()+1 >= nOldDiffSwitchoverBlock)
    {
        if (indexLast.getHeight()+1 >= nDeltaSwitchoverBlock)
        {
            return GetNextWorkRequired_DELTA(indexLast, block, (int)nPowTargetSpacing, nPowLimit, nDeltaSwitchoverBlock, blockStore);
        }
        else
        {
            return 524287999;
        }
    }
    return diff_old(indexLast.getHeight()+1, nPowLimit);
}
}
