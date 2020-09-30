package org.deltaproject.manager.utils;

import java.util.Random;

public class ProgressBar extends Thread {
	public CustomHandler handler;

	private Random ran = new Random();
	private StringBuilder progress;
	private int done;
	private int before;
	
	private int extracharsBK;

	private boolean isFirst = false;

	/**
	 * initialize progress bar properties.
	 */
	public ProgressBar(String code) {
		handler = new CustomHandler(code);
		init();
	}

	/**
	 * called whenever the progress bar needs to be updated. that is whenever
	 * progress was made.
	 * 
	 * @param done
	 *            an int representing the work done so far
	 * @param total
	 *            an int representing the total work
	 */
	public void update(int done, int total) {
		if (isFirst) {
			this.clearConsole();
			System.out.println("");
		} else {
			setCursorPos(0, 1);
			lineClear();
			setCursorPos(0, 1);
		}

		char[] workchars = { '|', '/', '-', '\\' };
		String format = "\r%3d%% %s %c";

		int percent = (++done * 100) / total;

		int extrachars = (percent / 2) - this.progress.length();

		while (extrachars-- > 0) {
			progress.append('=');
		}

		extracharsBK = this.progress.length() + 7;

		System.out.printf(format, percent, progress, workchars[done
				% workchars.length]);

		handler.flush();

		if (done == total) {
			System.out.flush();
			init();
		}
	}

	public void noUpdate() {
		setCursorPos(extracharsBK, 1);

		char[] workchars = { '|', '/', '-', '\\' };
		String format = "\b%c";
		System.out
				.printf(format, workchars[ran.nextInt(10) % workchars.length]);
	}

	public void lineClear() {
		System.out.println("                                  "
				+ "                                           ");
	}

	public void clearMsg() {
		handler.clear();
	}
	
	private void init() {
		this.progress = new StringBuilder(60);
		this.before = 0;
	}

	public void done(int value) {
		this.done = value;
	}

	public void done(int value, String msg) {
		this.done = value;
		handler.publish(msg);
	}

	public int getDone() {
		return this.done;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int done = getDone();
			int i = before;

			if (i < done) {
				while (i < done) {
					for (int j = 0; j < 10000000; j++)
						for (int p = 0; p < 10000000; p++)
							;
					// update the progress bar
					update(i++, 100);
				}
			} else {
				noUpdate();

				continue;
			}

			before = done;
			if (done == 100)
				break;
		}
	}

	public final static void clearConsole() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	public final static void setCursorPos(int XPos, int YPos) {
		System.out.format("\033[%d;%dH", YPos + 1, XPos + 1);
	}
}
