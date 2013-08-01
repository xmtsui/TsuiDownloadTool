/**
 * @Projectname Download_Tool--NetGothic
 * @FileName ChildThread.java
 * @Author MarioTsui
 * @Description Child Thread
 */

package com.tsui.threadlogic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;

/**
 * ���߳�
 * 
 * @author �����F
 * @version version-1.0
 */
public class ChildThread extends Thread
{

	public static final int STATUS_HASNOT_FINISHED = 0;
	public static final int STATUS_HAS_FINISHED = 1;
	public static final int STATUS_HTTPSTATUS_ERROR = 2;

	private int threadID; // �̺߳�
	private int childStatus; // �̵߳�ǰ����״̬
	private File tmpFile; // �߳���ʱ�ļ�
	private RandomAccessFile ranFile; // �߳���ʱ�ļ�
	private final CountDownLatch latch; // ���߳�ִ������1
	// private Semaphore gate;
	private volatile boolean run;

	// ������Ϣ��������
	private int ctThreadNum;
	private long ctThreadLen;
	private long ctStart; // ���߳̿�ʼλ��
	private long ctEnd; // ���߳̽���λ��
	private String ctURI;
	private String ctURL;

	private MainThread main = null;
	private JProgressBar pb; // ������
	private JTextArea ta; // ��ʾ������Ϣ

