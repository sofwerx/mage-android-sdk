package mil.nga.giat.mage.sdk.datastore.observation;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "attachments")
public class Attachment {

	@DatabaseField(generatedId = true, columnName="pk_id")
	private Long id;

	@DatabaseField(unique = true, columnName="remote_id")
	private String remoteId;

	@DatabaseField(columnName="content_type")
	private String contentType;

	@DatabaseField(columnName="size")
	private Long size;

	@DatabaseField(columnName="name")
	private String name;

	@DatabaseField(columnName="local_path")
	private String localPath;

	@DatabaseField(columnName="remote_path")
	private String remotePath;

	@DatabaseField(foreign = true)
	private Observation observation;

	public Attachment() {
		// ORMLite needs a no-arg constructor
	}

	public Attachment(String pContentType, Long pSize, String pName, String pLocalPath, String pRemotePath) {
		this.contentType = pContentType;
		this.size = pSize;
		this.name = pName;
		this.localPath = pLocalPath;
		this.remotePath = pRemotePath;
	}

	public Long getId() {
		return id;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	@Override
	public String toString() {
		return "Attachment [pk_id=" + id + ", content_type=" + contentType + ", size=" + size + ", name=" + name + ", local_path=" + localPath + ", remote_path=" + remotePath + ", remote_id=" + remoteId + ", observation=" + observation.getId() + "]";
	}

}
