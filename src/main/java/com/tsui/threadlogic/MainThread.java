/**
 * @Projectname Download_Tool--NetGothic
 * @FileName MainThread.java
 * @Author MarioTsui
 * @Description Main Thread,���а���һ���ڲ��࣬downloadProperty������ʵ����Serializable�ӿ�
 */

package com.tsui.threadlogic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;

/**
 * ���߳���
 * 
 * @author �����F
 * @version version-1.2 �����ж�Դ�Ƿ�֧�ֶϵ�����
 */
public class MainThread implements Runnable
{
	/**
	 * ���̱߳�������
	 */
	private CountDownLatch latch = null;// �û��������߳�
	private JTextArea ta = new JTextArea(); // �������߳���Ϣ����
	private JProgressBar pb; // ������������������ɺ����ý�����Ϊ100%
	private ExecutorService exec = Executors.newCachedThreadPool();// �̳߳أ�
	// ���ڹ���ִ�����߳�
	private ChildThread[] childThread = null;// ���߳�
	private DownloadProperty prop = null;// ���߳��ڲ���̬�࣬���ڳ־û�������Ϣ��

	// �Լ����̷߳���������Ϣ

	/**
	 * MainThread���캯��
	 * 
	 * @param ta
	 *            JTextArea ������Ϣ
	 * @param pb
	 *            JProgressBar ������
	 * @param mtURL
	 *            �����Ñ������URL
	 * @param mtURI
	 *            �����Ñ�����ı���URI
	 * @param mtThreadNum
	 *            �����û�������߳���
	 */
	public MainThread(JTextArea ta, JProgressBar pb, String mtURL,
			String mtURI, int mtThreadNum)
	{
		prop = new DownloadProperty();
		// ��ʼ���������ò���
		this.ta = ta;
		this.pb = pb;

		// ��ʼ���־�����Ϣ
		prop.setURL(mtURL);
		prop.setURI(mtURI);
		prop.setThreadNum(mtThreadNum);
		prop.setContentSize();

		// ��ʼ����
		latch = new CountDownLatch(mtThreadNum);
		childThread = new ChildThread[mtThreadNum];
	};

	/**
	 * getProp ����
	 * 
	 * @return DownloadProperty
	 */
	public DownloadProperty getProp()
	{
		return prop;
	}

	/**
	 * shutdownDownloadThread ���� ���߳��ô˺�����ֹ���̣߳��������û�ѡ�񱣴����ɾ���ϵ���Ϣ
	 * 
	 * @param value
	 *            int �����û�ѡ���ֵ
	 */
	public void shutdownDownloadThread(int value)
	{
		if (value == 0)
		{
			// ���û�ѡ�񱣴�ϵ���Ϣ������ֹ�����̣߳�������ʱ�ļ�
			shutdownAndAwaitTermination(exec);

			// ���öϵ���Ϣ
			ta.append("���ڱ���ϵ���Ϣ...\n");
			File temp = new File(prop.getURI());
			if (!temp.exists())
			{
				setBreakpoint();
			}
		}
		else
		{
			shutdownAndAwaitTermination(exec);
			// ���û�ѡ�񲻱���ϵ���Ϣ����ɾ��������ʱ�ļ�
			ta.append("����ɾ����ʱ�ļ�...\n");
			deleteTempFiles();
		}
	}

	/**
	 * deleteTempFiles ���� ɾ����ʱ�ļ�
	 * 
	 * @param
	 * @return void
	 */
	private void deleteTempFiles()
	{
		boolean t;
		File tmp = new File(prop.getURI() + "_tmp");
		// ɾ�����߳���ʱ�ļ�
		for (int i = 0; i < prop.getThreadNum(); i++)
		{

			if (childThread[i].getChildThreadTempFile().exists())
			{
				t = childThread[i].getChildThreadTempFile().delete();
				if (t == false)
				{
					System.err.println("thread " + i
							+ " thread temp file delete failed!");
				}
				else
					System.out.println("thread temp file " + i + " deleted");
			}
		}
		if (tmp.exists())
		{
			t = tmp.delete();
			if (t == false)
			{
				System.err.println("temp file delete failed!");
			}
			else
				System.out.println("temp file deleted");
		}
	}

