
public class Rational implements Comparable<Rational> {

	/*
	 * Fraction shape components : p/q.
	 */
	private Integer p;
	private Integer q;
	
	/*
	 * Constructor that initialize new fraction.
	 * In case of negative number, it always taking care the sign is at p.
	 * If q is equal to zero, the number will be infinity of -infinity (decided by the sign).
	 */
	public Rational(Integer p, Integer q) {
		this.q = Math.abs(q);
		if((p >= 0 && q >= 0) || (p < 0 && q < 0)) {
			this.p = Math.abs(p);
		}
		else {
			this.p = -Math.abs(p);
		}
		
		if(q == 0) {
			if(p >= 0) {
				this.p = Integer.MAX_VALUE;
			}
			else {
				this.p = Integer.MIN_VALUE;
			}
			this.q = 1;
		}
		
		Integer gcd = Rational.gcd(this.p, this.q);
		this.p /= gcd;
		this.q /= gcd;
	}
	
	public Rational(Integer number) {
		this(number, 1);
	}
	
	/*
	 * Return infinity.
	 */
	public static Rational infinity() {
		return new Rational(Integer.MAX_VALUE, 1);
	}
	
	/*
	 * Return the negative of this fraction.
	 */
	public Rational negative() {
		return new Rational(-this.p, this.q);
	}
	
	/*
	 * Return the reciprocal of this fraction.
	 */
	public Rational reciprocal() {
		return new Rational(this.q, this.p);
	}
	
	/*
	 * Check if two fractions are equals.
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		Boolean result = false;
		if(obj instanceof Rational) {
			Rational r = (Rational)obj;
			result = (this.p.equals(r.p)) && (this.q.equals(r.q));
		}
		return result;
	}
	
	/*
	 * Find the greatest common divisor of two numbers.
	 */
	private static Integer gcd(Integer a, Integer b) {
		Integer t;
		if(a > b) {
			t = b;
			b = a;
			a = t;
		}
		while(b != 0) {
			t = a % b;
			a = b;
			b = t;
		}
		return Math.abs(a);
	}
	
	/*
	 * Multiply this fraction with given one and return the result.
	 */
	public Rational multiply(Rational r) {
		Rational result = new Rational(this.p * r.p, this.q * r.q);
		Integer gcd = Rational.gcd(result.p, result.q);
		result.p /= gcd;
		result.q /= gcd;
		return result;
	}
	
	/*
	 * Divide this fraction with given one and return the result.
	 */
	public Rational divide(Rational r) {
		return this.multiply(new Rational(r.q, r.p));
	}
	
	/*
	 * Add given fraction to this one and return the result.
	 */
	public Rational add(Rational r) {
		Integer rp = r.q * this.p + r.p * this.q;
		Integer rq = this.q * r.q;
		Integer gcd = Rational.gcd(rp, rq);
		rp /= gcd;
		rq /= gcd;
		return new Rational(rp, rq);
	}
	
	/*
	 * Subtract given fraction from this one and return the result.
	 */
	public Rational subtract(Rational r) {
		return this.add(r.negative());
	}
	
	/*
	 * Get string representation of this fraction.
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(p == 0) {
			return "+0";
		}
		Double div = this.p.doubleValue() / this.q.doubleValue();
		if(this.p / this.q == div) {
			Integer z = this.p / this.q;
			if(z > 0) {
				return "+" + z;
			}
			return z.toString();
		}
		String sign = (this.p > 0) ? ("+") : ("-");
		return sign + Math.abs(this.p) + "/" + Math.abs(this.q); 
	}
	
	/*
	 * Parse string to fraction.
	 */
	public static Rational parseRational(String str) {
		Integer slashIndex = str.indexOf("/");
		if(slashIndex == -1) {
			try {
				return new Rational(Integer.parseInt(str), 1);
			}
			catch(NumberFormatException e) {
				return null;
			}
		}
		else {
			String[] pq = str.split("[/]");
			if(pq.length == 2) {
				try {
					return new Rational(Integer.parseInt(pq[0]), Integer.parseInt(pq[1]));
				}
				catch(NumberFormatException e) {
					return null;
				}
			}
		}
		return null;
	}

	/*
	 * Compare between two fractions.
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Rational r) {
		
		if(this.equals(Rational.infinity()) && r.equals(Rational.infinity())) {
			return 0;
		}
		else if(this.equals(Rational.infinity().negative()) && r.equals(Rational.infinity().negative())) {
			return 0;
		}
		else if(this.equals(Rational.infinity()) || r.equals(Rational.infinity().negative())) {
			return 1;
		}
		else if(this.equals(Rational.infinity().negative()) || r.equals(Rational.infinity())) {
			return -1;
		}
		
		Rational c = this.subtract(r);
		if(c.p > 0) {
			return 1;
		}
		else if(c.p < 0) {
			return -1;
		}
		return 0;
	}
}
