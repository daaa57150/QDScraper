package daaa.qdscraper.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Platform {
	
	/** id of the platform on the corresponding API */
	private String id;
	
	/** name of the platform on the corresponding API */
	private String name;
	//private String alternate; => slug version from igdb


	/** Default constructor */
	public Platform() {
		super();
	}

	/** Constructor */
	public Platform(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
