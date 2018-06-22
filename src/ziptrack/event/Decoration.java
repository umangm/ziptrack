package ziptrack.event;

// Abstract class for Lock, Variable and Thread.
public abstract class Decoration {
	protected int id;
	protected String name;

	public String getName() {
		return this.name;
	}

	public String toString() {
		return getName();
	}

}
