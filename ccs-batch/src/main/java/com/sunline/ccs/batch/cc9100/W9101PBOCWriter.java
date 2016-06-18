package com.sunline.ccs.batch.cc9100;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.util.ExecutionContextUserSupport;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.sunline.ppy.dictionary.report.ccs.T9002ToPBOCRptItem;
import com.sunline.ark.support.cstruct.CStruct;
import com.sunline.ark.support.utils.DateUtils;

/**
 * @see 类名：W9101PBOCWriter
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:48:52
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class W9101PBOCWriter<H extends H9101PBOCFileHeader, D extends T9002ToPBOCRptItem> extends ExecutionContextUserSupport implements ResourceAwareItemWriterItemStream<D>, BeanNameAware {
	
	private static final String STATE_KEY = "state";

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Resource resource;
	
	private FileChannel fileChannel;
	
	private RandomAccessFile outputRandomAccessFile;
	
	private File outputTempFile;
	
	private State state;
	
	private Class<H> fileHeaderClass;
	
	private Class<D> fileDetailClass;
	
	
	/**
	 * 使用CStruct输出时的编码
	 */
	protected String charset = "utf-8";

	private CStruct<D> detailStruct;
	private CStruct<H> headerStruct;
	private ByteBuffer detailBuffer;

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException
	{
		String key = getKey(STATE_KEY);
		if (executionContext.containsKey(key))
			state = (State)executionContext.get(key);
		else
			state = new State();

		try
		{
			outputTempFile = new File(resource.getFile().getAbsolutePath() + ".yak");
			outputRandomAccessFile = new RandomAccessFile(outputTempFile, "rw");
			fileChannel = outputRandomAccessFile.getChannel();
			
			//截取上次断点位置，如果是第一次运行，也可以这么处理，这样保证覆盖原有文件
			fileChannel.truncate(state.lastPosition);
			fileChannel.position(state.lastPosition);
			
			//放一个空文件头
			if (state.lastPosition == 0)
			{
				H header = fileHeaderClass.newInstance();
				ByteBuffer buffer = ByteBuffer.allocate(headerStruct.getByteLength() + 1);
				headerStruct.writeByteBuffer(header, buffer);
				buffer.put((byte)'\n');
				buffer.flip();
				fileChannel.write(buffer);
			}
		} 
		catch (Exception e)
		{
			logger.error("打开文件出错", e);
		}
		
		
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		Assert.notNull(state);
		
		try
		{
			if (fileChannel != null && fileChannel.isOpen())
			{
				state.lastPosition = fileChannel.position();
				
				//存入context
				executionContext.put(getKey(STATE_KEY), state);
			}
		}
		catch (IOException e)
		{
			throw new ItemStreamException("更新文件状态出错", e);
		}
		
	}

	@Override
	public void close() throws ItemStreamException
	{
		try
		{
			if (fileChannel != null && fileChannel.isOpen())
			{
				//最终更新文件头
				fileChannel.position(0);
				H header = fileHeaderClass.newInstance();
				// 回调生成文件头
				header.h8115 = "1.3";//数据格式版本号； T+1版本1.3
				header.h6101 = "";//金融机构代码； FTS参数实现
				header.h2402 = new Date();//报文生成时间，机器时间
				header.h8117 = "1.0";//上传报文版本号，暂定
				header.h8121 = "1";//重报提示; 1-非重报报文；2-报文级重报报文 参数
				header.h8119 = "1";//报文类别; 1-正常数据
				header.h8111 = state.count;//账户记录总数
				header.h2412 = state.minBillingDate;//最早结算/应还款日期
				header.h2414 = state.maxBillingDate;//最晚结算/应还款日期
				header.h5212 = "";//联系人
				header.h3145 = "";//联系电话
				header.h8107 = "";//预留字段
				
				ByteBuffer buffer = ByteBuffer.allocate(headerStruct.getByteLength() + 1);
				headerStruct.writeByteBuffer(header, buffer);
				buffer.put((byte)'\n');
				buffer.flip();
				fileChannel.write(buffer);
				
			}
		}
		catch (IllegalAccessException e)
		{
			throw new ItemStreamException("写入文件头出错", e);
		}
		catch (InstantiationException e)
		{
			throw new ItemStreamException("写入文件头出错", e);
		}
		catch (IOException e) {
			throw new ItemStreamException("文件IO出错", e);
		}
		finally
		{
			IOUtils.closeQuietly(fileChannel);
			IOUtils.closeQuietly(outputRandomAccessFile);
		}

		if (StepSynchronizationManager.getContext().getStepExecution().getExitStatus().equals(ExitStatus.COMPLETED))
		{
			//成功处理，改文件名为最终文件
			try
			{
				if (resource.exists())
				{
					logger.warn("输出文件[{}]已存在，将被删除。", resource.getURL());
					resource.getFile().delete();
				}
				outputTempFile.renameTo(resource.getFile());
			} 
			catch (IOException e)
			{
				throw new ItemStreamException("最终文件改名失败", e);
			}
		}
	}

	@SuppressWarnings("unchecked")	//理由见下
	@Override
	public void write(List<? extends D> items) throws Exception {
		for (D item : items)
		{
			if (item instanceof Iterable)
			{
				//加入对Iterable类型的Item的支持，其实这么写是违反泛型的语义的。
				//这样List<? extends D> items就是错的，但为了开发方便，就这么处理了。
				for (D itemitem : (Iterable<D>)item)
				{
					doWriteItem(itemitem);
				}
			}
			else
			{
				doWriteItem(item);
				
				if(item.dataItem04_2301 != null){
					
					if(state.minBillingDate == null){
						state.minBillingDate = item.dataItem04_2301;
						state.maxBillingDate = item.dataItem04_2301;
					}
				
					//最早结算/应还款日期，为填入文件头
					if(DateUtils.truncatedCompareTo(item.dataItem04_2301, state.minBillingDate, Calendar.DATE) < 0){
						state.minBillingDate = item.dataItem04_2301;
					}
					//最晚结算/应还款日期，为填入文件头
					if(DateUtils.truncatedCompareTo(item.dataItem04_2301, state.maxBillingDate, Calendar.DATE) > 0){
						state.maxBillingDate = item.dataItem04_2301;
					}
				}
			}
		}
	}
        /**
         * @see 方法名：doWriteItem 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午3:06:09
         * @author ChengChun
         *  
         * @param item
         * @throws IOException
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	private void doWriteItem(D item) throws IOException {
		detailBuffer.clear();
		
		detailStruct.writeByteBuffer(item, detailBuffer);
		detailBuffer.put((byte)'\n');
		detailBuffer.flip();
		fileChannel.write(detailBuffer);
		
		state.count++;
	}
	
	@PostConstruct
	public void init()
	{
		detailStruct = new CStruct<D>(fileDetailClass, charset);
		headerStruct = new CStruct<H>(fileHeaderClass, charset);
		detailBuffer = ByteBuffer.allocate(detailStruct.getByteLength() + 1);
	}
	
	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	@SuppressWarnings("serial")
	private static class State implements Serializable
	{
		private long lastPosition = 0;
		private int count = 0;
		
		//最早结算/应还款日期
		private Date maxBillingDate;
		//最晚结算/应还款日期
		private Date minBillingDate;
	}

	public void setFileHeaderClass(Class<H> fileHeaderClass) {
		this.fileHeaderClass = fileHeaderClass;
	}

	public void setFileDetailClass(Class<D> fileDetailClass) {
		this.fileDetailClass = fileDetailClass;
	}

	@Override
	public void setBeanName(String name) {
		//默认使用bean id作为name
		setName(name);
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

}