	/**
	 * ChildThread���캯��
	 * 
	 * @param m
	 *            MainThread
	 * @param latch
	 *            CountDownLatch
	 * @param ID
	 *            int �̺߳�
	 * @param ta
	 *            JTextArea
	 * @param pb
	 *            JProgressBar
	 */
	public ChildThread(MainThread m, CountDownLatch latch, int ID,
			JTextArea ta, JProgressBar pb)
	{
		main = m;
		this.latch = latch;
		this.ta = ta;
		this.pb = pb;
		this.run = true;
		ta.setSelectionStart(ta.getText().length());
		threadID = ID;
		childStatus = ChildThread.STATUS_HASNOT_FINISHED;

		ctThreadLen = m.getProp().getThreadLen();
		ctURL = m.getProp().getURL();
		ctURI = m.getProp().getURI();
		ctThreadNum = m.getProp().getThreadNum();
		ctStart = m.getProp().getStarts()[ID];
		ctEnd = m.getProp().getEnds()[ID];

		try
		{
			tmpFile = new File(ctURI + "_td" + ID);
			ranFile = new RandomAccessFile(tmpFile, "rw");
			ranFile.seek(ctStart - ctThreadLen * threadID);
			if (!tmpFile.exists())
			{
				tmpFile.createNewFile();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	};

	/**
	 * Sets the sorter to "run" mode. Called on the event dispatch thread.
	 */
	public void setRun()
	{
		run = true;
		// gate.release();
	}

	/**
	 * Sets the sorter to "step" mode. Called on the event dispatch thread.
	 */
	public void setPause()
	{
		run = false;
		// gate.release();
	}

	/**
	 * getChildThreadID ���� ��ȡ���̺߳�
	 * 
	 * @return int
	 */
	public int getChildThreadID()
	{
		return threadID;
	}

	/**
	 * getChildThreadStatus ���� ��ȡ���߳�״̬
	 * 
	 * @return int
	 */
	public int getChildThreadStatus()
	{
		return childStatus;
	}

	/**
	 * getChildThreadTempFile ���� ��ȡ���߳���ʱ�ļ�
	 * 
	 * @return File
	 */
	public File getChildThreadTempFile()
	{
		return tmpFile;
	}

	/**
	 * run ���� ���߳�ִ��
	 */
	public void run()
	{
		System.out.println("Thread " + threadID + " run ...");

		// ��ʱ������ʼ��
		HttpURLConnection con = null;
		InputStream inputS = null;
		BufferedOutputStream outputS = null;

		// ��ȡ�ϵ���Ϣ�����޶ϵ���ʱ�ļ����½��߳���ʱ�ļ�
		// readThreadBreakpoint();

		// ����ʼ�����ļ������棬�����óɿ�����β��׷������
		try
		{
			outputS = new BufferedOutputStream(new FileOutputStream(ranFile
					.getFD()));
		} catch (IOException e2)
		{
			e2.printStackTrace();
		}

		// �����ӳ����ɳ���10����������
		for (int i = 0; i < 10; i++)
		{
			// ���ӳ��������������ʾ
			if (i > 0)
				System.out.println("Now thread " + threadID
						+ "is reconnect for the " + i
						+ "times , start position is " + ctStart);
			try
			{

				ctStart = main.getProp().getStarts()[threadID];
				ctEnd = main.getProp().getEnds()[threadID];
				URL url = new URL(ctURL);
				// ��URLConnection
				con = (HttpURLConnection) url.openConnection();

				con.setAllowUserInteraction(true);

				// �������ӳ�ʱʱ��Ϊ10000ms
				con.setConnectTimeout(10000);

				// ���ö�ȡ���ݳ�ʱʱ��Ϊ10000ms
				con.setReadTimeout(10000);

				// �����߳����ص���ֹ����
				if (ctStart < ctEnd)
				{
					con.setRequestProperty("Range", "bytes=" + ctStart + "-"
							+ ctEnd);
					System.out.println("Thread " + threadID
							+ " startPosition is " + ctStart);
					System.out.println("Thread " + threadID
							+ " endPosition is " + ctEnd);

					// �ж�http status�Ƿ�ΪHTTP/1.1 206 Partial Content����200 OK
					// ���������������״̬����status��ΪSTATUS_HTTPSTATUS_ERROR
					if (con.getResponseCode() != HttpURLConnection.HTTP_OK
							&& con.getResponseCode() != HttpURLConnection.HTTP_PARTIAL)
					{
						System.out.println("Thread " + threadID + ": code = "
								+ con.getResponseCode() + ", status = "
								+ con.getResponseMessage());
						childStatus = ChildThread.STATUS_HTTPSTATUS_ERROR;
						outputS.close();
						con.disconnect();
						System.out.println("Thread " + threadID + " finished.");
						latch.countDown();
						break;
					}

					// ��Դ�ļ�����������
					inputS = con.getInputStream();

					int len = 0;// ��¼ÿ�ζ�ȡ���ֽ���
					long cntlen = ctStart - (threadID * ctThreadLen);// ��¼�����ص��ֽ���
					byte[] buff = new byte[1024];// �������������ջ�����
					while ((len = inputS.read(buff)) != -1)
					{
						outputS.write(buff, 0, len);
						cntlen += len;
						ctStart += len;

						// ÿ����4096��byte��һ���ڴ�ҳ������������flushһ��
						if (cntlen % 4096 == 0)
						{
							outputS.flush();
							// ÿflushһ�Σ�����һ�γ־����еĶϵ���Ϣ
							main.getProp().setStarts(
									main.getProp().getStarts(), ctThreadNum,
									threadID, ctStart);
						}

						// ÿѭ��һ�Σ�����һ�������س���
						main.getProp().setFinishedLen(len);
						// �����õ�ǰ����
						long tempLen = main.getProp().getFinishedLen();
						pb.setValue((int) tempLen - 1);
						// Thread.sleep(100);
					}

					// ����һ�γ־����еĶϵ���Ϣ
					main.getProp().setStarts(main.getProp().getStarts(),
							ctThreadNum, threadID, ctStart);

					System.out.println("down finished length is��"
							+ ((threadID * ctThreadLen) + cntlen));
					if (cntlen >= ctThreadLen)
					{
						childStatus = ChildThread.STATUS_HAS_FINISHED;
					}
					outputS.flush();
					outputS.close();
					inputS.close();
					con.disconnect();
				}
				else
				{
					childStatus = ChildThread.STATUS_HAS_FINISHED;
					con.disconnect();
				}

				System.out.println("Thread " + threadID + " finished.");
				ta.append("Thread" + threadID + "finished\n");
				// ���߳̽�latch��1
				latch.countDown();
				break;
			} catch (IOException e)
			{
				try
				{
					outputS.close();
					// inputS.close();
					con.disconnect();
					TimeUnit.SECONDS.sleep(10);
				} catch (InterruptedException e1)
				{
					e1.printStackTrace();
				} catch (IOException e2)
				{
					e2.printStackTrace();
				}
				continue;
			}
			// catch (InterruptedException e1)
			// {
			// try
			// {
			// outputS.close();
			// inputS.close();
			// con.disconnect();
			// } catch (IOException e2)
			// {
			// e2.printStackTrace();
			// }
			// }
			// ;
		}// end of for
	}// end of run()
}// end of class