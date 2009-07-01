/**
 * @author xrm0
 * 
 */
public class Stats {
	private int n;

	private long start = now();

	private long users;

	private long anons;

	private long pages;

	public void next() {
		final long WAIT = 5000;
		n += 1;
		long now = now();
		if (start + WAIT < now) {
			System.out.print(String.format("%d\t%d\t%d\t%f\r", pages, anons,
					users, (n + 0.0) / WAIT * 1000));
			n = 0;
			start = now;
		}
	}

	private long now() {
		return System.currentTimeMillis();
	}

	public void incCounters(String name) {
		if (name == "page")
			pages += 1;
		else if (name == "ip")
			anons += 1;
		else if (name == "username")
			users += 1;
	}

}
