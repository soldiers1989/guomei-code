package com.sunline.ccs.batch.cc1400;

public class TestNum {
    private static final long   MULTMIN_RADIX_TEN =  Long.MIN_VALUE / 10;
    private static final long N_MULTMAX_RADIX_TEN = -Long.MAX_VALUE / 10;
	public static void main(String[] args) {
		try {
			System.out.println(parseLong("1109018", 10));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static long parseLong(String s, int radix)
            throws Exception
  {
      if (s == null) {
          throw new NumberFormatException("null");
      }

	if (radix < Character.MIN_RADIX) {
	    throw new NumberFormatException("radix " + radix +
					    " less than Character.MIN_RADIX");
	}
	if (radix > Character.MAX_RADIX) {
	    throw new NumberFormatException("radix " + radix +
					    " greater than Character.MAX_RADIX");
	}

	long result = 0;
	boolean negative = false;
	int i = 0, max = s.length();
	long limit;
	long multmin;
	int digit;

	if (max > 0) {
	    if (s.charAt(0) == '-') {
		negative = true;
		limit = Long.MIN_VALUE;
		i++;
	    } else {
		limit = -Long.MAX_VALUE;
	    }
          if (radix == 10) {
              multmin = negative ? MULTMIN_RADIX_TEN : N_MULTMAX_RADIX_TEN;
          } else {
              multmin = limit / radix;
          }
          if (i < max) {
              digit = Character.digit(s.charAt(i++),radix);
		if (digit < 0) {
		    throw new Exception();
		} else {
		    result = -digit;
		}
	    }
	    while (i < max) {
		// Accumulating negatively avoids surprises near MAX_VALUE
		digit = Character.digit(s.charAt(i++),radix);
		if (digit < 0) {
		    throw new Exception();
		}
		if (result < multmin) {
		    throw new Exception();
		}
		result *= radix;
		if (result < limit + digit) {
		    throw new Exception();
		}
		result -= digit;
	    }
	} else {
	    throw new Exception();
	}
	if (negative) {
	    if (i > 1) {
		return result;
	    } else {	/* Only got "-" */
		throw new Exception();
	    }
	} else {
	    return -result;
	}
  }

}
