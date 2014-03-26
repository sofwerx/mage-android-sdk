package mil.nga.giat.mage.sdk.datastore.observation;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "attachments")
public class Attachment {

	@DatabaseField(generatedId = true)
	private Long pk_id;

	@DatabaseField(unique = true)
	private String remote_id;

	@DatabaseField
	private String content_type;

	@DatabaseField
	private Long size;

	@DatabaseField
	private String name;

	@DatabaseField
	private String local_path;

	@DatabaseField
	private String remote_path;

	@DatabaseField(foreign = true)
	private Observation observation;

	public Attachment() {
		// ORMLite needs a no-arg constructor
	}

	public Attachment(String pContentType, Long pSize, String pName, String pLocalPath, String pRemotePath) {
		this.content_type = pContentType;
		this.size = pSize;
		this.name = pName;
		this.local_path = pLocalPath;
		this.remote_path = pRemotePath;
	}

	public Long getPk_id() {
		return pk_id;
	}

	public String getRemote_id() {
		return remote_id;
	}

	public void setRemote_id(String remote_id) {
		this.remote_id = remote_id;
	}

	public String getContent_type() {
		return content_type;
	}

	public void setContent_type(String content_type) {
		this.content_type = content_type;
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

	public String getLocal_path() {
		return local_path;
	}

	public void setLocal_path(String local_path) {
		this.local_path = local_path;
	}

	public String getRemote_path() {
		return remote_path;
	}

	public void setRemote_path(String remote_path) {
		this.remote_path = remote_path;
	}

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	@Override
	public String toString() {
		return "Attachment [pk_id=" + pk_id + ", content_type=" + content_type + ", size=" + size + ", name=" + name + ", local_path=" + local_path + ", remote_path=" + remote_path + ", remote_id=" + remote_id + ", observation=" + observation.getPk_id() + "]";
	}

}
