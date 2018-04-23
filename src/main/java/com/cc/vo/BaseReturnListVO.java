package com.cc.vo;

import java.util.ArrayList;
import java.util.List;

public class BaseReturnListVO {
	private String returnCode;
	private List<BaseReturnInfoVO> baseReturnInfoVOs = new ArrayList<>();
	private int size = 0;

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public List<BaseReturnInfoVO> getBaseReturnInfoVOs() {
		return baseReturnInfoVOs;
	}

	public void setBaseReturnInfoVOs(List<BaseReturnInfoVO> baseReturnInfoVOs) {
		this.baseReturnInfoVOs = baseReturnInfoVOs;
		this.size = baseReturnInfoVOs.size();
	}
	
	public int getSize() {
		return size;
	}
	
	public void addBaseReturnInfoVO(BaseReturnInfoVO baseReturnInfoVO) {
		this.baseReturnInfoVOs.add(baseReturnInfoVO);
		size++;
	}
}