	/**
	 * judgeSourceResumeEnabled ���� �ж�Դ�Ƿ�֧�ֶϵ�����
	 * 
	 * @param
	 * @return boolean
	 */
	private boolean judgeSourceResumeEnabled()
	{
		try
		{
			URL url = new URL(prop.getURL());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setAllowUserInteraction(true);
			con.setRequestProperty("Range", "bytes=1-2");

			// �������ӳ�ʱʱ��Ϊ10000ms
			con.setConnectTimeout(10000);

			// ���ö�ȡ���ݳ�ʱʱ��Ϊ10000ms
			con.setReadTimeout(10000);
			// �ж�Դ�Ƿ�֧�ֶϵ�����
			if (con.getResponseCode() != HttpURLConnection.HTTP_PARTIAL)
			{
				ta.append("��Դ��֧�ֶϵ�����...\n");
				con.disconnect();
				return false;
			}
			else
			{
				ta.append("��Դ֧�ֶϵ�����...\n");
				return true;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * setBreakponit ���� ���߳��ô˺������öϵ���Ϣ
	 * 
	 * @param
	 * @return void
	 */
	private void setBreakpoint()
	{
		try
		{
			if (prop.getURI() != null)
			{
				ObjectOutputStream out = new ObjectOutputStream(
						new FileOutputStream(prop.getSavePath() + "\\"
								+ prop.getSaveName() + "_tmp"));
				out.writeObject(prop);
				out.close();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		;
	}

	/**
	 * readThreadBreakpoint ���� ���߳��ô˺�����ȡ�ϵ���Ϣ
	 * 
	 * @param
	 * @return boolean
	 */
	private boolean readThreadBreakpoint()
	{
		try
		{
			File file = new File(prop.getURI() + "_tmp");
			if (file.exists())// ������ص�Ŀ���ļ����ڣ������л�
			{
				ta.append("��ȡ�ϵ���Ϣ...\n");
				ta.append("��������...\n");

				ObjectInputStream out = new ObjectInputStream(
						new FileInputStream(file));
				prop = (DownloadProperty) out.readObject();
				latch = new CountDownLatch(prop.getThreadNum());
				out.close();
				return true;
			}
			else
				return false;
		} catch (IOException e)
		{
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
			return false;
		}
	}

	/**
	 * tempFileToTargetFile ���� ������ɺ����߳��ô˺����ϲ����̵߳���ʱ�ļ�
	 * 
	 * @param ChildThread
	 *            []
	 * @return void
	 */
	private void tempFileToTargetFile(ChildThread[] childThreads)
	{
		ta.append("������װ�ļ�...\n");
		try
		{
			String URI = prop.getURI();
			int threadNum = prop.getThreadNum();
			BufferedOutputStream outputStream = new BufferedOutputStream(
					new FileOutputStream(URI, true));

			// �����������̴߳�������ʱ�ļ�����˳�����������д��Ŀ���ļ���
			for (int i = 0; i < threadNum; i++)
			{
				BufferedInputStream inputStream = new BufferedInputStream(
						new FileInputStream(childThreads[i]
								.getChildThreadTempFile()));
				System.out.println("Now is file " + i);

				int len = 0;
				long count = 0;
				byte[] b = new byte[1024];

				while ((len = inputStream.read(b)) != -1)
				{
					count += len;
					outputStream.write(b, 0, len);
					if ((count % 4096) == 0)
					{
						outputStream.flush();
					}
				}
				inputStream.close();
				ta.append("��װ��ʱ�ļ� " + i + " ���!\n");

				// ������ɺ�ɾ����ʱ�ļ�
				ta.append("����ɾ����ʱ�ļ� " + i + " ...\n");
				File tmp = new File(prop.getURI() + "_tmp");
				if (childThreads[i].getChildThreadStatus() == ChildThread.STATUS_HAS_FINISHED)
				{
					childThreads[i].getChildThreadTempFile().delete();
					tmp.delete();
				}
				// �����߳����ӳ�����ɾ����ʱ�ļ�,�������߳���ʱ�ļ��Ͷϵ���Ϣ��ʱ�ļ�
				else if (childThreads[i].getChildThreadStatus() == ChildThread.STATUS_HTTPSTATUS_ERROR)
				{
					childThreads[i].getChildThreadTempFile().delete();
					tmp.delete();
				}
			}
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * shutdownAndAwaitTermination ���� ������ɺ���ֹ�̳߳��е�����
	 * 
	 * @param pool
	 *            ExecutorService
	 * @return void
	 */
	private void shutdownAndAwaitTermination(ExecutorService pool)
	{
		for (int i = 0; i < prop.getThreadNum(); i++)
		{
			childThread[i].setPause();
			childThread[i].interrupt();
		}
		// ��ֹ�߳�
		try
		{
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(1, TimeUnit.SECONDS))
			{
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(1, TimeUnit.SECONDS))
				{
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie)
		{
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * setChildThreadValue ���� ����������ʱ�����̶߳ϵ���Ϣ��ʼ��
	 * 
	 * @param
	 * @return void
	 */
	private void setChildThreadValue()
	{
		int threadNum = prop.getThreadNum();
		long threadLen = prop.getThreadLen();
		long contentLen = prop.getContentLen();

		// ������ʱ����������ϵ���Ϣ
		long[] starts = new long[threadNum];
		long[] ends = new long[threadNum];

		// ���öϵ�ĳ�ʼ��Ϣ
		for (int i = 0; i < threadNum; i++)
		{

			// ÿ�����ݵ���ʼλ��Ϊ(threadLength * i + �����س���)
			starts[i] = (long) i * threadLen;

			// ÿ����β�����ݵĽ���λ��Ϊ(threadLength * i + �����س��� - 1)
			if (i == (threadNum - 1))
			{
				ends[i] = contentLen;
			}
			else
			{
				ends[i] = threadLen * (i + 1) - 1;
			}
		}

		// ���öϵ���Ϣ���־�����
		prop.setStarts(starts, threadNum, -1, 0);
		prop.setEnds(ends, threadNum, -1, 0);

	}

	/**
	 * run ���� ���߳�ִ�к���
	 */
	public void run()
	{
		ta.append("\n" + "Ѱ�ҷ�������Դ..." + "\n");
		ta.append("\n" + "���߳�������..." + "\n");

		// �ж��Ƿ�֧�ֶϵ�����
		if (judgeSourceResumeEnabled())
		{
			if (!readThreadBreakpoint())// ��֧�֣�������Ƿ��жϵ���Ϣ��ʱ�ļ�
			{
				// ��û�жϵ���Ϣ��˵������ִ��Ϊ������
				setChildThreadValue();
			}
		}
		else
		{
			setChildThreadValue();
		}

		// ���߳�ִ����ʱ�������壬��ȡ�û������������Ϣ
		String mtInfoEcho = null;
		String mtURL = prop.getURL();
		int mtThreadNum = prop.getThreadNum();
		long mtThreadLen = prop.getThreadLen();
		long mtContentLen = prop.getContentLen();
		childThread = new ChildThread[mtThreadNum];

		mtInfoEcho = "�ѻ�ȡ����ԴΪ��\n" + mtURL + "\n" + "������Դ���߳���" + mtThreadNum
				+ "\n";
		ta.append(mtInfoEcho);

		mtInfoEcho = "Դ����Ϊ:" + new BigDecimal(mtContentLen / 1024).setScale(1)
				+ "KB\n";
		ta.append(mtInfoEcho);

		// �������߳�
		for (int i = 0; i < mtThreadNum; i++)
		{

			mtInfoEcho = "\n�߳�" + i + "�ļ���СΪ"
					+ new BigDecimal(mtThreadLen / 1024).setScale(1) + "KB\n";
			ta.append(mtInfoEcho);

			ChildThread thread = new ChildThread(this, latch, i, ta, pb);
			childThread[i] = thread;
			exec.execute(thread);
		}

		try
		{
			// �ȴ�CountdownLatch�ź�Ϊ0����ʾ�������̶߳�������
			latch.await();
			pb.setValue((int) prop.getContentLen() - 1);
			// �ѷֶ�������������ʱ�ļ��е�����д��Ŀ���ļ���
			tempFileToTargetFile(childThread);
			exec.shutdown();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		// �����������ֵ100%
		pb.setValue(pb.getMaximum());
		ta.append("\n�ļ����ؽ���\n");
	}

	/**
	 * DownloadProperty �ڲ��� ʵ�������л��ӿڣ����ڳ־û�������Ϣ
	 * 
	 * @author �����F
	 * @version version-1.0
	 */
	public static class DownloadProperty implements Serializable
	{
		/**
		 * ������Ϣ�йر�������
		 */
		private static final long serialVersionUID = 1;
		private String URL = null; // Դ��ַ
		private String URI = null; // Ŀ�ĵ�ַ�������ַ��

		private String saveName = null; // �����ļ���
		private String savePath = null; // ����·��
		private int threadNum = 0;// �߳���
		private long contentLen = 0;// Դ�ļ�����
		private long threadLen = 0;// �����̳߳���
		private volatile long finishedLen = 0;// ����ɵĳ��ȣ���ʼֵΪ0

		// �ϵ���Ϣ
		private volatile long[] starts;
		private volatile long[] ends;

		public DownloadProperty()
		{
		}

		/**
		 * setURL ���� ����URL
		 * 
		 * @param URL
		 *            String
		 */
		public void setURL(String URL)
		{
			this.URL = URL;
		}

		/**
		 * getURL ����
		 * 
		 * @return String
		 */
		public String getURL()
		{
			return URL;
		}

		/**
		 * setURI ���� ����URI
		 * 
		 * @param URI
		 *            String
		 */
		public void setURI(String URI)
		{
			this.URI = URI;
		}

		/**
		 * getURI ����
		 * 
		 * @return String
		 */
		public String getURI()
		{
			return URI;
		}

		/**
		 * setContentLen ���� ����contentLen
		 * 
		 * @param len
		 *            long
		 */
		public void setContentLen(long len)
		{
			contentLen = len;
		}

		/**
		 * getContentLen ����
		 * 
		 * @return long
		 */
		public long getContentLen()
		{
			return contentLen;
		}

		/**
		 * setThreadLen ���� ����threadLen
		 * 
		 * @param len
		 *            long
		 */
		public void setThreadLen(long len)
		{
			threadLen = len;
		}

		/**
		 * getThreadLen ����
		 * 
		 * @return long
		 */
		public long getThreadLen()
		{
			return threadLen;
		}

		/**
		 * setFinishedLen ���� ����finishedLen
		 * 
		 * @param len
		 *            long
		 */
		public void setFinishedLen(long len)
		{
			finishedLen += len;
		}

		/**
		 * getFinishedLen ����
		 * 
		 * @return long
		 */
		public long getFinishedLen()
		{
			return finishedLen;
		}

		/**
		 * setThreadNum ���� ����threadNum
		 * 
		 * @param num
		 *            int
		 */
		public void setThreadNum(int num)
		{
			threadNum = num;
		}

		/**
		 * getThreadNum ����
		 * 
		 * @return int
		 */
		public int getThreadNum()
		{
			return threadNum;
		}

		/**
		 * setStarts ���� ���öϵ���Ϣ���������ַ�ʽ�ĸ�ֵ һ���̺߳�Ϊ-1ʱ�����еĶ϶���Ϣ����
		 * �����̺߳Ų�Ϊ-1ʱ����Ӧ�̵߳Ķ϶���Ϣ����
		 * 
		 * @param starts
		 *            [] long
		 * @param threadNum
		 *            int
		 * @param threadID
		 *            int
		 * @param aThreadStart
		 *            long ĳ���̵߳Ķϵ����ֵ
		 */
		public void setStarts(long starts[], int threadNum, int threadID,
				long aThreadStart)
		{
			if (threadID == -1)
			{
				this.starts = new long[threadNum];
				this.starts = starts;
			}
			else
			{
				this.starts[threadID] = aThreadStart;
			}
		}

		/**
		 * getStarts ���� ��ȡ�����̵߳Ķϵ���Ϣ
		 * 
		 * @return long[]
		 */
		public long[] getStarts()
		{
			return starts;
		}

		/**
		 * setEnds ���� ���öϵ���Ϣ���������ַ�ʽ�ĸ�ֵ һ���̺߳�Ϊ-1ʱ�����еĶ϶���Ϣ����
		 * �����̺߳Ų�Ϊ-1ʱ����Ӧ�̵߳Ķ϶���Ϣ����
		 * 
		 * @param ends
		 *            [] long
		 * @param threadNum
		 *            int
		 * @param threadID
		 *            int
		 * @param aThreadEnd
		 *            long ĳ���̵߳Ķϵ����ֵ
		 */
		public void setEnds(long ends[], int threadNum, int threadID,
				long aThreadEnd)
		{
			if (threadID == -1)
			{
				this.ends = new long[threadNum];
				this.ends = ends;
			}
			else
			{
				this.ends[threadID] = aThreadEnd;
			}
		}

		/**
		 * getEnds ���� ��ȡ�����̵߳Ķϵ���Ϣ
		 * 
		 * @return long[]
		 */
		public long[] getEnds()
		{
			return ends;
		}

		/**
		 * getSaveName ���� ��ȡ���ش洢�ļ����ļ���
		 * 
		 * @return String
		 */
		public String getSaveName()
		{
			saveName = URI
					.substring(URI.lastIndexOf("\\") + 1,
							URI.lastIndexOf("?") > 0 ? URI.lastIndexOf("?")
									: URI.length());
			if ("".equalsIgnoreCase(this.saveName))
			{
				this.saveName = UUID.randomUUID().toString();
			}
			return saveName;
		}

		/**
		 * getSaveName ���� ��ȡ���ش洢�ļ���·��
		 * 
		 * @return String
		 */
		public String getSavePath()
		{
			savePath = URI.substring(URI.indexOf(URI.charAt(0)), URI
					.lastIndexOf("\\") > 0 ? URI.lastIndexOf("\\") : URI
					.length());
			if ("".equalsIgnoreCase(this.savePath))
			{
				this.savePath = UUID.randomUUID().toString();
			}
			return savePath;
		}

		/**
		 * setContentSize ���� �������̳߳�ʼ��contentLen��threadLen
		 */
		public void setContentSize()
		{
			try
			{
				for (int i = 0; i < 10; i++)
				{
					// ���ӳ��������������ʾ
					if (i > 0)
						System.out.println("���ӳ�����������������...\n");
					if (i == 9)
					{
						System.out.println("����10��δ�ɹ����˳�\n");
						System.exit(0);
					}
					URL url = new URL(URL);
					// ��URLConnection
					HttpURLConnection con = (HttpURLConnection) url
							.openConnection();
					con.setAllowUserInteraction(true);

					// �������ӳ�ʱʱ��Ϊ10000ms
					con.setConnectTimeout(10000);

					// ���ö�ȡ���ݳ�ʱʱ��Ϊ10000ms
					con.setReadTimeout(10000);

					// �ж�http status�Ƿ�ΪHTTP/1.1 206 Partial Content����200 OK
					// ���������������״̬����status��ΪSTATUS_HTTPSTATUS_ERROR
					if (con.getResponseCode() != HttpURLConnection.HTTP_OK
							&& con.getResponseCode() != HttpURLConnection.HTTP_PARTIAL)
					{
						System.out.println("");
						break;
					}
					contentLen = con.getContentLength();
					threadLen = contentLen / threadNum;
					// con.disconnect();
					break;
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			;

		}
	}
}
