package de.otto.flummi.response;

public class PingResponse {

	private String clusterName;
	private String name;
	private String tagline;
	private String buildHash;
	private boolean buildSnapshot;
	private String buildTimestamp;
	private String lucineVerion;
	private String number;

	public PingResponse(String clusterName, String name, String tagline, String buildHash, boolean buildSnapshot,
			String buildTimestamp, String lucineVerion, String number) {
		this.clusterName = clusterName;
		this.name = name;
		this.tagline = tagline;
		this.buildHash = buildHash;
		this.buildSnapshot = buildSnapshot;
		this.buildTimestamp = buildTimestamp;
		this.lucineVerion = lucineVerion;
		this.number = number;
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getName() {
		return name;
	}

	public String getTagline() {
		return tagline;
	}

	public String getBuildHash() {
		return buildHash;
	}

	public boolean isBuildSnapshot() {
		return buildSnapshot;
	}

	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	public String getLucineVerion() {
		return lucineVerion;
	}

	public String getNumber() {
		return number;
	}

}
