package mil.nga.giat.mage.sdk.datastore.staticfeature;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "staticfeatures")
public class StaticFeature implements Comparable<StaticFeature> {

	@DatabaseField(generatedId = true)
	private Long id;

	@DatabaseField(unique = true, columnName = "remote_id")
	private String remoteId;

	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private StaticFeatureGeometry staticFeatureGeometry;

	public StaticFeature() {
		// ORMLite needs a no-arg constructor
	}

	public StaticFeature(StaticFeatureGeometry observationGeometry) {
		this(null, observationGeometry);
	}

	public StaticFeature(String remoteId, StaticFeatureGeometry observationGeometry) {
		super();
		this.remoteId = remoteId;
		this.staticFeatureGeometry = observationGeometry;
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

	public StaticFeatureGeometry getStaticFeatureGeometry() {
		return staticFeatureGeometry;
	}

	public void setStaticFeatureGeometry(StaticFeatureGeometry staticFeatureGeometry) {
		this.staticFeatureGeometry = staticFeatureGeometry;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int compareTo(StaticFeature another) {
		return new CompareToBuilder().append(this.id, another.id).append(this.remoteId, another.remoteId).toComparison();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((remoteId == null) ? 0 : remoteId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StaticFeature other = (StaticFeature) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(id, other.id).append(remoteId, other.remoteId).isEquals();
	}

}