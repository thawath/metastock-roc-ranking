package com.metastock.rocranker;

import java.util.Date;

/**
 * Bean representing one MASTER/XMASTER header entry (symbol -> data file mapping).
 */
public class HeaderMetaStock {

	private String name;
	private Date beginDate;
	private Date endDate;
	private String filename;
	private String master;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMaster() {
		return master;
	}

	public void setMaster(String master) {
		this.master = master;
	}
}
