package mil.nga.giat.mage.sdk.datastore.layer;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "layers")
public class Layer {

	@DatabaseField(generatedId = true)
	private Long id;

	@DatabaseField(unique = true, columnName = "remote_id")
	private Long remoteId;

	@DatabaseField
	private String type;

	@DatabaseField
	private String name;

	public Layer(Long remoteId, String type, String name) {
		super();
		this.remoteId = remoteId;
		this.type = type;
		this.name = name;
	}

	public Long getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(Long remoteId) {
		this.remoteId = remoteId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
