package com.cc.vo;

/**
 * 以下返回类型封装类
 * <ID Return="1(0)" status="错误代码">对象ID</ID>
 */
public class BaseReturnInfoVO {

	private String id;
	private String returnCode;
	private String status;	
	
	private String mac;	
	
	private String cdid;	
	
	private String serverIp;	
	
	private String pemData;	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getCdid() {
		return cdid;
	}

	public void setCdid(String cdid) {
		this.cdid = cdid;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getPemData() {
		return pemData;
	}

	public void setPemData(String pemData) {
		this.pemData = pemData;
	}

	public void initInfo(String returnCode) {
		this.returnCode = returnCode;
	}
	
	public void initInfo(String returnCode, String status) {
		this.returnCode = returnCode;
		this.status = status;
	}
	
	public void initInfo(String returnCode, String status,String mac,String cdid,String serverIp,String pemData) {
		this.returnCode = returnCode;
		this.status = status;
		this.mac = mac;
		this.cdid = cdid;
		this.serverIp = serverIp;
		this.pemData = pemData;
	}

}
