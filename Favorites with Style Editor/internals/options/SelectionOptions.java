package options;

public class SelectionOptions {
	public boolean[] allowed_channels = new boolean[16];
	public boolean allowed_active = true;

	public SelectionOptions() {
		for(int i=0;i<allowed_channels.length;i+=1)
			allowed_channels[i] = true;
	}
}
