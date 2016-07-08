package org.guldenj.core;
import org.guldenj.store.BlockStore;
import java.math.BigInteger;
import org.guldenj.store.BlockStoreException;
import java.util.*;
public class OldDiff{
static int nMaxHeight = 260000;
static int nDiffArraySize = nMaxHeight + 1;
static Integer udiff[];
static Boolean is_unit_test = null;
public static boolean isJUnitTest()
{
 if (is_unit_test == null)
 {
     StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
     List<StackTraceElement> list = Arrays.asList(stackTrace);
     for (StackTraceElement element : list) {
         if (element.getClassName().startsWith("org.junit.")) {
             is_unit_test = new Boolean(true);
         }
     }
     if (is_unit_test == null)
     {
      is_unit_test = new Boolean(false);
     }
 }
 return is_unit_test;
}
public static
long diff_old(int nHeight, long nPowLimit)
{
     if(udiff == null)
    {
        udiff = new Integer[nDiffArraySize];
        Scanner s = new Scanner(OldDiff.class.getResourceAsStream("/org.gulden.difficulties"));
        int count = 0;
        while (count < nMaxHeight)
        {
            udiff[count] = Integer.valueOf(s.next());
            count = count+1;
        }
        s.close();
    }
    long nRet;
    if ((nHeight < 0) || (nHeight > nMaxHeight))
    {
        ;
        return 0;
    }
    nRet = (long)udiff[nHeight];
    if (isJUnitTest())
    {
     nRet*=4;
    }
    if (nRet > nPowLimit)
     return nPowLimit;
    return nRet;
}
}
