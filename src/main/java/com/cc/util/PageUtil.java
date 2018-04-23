package com.cc.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * spring分页工具类
 *
 * @author chencheng0816@gmail.com
 * @date 2018年4月23日 下午4:29:32
 */
public class PageUtil<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int DEF_COUNT = 20;

	protected int pageNo = 1;       // 页码
	protected int pageSize = 20;    // 每页最大记录数
	protected int totalCount = 0;   // 记录总数
	private List<T> list;           // 当前页的记录

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		if (pageNo < 1) {
			this.pageNo = 1;
		} else {
			this.pageNo = pageNo;
		}
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		if (pageSize < 1) {
			this.pageSize = DEF_COUNT;
		} else {
			this.pageSize = pageSize;
		}
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		if (totalCount < 0) {
			this.totalCount = 0;
		} else {
			this.totalCount = totalCount;
		}
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public PageUtil() {
	}

	public PageUtil(int pageNo, int pageSize, int totalCount) {
		setTotalCount(totalCount);
		setPageSize(pageSize);
		setPageNo(pageNo);
		adjustPageNo();
	}

	public PageUtil(int pageNo, int pageSize, List<T> list) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.list = list;
	}

	public PageUtil(int pageNo, int pageSize, int totalCount, List<T> list) {
		this(pageNo, pageSize, totalCount);
		this.list = list;
	}

	/**
	 * 初始化时，页码不得超过总页数
	 */
	public void adjustPageNo() {
		if (pageNo == 1) {
			return;
		}
		int tp = getTotalPage();
		if (pageNo > tp) {
			pageNo = tp;
		}
	}

	/**
	 * 总共几页
	 */
	public int getTotalPage() {
		int totalPage = totalCount / pageSize;
		if (totalPage == 0 || totalCount % pageSize != 0) {
			totalPage++;
		}
		return totalPage;
	}

	/**
	 * 是否为第一页
	 */
	public boolean isFirstPage() {
		return pageNo <= 1;
	}

	/**
	 * 是否为最后一页
	 */
	public boolean isLastPage() {
		return pageNo >= getTotalPage();
	}

	/**
	 * 获取下一页页码
	 */
	public int getNextPage() {
		if (isLastPage()) {
			return pageNo;
		} else {
			return pageNo + 1;
		}
	}

	/**
	 * 获取上一页页码
	 */
	public int getPrePage() {
		if (isFirstPage()) {
			return pageNo;
		} else {
			return pageNo - 1;
		}
	}

	/**
	 * 获取当前页第一条记录id
	 */
	public int getFirstResult() {
		return (pageNo - 1) * pageSize;
	}

	/**
	 * 获取指定页码第一条记录id
	 * 
	 * @param pageNo 页码
	 * @param pageSize 每页记录量
	 * @return
	 */
	public static Integer getStartIndex(Integer pageNo, Integer pageSize) {
		Integer startIndex = 0;
		if (pageNo == null) {
			startIndex = 0;
		} else {
			startIndex = (pageNo - 1) * pageSize;
		}
		return startIndex;
	}

	/**
	 * 设置页面信息到head
	 */
	public void setHead() {
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
		response.setIntHeader("pageNo", getPageNo());
		response.setIntHeader("pageSize", getPageSize());
		response.setIntHeader("totalCount", getTotalCount());
	}

	/**
	 * 返回页面统计信息
	 */
	public Map<String, Object> getResultMap() {
		setHead();
		Map<String, Object> resultMap = new HashMap<>(3);
		resultMap.put("pageNo", getPageNo());
		resultMap.put("pageSize", getPageSize());
		resultMap.put("totalCount", getTotalCount());
		return resultMap;
	}

	/**
	 * 返回页面详细信息
	 */
	public Map<String, Object> getResultMapWithList(String key) {
		Map<String, Object> resultMap = getResultMap();
		resultMap.put(key, getList());
		return resultMap;
	}
}
