/**
 * @Projectname Download_Tool--NetGothic
 * @FileName GUI.java
 * @Author MarioTsui
 * @Description Interface Design
 */

package com.tsui.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.tsui.threadlogic.MainThread;

/**
 * �������
 * 
 * @author �����F
 * @version version-1.1�������˳�ʱ�ĶԻ���,����ѡ���Ƿ񱣴�ϵ���Ϣ
 */

public class GUI extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	/**
	 * ����������� �����̶߳���
	 */
	private JPanel p1;
	private JPanel p2;
	private JPanel p3;
	private JPanel p4;
	private JPanel p5;

	private JProgressBar jProgBar;
	private JTextArea jTextArea;
	private JScrollPane jScrollPane;

	private JLabel srcPathLabel;
	private JTextField srcPathInput;

	private JLabel savePathLabel;
	private JTextField savePathInput;

	private JLabel threadNumLabel;
	private JSpinner threadNumSpinner;

	private JButton select;
	private JButton start;
	private JButton restart;
	private JButton pause;
	private JButton quit;

	MainThread main = null;
	Thread threadMain = null;

	/**
	 * GUI���캯��
	 */
	public GUI()
	{
		super("TsuiTool");

		// ������ʼ��
		p1 = new JPanel();
		p2 = new JPanel();
		p3 = new JPanel();
		p4 = new JPanel();
		p5 = new JPanel();

		jProgBar = new JProgressBar(0, 100);
		jTextArea = new JTextArea(15, 20);
		jScrollPane = new JScrollPane(jTextArea);

		srcPathLabel = new JLabel("���ص�ַ��");
		srcPathInput = new JTextField(20);
		savePathLabel = new JLabel("����λ�ã�");
		savePathInput = new JTextField(20);

		select = new JButton("���Ϊ");
		start = new JButton("��ʼ����");
		pause = new JButton("��ͣ����");
		restart = new JButton("��������");
		quit = new JButton("�˳�");

		// ��ֵΪ5����Сֵ0�����ֵ20������1
		threadNumSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 20, 1));
		threadNumLabel = new JLabel("�߳���");

		// JFrame���ֳ�ʼ��
		setLayout(new BorderLayout());
		setSize(370, 478);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);

		add(p1, BorderLayout.NORTH);
		add(p4, BorderLayout.CENTER);
		add(p5, BorderLayout.SOUTH);

		// Panel 1---BorderLayout.NORTH
		p1.add(jScrollPane);

		// Panel 4---BorderLayout.CENTER
		p4.setLayout(new GridLayout(2, 1));
		p4.add(p2);
		p4.add(p3);
		p2.add(srcPathLabel);

		p2.add(srcPathInput);
		srcPathInput.setText("http://bolofski.freeservers.com/tobewithyou.mp3");
		p2.add(jProgBar);
		jProgBar.setStringPainted(true);

		p2.add(threadNumLabel);
		p2.add(threadNumSpinner);
		p2.add(select);
		p3.add(savePathLabel);
		p3.add(savePathInput);

		// Panel 5---BorderLayout.SOUTH
		p5.add(start);
		// p5.add(pause);
		// p5.add(restart);
		p5.add(quit);

		// ���Action
		// Button ��ʼ���� add Action
		start.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startAction(e);
			}
		});

		// Button ���Ϊ add Action
		select.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				selectAction(e);
			}

		});

		// Button ��ͣ���� add Action
		pause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				pauseAction(e);
			}
		});

		// Button �������� add Action
		restart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				restartAction(e);
			}
		});

		// Button �˳� add Action
		quit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				quitAction(e);
			}
		});
	}

	/**
	 * selectAction �¼���Ӧ�������Ϊ����ť�¼���Ӧ��
	 * 
	 * @param e
	 *            ActionEvent �����Ϊ��ť�¼���
	 * @return void
	 */
	private void selectAction(ActionEvent e)
	{
		String URL = srcPathInput.getText();
		if (URL.equals(""))
			jTextArea.setText("���������ص�ַ");

		Frame f = new Frame("���Ϊ");
		FileDialog fd = new FileDialog(f, "���Ϊ", FileDialog.SAVE);
		int index = URL.lastIndexOf(".");
		String ends = URL.substring(index, URL.length());
		fd.setFile("*" + ends);
		fd.setVisible(true);
		try
		{
			String savepath = fd.getDirectory();
			String savename = fd.getFile();
			if (savename != null)
			{
				savePathInput.setText(savepath + savename);
			}
		} catch (Exception esave)
		{
		}
	}

	/**
	 * startAction �¼���Ӧ������ʼ���ء���ť�¼���Ӧ��
	 * 
	 * @param e
	 *            ActionEvent ������ʼ���ء���ť�¼���
	 * @return void
	 */
	private void startAction(ActionEvent e)
	{

		if (srcPathInput.getText().equals(""))
		{
			jTextArea.setText("���������������ص�ַ");
		}
		else if (savePathInput.getText().equals(""))
		{
			jTextArea.setText("�����������ı����ַ");
		}
		else
		{
			// �����̴߳��ݲ���
			String guiURL = srcPathInput.getText();
			String guiURI = savePathInput.getText();
			Integer guiThreadNum1 = (Integer) threadNumSpinner.getValue();
			int guiThreadNum = guiThreadNum1.intValue();
			// �����̳߳�ʼ��
			main = new MainThread(jTextArea, jProgBar, guiURL, guiURI,
					guiThreadNum);
			threadMain = new Thread(main);

			// �����߳�ִ��
			threadMain.start();

			// �ӳ־����ж�ȡ��Ϣ�������ý�������ֵ��
			jProgBar.setMaximum((int) main.getProp().getContentLen());
			jProgBar.setMinimum(0);
		}
	}

	/**
	 * pauseAction �¼���Ӧ������ͣ���ء���ť�¼���Ӧ��
	 * 
	 * @param e
	 *            ActionEvent ������ͣ���ء���ť�¼���
	 * @return void
	 */
	private void pauseAction(ActionEvent e)
	{
		jTextArea.append("Now Pausing...");
	}

	/**
	 * restartAction �¼���Ӧ�����������ء���ť�¼���Ӧ��
	 * 
	 * @param ActionEvent
	 *            e �����������ء���ť�¼���
	 * @return void
	 */
	private void restartAction(ActionEvent e)
	{
		jTextArea.append("Now restart...");
	}

	/**
	 * quitAction �¼���Ӧ�����˳�����ť�¼���Ӧ��
	 * 
	 * @param e
	 *            ActionEvent �����˳�����ť�¼���
	 * @return void
	 */
	private void quitAction(ActionEvent e)
	{
		jTextArea.append("Now quit...\n");
		int value = JOptionPane.showConfirmDialog((Component) null, "�Ƿ񱣴�ϵ���Ϣ",
				"��ʾ", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);

		if (main != null)
		{
			jTextArea.append("������ֹ�߳�...\n");
			if (value == 0)
			{
				main.shutdownDownloadThread(value);
				System.exit(0);
			}
			else if (value == 1)
			{
				main.shutdownDownloadThread(value);
				System.exit(0);
			}
		}
		else
		{
			if (value != 2)
				System.exit(0);
		}
	}

}
