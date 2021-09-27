package generator.fragments;

import java.util.ArrayList;

public class FragmentResolutionInformations {

	private FragmentGenerationType type;
	private ArrayList<Fragment> fragments;
	private boolean enabled;
	
	public FragmentResolutionInformations(FragmentGenerationType type, ArrayList<Fragment> fragments) {
		this.type = type;
		this.fragments = fragments;
		enabled = true;
	}

	public FragmentGenerationType getType() {
		return type;
	}

	public ArrayList<Fragment> getFragments() {
		return fragments;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
