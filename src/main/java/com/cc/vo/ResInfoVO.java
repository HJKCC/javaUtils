package com.cc.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回前端
 *
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午6:29:50
 */
public class ResInfoVO {
	private String code;
	private String info;
	private Object object;
	private List list = new ArrayList<>();
	private Integer size;
	public String getResultCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public Object getObject() {
		return object;
	}
	public void setObject(Object object) {
		this.object = object;
	}
	public List getList() {
		return list;
	}
	public void setList(List list) {
		this.list = list;
		this.size = list.size();
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public ResInfoVO() {
		super();
		this.code = "1";
		this.info = "操作成功";
	}	
	public ResInfoVO(String code) {
		super();
		this.code = "1";
		this.info = "操作成功";
	}	
	public ResInfoVO(String code, String info) {
		super();
		this.code = code;
		this.info = info;
	}
}
