package com.cc.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cc.vo.BaseReturnInfoVO;
import com.cc.vo.BaseReturnListVO;
import com.cc.vo.ResInfoVO;

/**
 * 通过socket调用服务端接口
 * 
 * @author chencheng0816@gmail.com 
 * @date 2016年10月28日 上午10:02:43
 */
public class SocketClientUtils {
	//服务端默认编码
	private static final String CHARSET_GBK = "GBK";
	//处理字符数组编码
	private static final String CHARSET_ISO = "ISO-8859-1";
	//操作成功
	private static final String SUCCESS = "1";
	
	private static final Logger logger = LoggerFactory.getLogger(SocketClientUtils.class);
	private static final long TIME_OUT = 30*60*1000; // 最长等待30m
	private static final int RESULT_COUNT = 1; // 默认成功记录条数
	private static String SELECTOR_ERROR = "没有可用的连接就绪通道"; // 最长等待30m
	private static byte[] HEAD_BYTES = "DAEH_ATAD_PMDC".getBytes();

	public static List<String> postFromServer(String ip, int port, String dataXml, int netWorkNum) {
		return postFromServer(ip, port, dataXml, netWorkNum, TIME_OUT);
	}

	/**
	 * 带数据的请求(外部请求)
	 * 
	 * @param ip
	 * @param port
	 * @param dataXml
	 * @param netWorkNum
	 * @return
	 */
	public static List<String> postFromServer(String ip, int port, String dataXml, int netWorkNum, long timeOut) {
		logger.debug("用户ID: " + SessionUtil.getUserID() + ", 请求信息如下: " + "\n"
				+ "请求commandID: " + netWorkNum + "\n"
				+ "请求xml数据长度: " + dataXml.length() + "\n"
				+ "请求xml数据: " + "\n" + dataXml + "\n");
		List<String> list = new ArrayList<String>();
		String dwFlag = "-99999";
		String result = "";
		Selector selector = null;
		SocketChannel connectChannel = null;
		SocketChannel readChannel = null;
		try {			
			InetSocketAddress address = new InetSocketAddress(ip, port); // 创建socket
			long start = System.currentTimeMillis();
			// 打开Socket通道
			connectChannel = SocketChannel.open();
			// 设置为非阻塞模式
			connectChannel.configureBlocking(false);
			// 打开选择器
			selector = Selector.open();
			// 注册connectChannel通道为“连接就绪”
			connectChannel.register(selector, SelectionKey.OP_CONNECT);
			// 连接
			connectChannel.connect(address);		
			
			boolean flag = true;
			int offset = 0;
			byte[] tempBuffer = new byte[0];
			int commandID = -1; // 返回网络ID
			int dwFlag1 = -1; // 附加标记位1
			int dwFlag2 = -1; // 附加标记位2
			int respXmlLength = 0;
			_FORLABEL: for (;;) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				selector.select(timeOut); // 最长等待8s
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				if (!keyIterator.hasNext()) {
					System.out.println(SELECTOR_ERROR);
					break _FORLABEL;
				}
				
				_SELECTIONKEY: while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					if (key.isConnectable()) { // a connection was established with a remote server.						
						readChannel = (SocketChannel) key.channel();
						keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
						if (readChannel.isConnectionPending()) {// 调用 isConnectionPending 方法来确定是否正在进行连接操作
							try {
								readChannel.finishConnect(); // 调用  finishConnect 方法来确定完成该连接
								System.out.println("成功连接到服务器");
							} catch (ConnectException e) {
								logger.debug("连接服务器失败，请联系管理员");
								dwFlag = "-99998";
								// e.printStackTrace();
								break _SELECTIONKEY;
							}
						}
						byte[] headBytes = "DAEH_ATAD_PMDC".getBytes(); // 数据头，DAEH_ATAD_PMDC（CDMP_DATA_HEAD反序）（作用可以忽略掉非客户端的连接通信）
						byte[] netWorkNumBytes = TypeConvertUtils.int2byte(netWorkNum);
						byte[] xmlBytes = dataXml.getBytes(CHARSET_GBK);
						int headBytesLength = headBytes.length;
						int netWorkNumBytesLength = netWorkNumBytes.length;
						int xmlBytesLength = xmlBytes.length;

						byte[] dataBytes = new byte[20 + xmlBytesLength];
						System.arraycopy(netWorkNumBytes, 0, dataBytes, 0, netWorkNumBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(xmlBytesLength), 0, dataBytes, 4, 4);
						System.arraycopy(xmlBytes, 0, dataBytes, 16, xmlBytesLength);
						int dataBytesLength = dataBytes.length;

						byte[] reqBytes = new byte[32 + dataBytesLength];
						System.arraycopy(headBytes, 0, reqBytes, 0, headBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(dataBytesLength), 0, reqBytes, 20, 4);
						System.arraycopy(dataBytes, 0, reqBytes, 28, dataBytesLength);

						readChannel.write(ByteBuffer.wrap(reqBytes)); // 将字节序列从给定的缓冲区写入此通道

						readChannel.register(selector, SelectionKey.OP_READ); // 注册readChannel通道为“读就绪”
						SELECTOR_ERROR = "服务端无返回或返回超时，请稍后";
					} else if (key.isReadable()) { // a channel is ready for reading
						// 分配内存
						ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
						
						readChannel = (SocketChannel) key.channel();
						int count = readChannel.read(buffer);
						if (count == 0) {
							continue _SELECTIONKEY;
						} else if (count == -1) {
							keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
							break _FORLABEL;
						}
						buffer.flip();
						
						byte[] respBytes = new byte[offset + count];  //存储本次读取数据
						if (offset != 0) {
							System.arraycopy(tempBuffer, 0, respBytes, 0, offset);
						}
						System.arraycopy(buffer.array(), 0, respBytes, offset, count);
						offset = 0;
						
						int srcPos = 14;
						String[] respss = new String(respBytes, CHARSET_ISO).split("DAEH_ATAD_PMDC");
						/*
						logger.debug("用户ID: " + SessionUtil.getUserID() + ", 服务器返回完整信息如下: " + "\n"
								+ "请求commandID: " + netWorkNum + "\n"
								+ "返回数据长度: " + count + "\n"
								+ "返回数据:" + "\n" + new String(buffer.array(), CHARSET_ISO).substring(0, count) + "\n");
						*/
						int respLength = respss.length;
						int tempRespLength = 0;
						byte[] tempRespBytes = null;
						for (int i = 1; i < respLength && flag; i++) {
							String tempResps = respss[i];
							tempRespLength = tempResps.length() + 14;
							tempRespBytes = new byte[tempRespLength];
							System.arraycopy(HEAD_BYTES, 0, tempRespBytes, 0, 14);
							System.arraycopy(respBytes, srcPos, tempRespBytes, 14, tempRespLength - 14);
							srcPos += tempRespLength;
							//解析返回包信息
							if (tempRespLength > 44) {
								byte[] paramBytes = new byte[4];
								
								System.arraycopy(tempRespBytes, 28, paramBytes, 0, 4);
								commandID = TypeConvertUtils.byte2int(paramBytes);									
								if (commandID == 10000 || commandID == 0) {
									logger.debug("收到服务器连接确认通知！！");
									continue;
								}

								System.arraycopy(tempRespBytes, 32, paramBytes, 0, 4);
								respXmlLength = TypeConvertUtils.byte2int(paramBytes);

								System.arraycopy(tempRespBytes, 36, paramBytes, 0, 4);
								dwFlag1 = TypeConvertUtils.byte2int(paramBytes);

								System.arraycopy(tempRespBytes, 40, paramBytes, 0, 4);
								dwFlag2 = TypeConvertUtils.byte2int(paramBytes);
								
								flag = false;
							}
							
							if (tempRespLength < respXmlLength + 44) {
								if (i == respLength - 1) {
									tempBuffer = tempRespBytes;
									offset = tempRespLength;
									continue _SELECTIONKEY;
								} else {
									SELECTOR_ERROR = "服务端返回结果有误";
									continue;
								}
							} else {
								respBytes = tempRespBytes;
							}
						}		
						
						if (flag) {
							continue _SELECTIONKEY;
						}
						
						if (respBytes.length < respXmlLength + 44) {
							tempBuffer = respBytes;
							offset = respBytes.length;
							continue _SELECTIONKEY;
						}

						byte[] respXmlBytes = new byte[respXmlLength];
						System.arraycopy(respBytes, 44, respXmlBytes, 0, respXmlLength);
						result = new String(respXmlBytes, CHARSET_GBK); // 返回xml数据
						logger.debug("用户ID: " + SessionUtil.getUserID() + ", 接收信息如下: " + "\n"
								+ "返回commandID: " + commandID + "\n"
								+ "附加标记位1: " + dwFlag1 + "\n"
								+ "附加标记位2: " + dwFlag2 + "\n"
								+ "返回xml数据长度: " + respXmlLength + "\n"
								+ "返回xml数据: " + "\n" + result + "\n");
						if (dwFlag1 == 1 && dwFlag2 == 0) {
							dwFlag = SUCCESS;
						} if (dwFlag1 != 1 || dwFlag2 != 0) {
							dwFlag = String.valueOf(dwFlag2);
						}
						list.add(dwFlag);
						list.add(result);

						if (count > 0) {
							buffer.clear();
							keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
							break _FORLABEL;
						}
					}
				}
			}
			double last = (System.currentTimeMillis() - start) * 1.0 / 1000;
			System.out.println("used time :" + last + "s.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (connectChannel != null) {
				try {
					connectChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
			if (readChannel != null) {
				try {
					readChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	public static ResInfoVO postFromServer2(String ip, int port, String dataXml, int netWorkNum, String className, String methodName) {
		return postFromServer2(ip, port, dataXml, netWorkNum, className, methodName, RESULT_COUNT, TIME_OUT);
	}
	
	/**
	 * 带数据的请求(外部请求),处理多次返回情况
	 * @param ip
	 * @param port
	 * @param dataXml
	 * @param netWorkNum
	 * @param className
	 * @param methodName
	 * @param resultCount
	 * @return
	 */
	public static ResInfoVO postFromServer2(String ip, int port, String dataXml, int netWorkNum, String className, String methodName, int resultCount) {
		return postFromServer2(ip, port, dataXml, netWorkNum, className, methodName, resultCount, TIME_OUT);
	}

	/**
	 * 带数据的请求(外部请求),处理多次返回情况
	 * @param ip 服务所在IP地址
	 * @param port 服务所在端口
	 * @param dataXml 请求数据
	 * @param netWorkNum 请求网络ID
	 * @param className xml解析完整类名
	 * @param methodName xml解析方法名
	 * @param resultCount 成功记录条数
	 * @param timeOut 等待时间
	 * @return
	 */
	public static ResInfoVO postFromServer2(String ip, int port, String dataXml, int netWorkNum, String className, String methodName, int resultCount, long timeOut) {
		logger.debug("用户ID: " + SessionUtil.getUserID() + ", 请求信息如下: " + "\n"
				+ "请求commandID: " + netWorkNum + "\n"
				+ "请求xml数据长度: " + dataXml.length() + "\n"
				+ "请求xml数据: " + "\n" + dataXml + "\n");
		ResInfoVO resInfoVO = new ResInfoVO(SUCCESS);
		String dwFlag = "-99999";
		String result = "";
		Selector selector = null;
		SocketChannel connectChannel = null;
		SocketChannel readChannel = null;
		try {
			List<BaseReturnInfoVO> baseReturnInfoVOs = new ArrayList<>();
			
			InetSocketAddress address = new InetSocketAddress(ip, port); // 创建socket
			long start = System.currentTimeMillis();
			// 打开Socket通道
			connectChannel = SocketChannel.open();
			// 设置为非阻塞模式
			connectChannel.configureBlocking(false);
			// 打开选择器
			selector = Selector.open();
			// 注册connectChannel通道为“连接就绪”
			connectChannel.register(selector, SelectionKey.OP_CONNECT);
			// 连接
			connectChannel.connect(address);			

			int returnCount = 0;
			_FORLABEL: for (;;) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				selector.select(timeOut); // 最长等待8s
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				if (!keyIterator.hasNext()) {
					System.out.println(SELECTOR_ERROR);
					break _FORLABEL;
				}
				
				int offset = 0;
				byte[] tempBuffer = new byte[0];
				int commandID = -1; // 返回网络ID
				int dwFlag1 = -1; // 附加标记位1
				int dwFlag2 = -1; // 附加标记位2
				int respXmlLength = 0;
				_SELECTIONKEY: while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();					
					if (key.isConnectable()) { // a connection was established with a remote server.
						keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
						readChannel = (SocketChannel) key.channel();
						if (readChannel.isConnectionPending()) {// 调用 isConnectionPending 方法来确定是否正在进行连接操作
							try {
								readChannel.finishConnect(); // 调用  finishConnect 方法来确定完成该连接
								System.out.println("成功连接到服务器");
							} catch (ConnectException e) {
								logger.debug("连接服务器失败，请联系管理员");
								dwFlag = "-99998";
								resInfoVO.setCode("-99998");
								// e.printStackTrace();
								break _SELECTIONKEY;
							}
						}
						byte[] headBytes = "DAEH_ATAD_PMDC".getBytes(); // 数据头，DAEH_ATAD_PMDC（CDMP_DATA_HEAD反序）（作用可以忽略掉非客户端的连接通信）
						byte[] netWorkNumBytes = TypeConvertUtils.int2byte(netWorkNum);
						byte[] xmlBytes = dataXml.getBytes(CHARSET_GBK);
						int headBytesLength = headBytes.length;
						int netWorkNumBytesLength = netWorkNumBytes.length;
						int xmlBytesLength = xmlBytes.length;

						byte[] dataBytes = new byte[20 + xmlBytesLength];
						System.arraycopy(netWorkNumBytes, 0, dataBytes, 0, netWorkNumBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(xmlBytesLength), 0, dataBytes, 4, 4);
						System.arraycopy(xmlBytes, 0, dataBytes, 16, xmlBytesLength);
						int dataBytesLength = dataBytes.length;

						byte[] reqBytes = new byte[32 + dataBytesLength];
						System.arraycopy(headBytes, 0, reqBytes, 0, headBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(dataBytesLength), 0, reqBytes, 20, 4);
						System.arraycopy(dataBytes, 0, reqBytes, 28, dataBytesLength);

						readChannel.write(ByteBuffer.wrap(reqBytes)); // 将字节序列从给定的缓冲区写入此通道

						readChannel.register(selector, SelectionKey.OP_READ); // 注册readChannel通道为“读就绪”
						SELECTOR_ERROR = "服务端无返回或返回超时，请稍后";
					} else if (key.isReadable()) { // a channel is ready for reading
						// 分配内存
						ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
						
						readChannel = (SocketChannel) key.channel();
						int count = readChannel.read(buffer);
						if (count == 0) {
							continue _SELECTIONKEY;
						} else if (count == -1) {
							keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
							break _FORLABEL;
						}
						buffer.flip();
						
						byte[] respBytes = new byte[offset + count];  //存储本次读取数据
						if (offset != 0) {
							System.arraycopy(tempBuffer, 0, respBytes, 0, offset);
						}
						System.arraycopy(buffer.array(), 0, respBytes, offset, count);
						offset = 0;
						
						int srcPos = 14;
						String[] respss = new String(respBytes, CHARSET_ISO).split("DAEH_ATAD_PMDC");					
						int respLength = respss.length;
						int tempRespLength = 0;
						byte[] tempRespBytes = null;
						for (int i = 0; i < respLength; i++) {
							String tempResps = respss[i];
							if (tempResps.length() == 0) {
								continue;
							}
							tempRespLength = tempResps.length() + 14;
							tempRespBytes = new byte[tempRespLength];
							System.arraycopy(HEAD_BYTES, 0, tempRespBytes, 0, 14);
							System.arraycopy(respBytes, srcPos, tempRespBytes, 14, tempRespLength - 14);
							srcPos += tempRespLength;
							
							//解析返回包信息
							if (tempRespLength > 44) {
								byte[] paramBytes = new byte[4];

								System.arraycopy(tempRespBytes, 28, paramBytes, 0, 4);
								commandID = TypeConvertUtils.byte2int(paramBytes);
								if (commandID == 10000) {
									logger.debug("收到服务器连接确认通知！！");
									continue;
								}

								System.arraycopy(tempRespBytes, 32, paramBytes, 0, 4);
								respXmlLength = TypeConvertUtils.byte2int(paramBytes);

								System.arraycopy(tempRespBytes, 36, paramBytes, 0, 4);
								dwFlag1 = TypeConvertUtils.byte2int(paramBytes);

								System.arraycopy(tempRespBytes, 40, paramBytes, 0, 4);
								dwFlag2 = TypeConvertUtils.byte2int(paramBytes);
							}
							
							if (tempRespLength < respXmlLength + 52) {
								if (i == respLength - 1) {
									tempBuffer = tempRespBytes;
									offset = tempRespLength;
									continue _SELECTIONKEY;
								} else {
									SELECTOR_ERROR = "服务端返回结果有误";
									continue;
								}
							} else if (tempRespLength > respXmlLength + 52) {
								if (i == respLength - 1) {
									tempBuffer = tempRespBytes;
									offset = tempRespLength;
									continue _SELECTIONKEY;
								}
							}

							byte[] respXmlBytes = new byte[respXmlLength];
							System.arraycopy(tempRespBytes, 44, respXmlBytes, 0, respXmlLength);
							result = new String(respXmlBytes, CHARSET_GBK); // 返回xml数据
							logger.debug("用户ID: " + SessionUtil.getUserID() + ", 接收信息如下: " + "\n"
									+ "返回commandID: " + commandID + "\n"
									+ "附加标记位1: " + dwFlag1 + "\n"
									+ "附加标记位2: " + dwFlag2 + "\n"
									+ "返回xml数据长度: " + respXmlLength + "\n"
									+ "返回xml数据: " + "\n" + result + "\n");
							if (dwFlag1 == 1 && (dwFlag2 == 0  || dwFlag2 == -1 || dwFlag2 == 1061109567)) {
								dwFlag = SUCCESS;
								Class<?> c;
								try {
									c = Class.forName(className);
									Method method = c.getMethod(methodName, new Class[]{String.class});
									BaseReturnListVO vo = (BaseReturnListVO) method.invoke(null, new Object[]{result});
									returnCount += vo.getSize();
									for(BaseReturnInfoVO v : vo.getBaseReturnInfoVOs()){
										if(CommonUtil.isEmpty(v.getReturnCode()) || v.getReturnCode().equals(SUCCESS)){
											baseReturnInfoVOs.add(v);
										}
									}
								} catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (NoSuchMethodException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (SecurityException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalArgumentException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							if (count > 0) {
								if (returnCount < resultCount) {
									continue;
								} else {
									keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
									break _FORLABEL;
								}
							}
						}	
					}
				}
			}
			resInfoVO.setList(baseReturnInfoVOs);
			double last = (System.currentTimeMillis() - start) * 1.0 / 1000;
			System.out.println("used time :" + last + "s.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (connectChannel != null) {
				try {
					connectChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
			if (readChannel != null) {
				try {
					readChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return resInfoVO;
	}

	public static List<String> postFromServer1(String ip, int port, String dataXml, int netWorkNum) {
		return postFromServer1(ip, port, dataXml, netWorkNum, TIME_OUT);
	}

	/**
	 * 带数据的请求(内部请求)
	 * 
	 * @param ip
	 * @param port
	 * @param dataXml
	 * @param netWorkNum
	 * @return
	 */
	public static List<String> postFromServer1(String ip, int port, String dataXml, int netWorkNum, long timeOut) {
		logger.debug("用户ID: " + SessionUtil.getUserID() + ", 请求信息如下: " + "\n"
				+ "请求commandID: " + netWorkNum + "\n"
				+ "请求xml数据长度: " + dataXml.length() + "\n"
				+ "请求xml数据: " + "\n" + dataXml + "\n");
		List<String> list = new ArrayList<String>();
		String dwFlag = "-99999";

		String result = "";
		Selector selector = null;
		SocketChannel connectChannel = null;
		SocketChannel readChannel = null;
		try {
			int commandID = -1; // 返回网络ID
			int dwFlag1 = -1; // 附加标记位1
			int dwFlag2 = -1; // 附加标记位2
			InetSocketAddress address = new InetSocketAddress(ip, port); // 创建socket
			long start = System.currentTimeMillis();
			// 打开Socket通道
			connectChannel = SocketChannel.open();
			// 设置为非阻塞模式
			connectChannel.configureBlocking(false);
			// 打开选择器
			selector = Selector.open();
			// 注册connectChannel通道为“连接就绪”
			connectChannel.register(selector, SelectionKey.OP_CONNECT);
			// 连接
			connectChannel.connect(address);
			// 分配内存
			ByteBuffer buffer = ByteBuffer.allocate(3 * 1024);
			ByteBuffer databuffer = ByteBuffer.allocate(100 * 1024);

			int respXmlLength = 0;
			int sum = 0;
			boolean flag = true;
			_FORLABEL: for (;;) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				selector.select(timeOut); // 最长等待8s
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				if (!keyIterator.hasNext()) {
					System.out.println(SELECTOR_ERROR);
					break;
				}
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
					if (key.isConnectable()) { // a connection was established with a remote server.
						readChannel = (SocketChannel) key.channel();
						if (readChannel.isConnectionPending()) {// 调用 isConnectionPending 方法来确定是否正在进行连接操作							
							try {
								readChannel.finishConnect(); // 调用  finishConnect 方法来确定完成该连接
								System.out.println("成功连接到服务器");
								int rr = readChannel.read(buffer);
								buffer.clear();
							} catch (ConnectException e) {
								logger.debug("连接服务器失败，请联系管理员");
								dwFlag = "-99998";
								// e.printStackTrace();
								break;
							}
						}
						byte[] headBytes = "DAEH_KCAP_RETNI".getBytes(); // 数据头，DAEH_ATAD_PMDC（CDMP_DATA_HEAD反序）（作用可以忽略掉非客户端的连接通信）
						byte[] netWorkNumBytes = TypeConvertUtils.int2byte(netWorkNum);
						byte[] xmlBytes = dataXml.getBytes(CHARSET_GBK);
						int headBytesLength = headBytes.length;
						int netWorkNumBytesLength = netWorkNumBytes.length;
						int xmlBytesLength = xmlBytes.length;

						byte[] dataBytes = new byte[20 + xmlBytesLength];
						System.arraycopy(netWorkNumBytes, 0, dataBytes, 0, netWorkNumBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(xmlBytesLength), 0, dataBytes, 4, 4);
						System.arraycopy(xmlBytes, 0, dataBytes, 16, xmlBytesLength);
						int dataBytesLength = dataBytes.length;

						byte[] reqBytes = new byte[28 + dataBytesLength];
						System.arraycopy(headBytes, 0, reqBytes, 0, headBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(dataBytesLength), 0, reqBytes, 20, 4);
						System.arraycopy(dataBytes, 0, reqBytes, 24, dataBytesLength);

						readChannel.write(ByteBuffer.wrap(reqBytes)); // 将字节序列从给定的缓冲区写入此通道

						readChannel.register(selector, SelectionKey.OP_READ); // 注册readChannel通道为“读就绪”
						SELECTOR_ERROR = "服务端无返回或返回超时，请稍后";
					} else if (key.isReadable()) { // a channel is ready for reading
						readChannel = (SocketChannel) key.channel();

						int count = readChannel.read(buffer);
						if (count == 0)
							continue;
						else if (count == -1) {
							break _FORLABEL;
						}

						sum += count;
						buffer.flip();
						byte[] respBytes = new byte[count];
						respBytes = buffer.array(); // 返回xml结果字节码
						/*
						logger.debug("用户ID: " + SessionUtil.getUserID() + ", 服务器返回完整信息如下: " + "\n"
								+ "请求commandID: " + netWorkNum + "\n"
								+ "返回结果:" + "\n" + new String(respBytes, CHARSET_GBK) + "\n");
						*/
						databuffer.put(buffer);
						buffer.clear();

						if (sum >= 40 && flag) {
							flag = false;

							byte[] paramBytes = new byte[4];

							System.arraycopy(respBytes, 24, paramBytes, 0, 4);//80
							commandID = TypeConvertUtils.byte2int(paramBytes);

							System.arraycopy(databuffer.array(), 28, paramBytes, 0, 4);
							respXmlLength = TypeConvertUtils.byte2int(paramBytes);

							System.arraycopy(databuffer.array(), 32, paramBytes, 0, 4);
							dwFlag1 = TypeConvertUtils.byte2int(paramBytes);

							System.arraycopy(databuffer.array(), 36, paramBytes, 0, 4);
							dwFlag2 = TypeConvertUtils.byte2int(paramBytes);
						}

						if (sum < respXmlLength + 40) {
							SELECTOR_ERROR = "服务端返回结果有误";
							continue;
						}

						byte[] respXmlBytes = new byte[respXmlLength];
						System.arraycopy(databuffer.array(), 40, respXmlBytes, 0, respXmlLength);
						result = new String(respXmlBytes, CHARSET_GBK); // 返回xml数据
						logger.debug("用户ID: " + SessionUtil.getUserID() + ", 接收信息如下: " + "\n"
								+ "返回commandID: " + commandID + "\n"
								+ "附加标记位1: " + dwFlag1 + "\n"
								+ "附加标记位2: " + dwFlag2 + "\n"
								+ "返回xml数据长度: " + respXmlLength + "\n"
								+ "返回xml数据: " + "\n" + result + "\n");
						if (dwFlag1 == 1 && dwFlag2 == 0) {
							dwFlag = SUCCESS;
						} if (dwFlag1 != 1 || dwFlag2 != 0) {
							dwFlag = String.valueOf(dwFlag2);
						}
						list.add(dwFlag);
						list.add(result);

						if (count > 0) {
							buffer.clear();
							break _FORLABEL;
						}
					}
				}
			}
			double last = (System.currentTimeMillis() - start) * 1.0 / 1000;
			System.out.println("used time :" + last + "s.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (connectChannel != null) {
				try {
					connectChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
			if (readChannel != null) {
				try {
					readChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}
	
	public static String getFromServer(String ip, int port, int netWorkNum) {
		return getFromServer(ip, port, netWorkNum, TIME_OUT);
	}
	
	/**
	 * 不带数据的请求
	 * 
	 * @param ip
	 * @param port
	 * @param netWorkNum
	 * @return
	 */
	public static String getFromServer(String ip, int port, int netWorkNum, long timeOut) {
		String result = "";
		SocketChannel client = null;
		SocketChannel channel = null;
		try {
			InetSocketAddress address = new InetSocketAddress(ip, port);
			long start = System.currentTimeMillis();
			// 打开Socket通道
			client = SocketChannel.open();
			// 设置为非阻塞模式
			client.configureBlocking(false);
			// 打开选择器
			Selector selector = Selector.open();
			// 注册连接服务端socket动作
			client.register(selector, SelectionKey.OP_CONNECT);
			// 连接
			client.connect(address);
			// 分配内存
			ByteBuffer buffer = ByteBuffer.allocate(10 * 1024);
			ByteBuffer databuffer = ByteBuffer.allocate(100 * 1024);
			int datalen = 0;
			int sum = 0;
			boolean flag = true;
			_FORLABEL: for (;;) {
				selector.select(timeOut); // 最长等待8秒钟
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				if (!iter.hasNext())
					break;
				while (iter.hasNext()) {
					/*
					 * try { Thread.sleep(1); } catch (InterruptedException e) {
					 * // TODO Auto-generated catch block e.printStackTrace(); }
					 */
					SelectionKey key = iter.next();
					iter.remove();
					if (key.isConnectable()) {
						channel = (SocketChannel) key.channel();
						if (channel.isConnectionPending()) {
							channel.finishConnect();
							System.out.println("成功连接到服务器");
						}
						byte[] byteArr = new byte[52];
						byte[] byteData = new byte[20];
						byte[] byteArr1 = new byte[20];
						byte[] byteArr2 = new byte[4];
						byteArr1 = "DAEH_ATAD_PMDC".getBytes();
						byteArr2 = TypeConvertUtils.int2byte(netWorkNum);
						System.arraycopy(byteArr1, 0, byteArr, 0, 14);
						System.arraycopy(byteArr2, 0, byteData, 0, 4);
						System.arraycopy(TypeConvertUtils.int2byte(byteData.length), 0, byteArr, 20, 4);
						System.arraycopy(byteData, 0, byteArr, 28, byteData.length);
						channel.write(ByteBuffer.wrap(byteArr));
						channel.register(selector, SelectionKey.OP_READ);
					} else if (key.isReadable()) {
						channel = (SocketChannel) key.channel();
						int count = channel.read(buffer);
						if (count == 52)
							continue;
						else if (count == -1) {
							break _FORLABEL;
						}
						sum += count;
						buffer.flip();
						byte[] isOk = new byte[count];
						isOk = buffer.array();
						System.out.println(sum + "=======>" + new String(isOk));
						databuffer.put(buffer);
						buffer.clear();
						if (sum >= 88 && flag) {
							flag = false;
							byte[] byteArr = new byte[4];
							System.arraycopy(isOk, 80, byteArr, 0, 4);
							int n = TypeConvertUtils.byte2int(byteArr);
							System.out.println("返回网络id:" + n);
							logger.debug("返回网络id:" + n);
							byte[] num = new byte[4];
							System.arraycopy(databuffer.array(), 84, num, 0, 4);
							datalen = TypeConvertUtils.byte2int(num);
							System.out.println("待解析的xml数据长度:" + datalen);
							logger.debug("待解析的xml数据长度:" + datalen);
							int ret = 0;
							System.arraycopy(databuffer.array(), 88, num, 0, 4);
							ret = TypeConvertUtils.byte2int(num);
							System.out.println("附加标记位1:" + ret);
							logger.debug("附加标记位1:" + ret);
							System.arraycopy(databuffer.array(), 92, num, 0, 4);
							ret = TypeConvertUtils.byte2int(num);
							System.out.println("附加标记位2:" + ret);
							logger.debug("附加标记位2:" + ret);
						}
						if (sum < datalen + 52) {
							continue;
						}
						byte[] returnData = new byte[datalen];
						System.arraycopy(databuffer.array(), 96, returnData, 0, datalen);
						System.out.println("待解析的xml数据returnData  :" + new String(returnData));
						logger.debug("待解析的xml数据returnData  :" + new String(returnData));
						result = new String(returnData);
						if (count > 0) {
							break _FORLABEL;
						}
					}
				}
			}
			double last = (System.currentTimeMillis() - start) * 1.0 / 1000;
			System.out.println("used time :" + last + "s.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (client != null)
				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (channel != null)
				try {
					channel.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}
	
	public static List<byte[]> vnPostFromServer(String ip, int port, byte[] xmlBytes, int netWorkNum) {
		return vnPostFromServer(ip, port, xmlBytes, netWorkNum, TIME_OUT);
	}
	
	public static List<byte[]> vnPostFromServer(String ip, int port, byte[] xmlBytes, int netWorkNum, long timeOut) {
		logger.debug("请求信息如下: " + "\n"
				+ "请求commandID: " + netWorkNum + "\n"
				+ "请求xml数据长度: " + xmlBytes.length + "\n");
		List<byte[]> list = new ArrayList<byte[]>();
		String dwFlag = "-99999";

		Selector selector = null;
		SocketChannel connectChannel = null;
		SocketChannel readChannel = null;
		try {
			int commandID = -1; // 返回网络ID
			int dwFlag1 = -1; // 附加标记位1
			int dwFlag2 = -1; // 附加标记位2
			InetSocketAddress address = new InetSocketAddress(ip, port); // 创建socket
			long start = System.currentTimeMillis();
			// 打开Socket通道
			connectChannel = SocketChannel.open();
			// 设置为非阻塞模式
			connectChannel.configureBlocking(false);
			// 打开选择器
			selector = Selector.open();
			// 注册connectChannel通道为“连接就绪”
			connectChannel.register(selector, SelectionKey.OP_CONNECT);
			// 连接
			connectChannel.connect(address);

			int respXmlLength = 0;
			int sum = 0;
			boolean flag = true;
			_FORLABEL: for (;;) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				selector.select(timeOut); // 最长等待8s
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
				if (!keyIterator.hasNext()) {
					System.out.println(SELECTOR_ERROR);
					break;
				}
				
				int offset = 0;
				byte[] tempBuffer = new byte[0];				
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
					if (key.isConnectable()) { // a connection was established with a remote server.
						readChannel = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(52);
						if (readChannel.isConnectionPending()) {// 调用 isConnectionPending 方法来确定是否正在进行连接操作
							try {
								readChannel.finishConnect(); // 调用  finishConnect 方法来确定完成该连接
								System.out.println("成功连接到服务器");
								int rr = readChannel.read(buffer);
								buffer.clear();
							} catch (ConnectException e) {
								logger.debug("连接服务器失败，请联系管理员");
								dwFlag = "-99998";
								// e.printStackTrace();
								break;
							}
						}
						byte[] headBytes = "DAEH_ATAD_PMDC".getBytes(); // 数据头，DAEH_ATAD_PMDC（CDMP_DATA_HEAD反序）（作用可以忽略掉非客户端的连接通信）
						byte[] netWorkNumBytes = TypeConvertUtils.int2byte(netWorkNum);
						int headBytesLength = headBytes.length;
						int netWorkNumBytesLength = netWorkNumBytes.length;
						int xmlBytesLength = xmlBytes.length;

						byte[] dataBytes = new byte[20 + xmlBytesLength];
						System.arraycopy(netWorkNumBytes, 0, dataBytes, 0, netWorkNumBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(xmlBytesLength), 0, dataBytes, 4, 4);
						System.arraycopy(xmlBytes, 0, dataBytes, 16, xmlBytesLength);
						int dataBytesLength = dataBytes.length;

						byte[] reqBytes = new byte[32 + dataBytesLength];
						System.arraycopy(headBytes, 0, reqBytes, 0, headBytesLength);
						System.arraycopy(TypeConvertUtils.int2byte(dataBytesLength), 0, reqBytes, 20, 4);
						System.arraycopy(dataBytes, 0, reqBytes, 28, dataBytesLength);

						readChannel.write(ByteBuffer.wrap(reqBytes)); // 将字节序列从给定的缓冲区写入此通道

						readChannel.register(selector, SelectionKey.OP_READ); // 注册readChannel通道为“读就绪”
						SELECTOR_ERROR = "服务端无返回或返回超时，请稍后";
					} else if (key.isReadable()) { // a channel is ready for reading						
						// 分配内存
						ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
						
						readChannel = (SocketChannel) key.channel();
						int count = readChannel.read(buffer);
						if (count == 0) {
							continue;
						} else if (count == -1) {
							keyIterator.remove(); // 必须在处理完通道时自己移除 SelectionKey实例
							break _FORLABEL;
						}
						buffer.flip();
						
						byte[] respBytes = new byte[offset + count];  //存储本次读取数据
						if (offset != 0) {
							System.arraycopy(tempBuffer, 0, respBytes, 0, offset);
						}
						System.arraycopy(buffer.array(), 0, respBytes, offset, count);
						offset = 0;
						
						sum += count;
						logger.debug("用户ID: " + SessionUtil.getUserID() + ", 服务器返回完整信息如下: " + "\n"
								+ "请求commandID: " + netWorkNum + "\n"
								+ "返回结果:" + "\n" + new String(buffer.array(), CHARSET_ISO).substring(0, count) + "\n");
						buffer.clear();
						
						if (sum >= 44 && flag) {
							flag = false;

							byte[] paramBytes = new byte[4];

							System.arraycopy(respBytes, 28, paramBytes, 0, 4);
							commandID = TypeConvertUtils.byte2int(paramBytes);
							if (commandID == 10000) {
								logger.debug("收到服务器连接确认通知！！");
								flag = true;
								continue;
							}
							
							System.arraycopy(respBytes, 32, paramBytes, 0, 4);
							respXmlLength = TypeConvertUtils.byte2int(paramBytes);

							System.arraycopy(respBytes, 36, paramBytes, 0, 4);
							dwFlag1 = TypeConvertUtils.byte2int(paramBytes);

							System.arraycopy(respBytes, 40, paramBytes, 0, 4);
							dwFlag2 = TypeConvertUtils.byte2int(paramBytes);
						}

						if (sum < respXmlLength + 44) {
							tempBuffer = respBytes;
							offset = sum;
							continue;
						}

						byte[] respXmlBytes = new byte[respXmlLength];
						System.arraycopy(respBytes, 44, respXmlBytes, 0, respXmlLength);
						logger.debug("接收信息如下: " + "\n"
								+ "返回commandID: " + commandID + "\n"
								+ "附加标记位1: " + dwFlag1 + "\n"
								+ "附加标记位2: " + dwFlag2 + "\n"
								+ "返回xml数据长度: " + respXmlLength + "\n"
								+ "返回xml数据: " + "\n" + new String(respXmlBytes, CHARSET_GBK) + "\n");
						if (dwFlag1 == 1 && dwFlag2 == 0) {
							dwFlag = SUCCESS;
						} if (dwFlag1 != 1 || dwFlag2 != 0) {
							dwFlag = String.valueOf(dwFlag2);
						}
						list.add(dwFlag.getBytes(CHARSET_GBK));
						list.add(respXmlBytes);

						if (count > 0) {
							buffer.clear();
							break _FORLABEL;
						}
					}
				}
			}
			double last = (System.currentTimeMillis() - start) * 1.0 / 1000;
			System.out.println("used time :" + last + "s.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (selector != null) {
				try {
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (connectChannel != null) {
				try {
					connectChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
				
			if (readChannel != null) {
				try {
					readChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}
}
