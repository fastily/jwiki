package fbot.ft;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import fbot.lib.core.Namespace;
import fbot.lib.core.W;
import fbot.lib.core.aux.Tuple;
import fbot.lib.util.FGUI;

/**
 * Program to perform Global Replacement of files. Provides us with a GUI.
 * 
 * @author Fastily
 * 
 */
public class GlobalReplace
{
	/**
	 * Our resident wiki object
	 */
	private static W wiki;
	
	/**
	 * Here's where we're signing in.
	 */
	private static final String signin = "Commons:GlobalReplace/Sign-in";
	
	/**
	 * Our title & version number
	 */
	private static final String title = "GlobalReplace v0.3";
	
	/**
	 * TextField for old filename
	 */
	private static final JTextField old_tf = new JTextField(30);
	
	/**
	 * TextField for new filename
	 */
	private static final JTextField new_tf = new JTextField(30);
	
	/**
	 * TextField for edit summary
	 */
	private static final JTextField r_tf = new JTextField(30);
	
	/**
	 * Our progress bar
	 */
	private static final JProgressBar bar = new JProgressBar(0, 100);
	
	/**
	 * The start stop button
	 */
	private static final JButton button = new JButton("Start/Stop");
	
	/**
	 * Flag indicating if we're active. TODO: Should be set with synchronized calls.
	 */
	private static boolean activated = false;
	
	/**
	 * Main driver.
	 * 
	 * @param args Prog args.
	 */
	public static void main(String[] args)
	{
		wiki = FGUI.login();
		signin();
		randomSettings();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				createAndShowGUI();
			}
		});
	}
	
	/**
	 * Creates and shows main GUI.
	 */
	private static void createAndShowGUI()
	{
		JFrame f = FGUI.simpleJFrame(title, JFrame.EXIT_ON_CLOSE, true);
		f.getContentPane().add(
				FGUI.buildForm(title, new JLabel("Old Title: "), old_tf, new JLabel("New Title: "), new_tf, new JLabel(
						"Summary: "), r_tf), BorderLayout.CENTER);
		f.getContentPane().add(FGUI.boxLayout(BoxLayout.Y_AXIS, FGUI.simpleJPanel(button), bar), BorderLayout.SOUTH);
		FGUI.setJFrameVisible(f);
	}
	
	/**
	 * Setting up our components to make them pretty and have them do things.
	 */
	private static void randomSettings()
	{
		old_tf.setToolTipText("Hint: Use Ctrl+v or Command+v to paste text");
		new_tf.setToolTipText("Hint: Use Ctrl+v or Command+v to paste text");
		r_tf.setToolTipText("Hint: Enter an optional edit summary");
		bar.setStringPainted(true);
		bar.setString(String.format("Hello, %s! :)", wiki.whoami()));
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				new Thread(new GRThread()).start();
			}
		});
	}
	
	/**
	 * Sign-in function for new users who have not used our program
	 */
	private static void signin()
	{
		String text = wiki.getPageText(signin);
		if (text == null)
			return; // meh, it's not that important
		else if (!text.contains(wiki.whoami()))
			wiki.edit(signin, text.trim() + "\n#~~~~", "Signing in");
	}
	
	/**
	 * Processes user request & updates the UI accordingly.
	 * 
	 * @author Fastily
	 * 
	 */
	private static class GRThread implements Runnable
	{
		/**
		 * The old name
		 */
		private String old_name;
		
		/**
		 * The new name
		 */
		private String new_name;
		
		/**
		 * The edit summary to use
		 */
		private String reason;
		
		/**
		 * A regex built from old_name
		 */
		private String regex;
		
		/**
		 * Constructor
		 */
		private GRThread()
		{
			old_name = Namespace.nss(old_tf.getText()).trim();
			new_name = Namespace.nss(new_tf.getText()).trim();
			reason = r_tf.getText().trim() + " ([[%sCommons:GlobalReplace|%s]])";
			
			regex = old_name;
			makeRegex();
		}
		
		/**
		 * Runs this procedure. Check if activated flag is false. If it is, set it to true and execute replace function.
		 * If it's true, set flag to false and return. The already running thread should stop when it determines that
		 * the flag is false.
		 */
		public void run()
		{
			if (!activated)
			{
				activated = !activated;
				doJob();
				wiki.switchDomain("commons.wikimedia.org"); // Time to go home.
			}
			else
			{
				button.setEnabled(false);
				activated = !activated;
			}
		}
		
		/**
		 * Performs the replace job & updates UI. Routinely checks activated flag to see if we're still authorized to
		 * execute
		 */
		private void doJob()
		{
			bar.setValue(0);
			button.setEnabled(false);
			setTextFieldState(false);
			ArrayList<Tuple<String, String>> l = wiki.globalUsage("File:" + old_name);
			button.setEnabled(true);
			
			if (l == null || l.size() == 0)
				bar.setString(String.format("'%s' is not globally used", old_name));
			else
			{
				bar.setMaximum(l.size());
				String domain = null;
				for (int i = 0; i < l.size(); i++)
				{
					if (!updateStatus(i, l.get(i)))
					{
						button.setEnabled(true);
						return;
					}
					
					if (domain != l.get(i).y)
					{
						domain = l.get(i).y;
						wiki.switchDomain(domain);
					}
					
					String text = wiki.getPageText(l.get(i).x);
					if (text != null)
						wiki.edit(l.get(i).x, text.replaceAll(regex, new_name),
								String.format(reason, (domain.contains("commons") ? "" : "Commons:"), title));
				}
				bar.setString("Done!");
			}
			
			setTextFieldState(true);
			activated = !activated;
		}
		
		/**
		 * Update the status of the UI and verify that we're still permitted to run.
		 * 
		 * @param i The index number of the item we're processing. This is applied to update the progress bar.
		 * @param t The current item we're processing
		 * @return True if we're still authorized to execute (activated has not been set to false by user).
		 */
		private boolean updateStatus(int i, Tuple<String, String> t)
		{
			if (!activated)
			{
				bar.setValue(0);
				bar.setString("Interrupted by user");
				setTextFieldState(true);
				return false;
			}
			
			bar.setValue(i + 1);
			bar.setString(String.format("Edit %s @ %s (%d/%d)", t.x, t.y, i + 1, bar.getMaximum()));
			return true;
		}
		
		/**
		 * Convenience method to set text fields to uneditable. This should be done when we don't want the user to mess
		 * with any fields.
		 * 
		 * @param editable Set to false to disable user-editing of text fields.
		 */
		private void setTextFieldState(boolean editable)
		{
			old_tf.setEditable(editable);
			new_tf.setEditable(editable);
			r_tf.setEditable(editable);
		}
		
		/**
		 * Generates a regex to replace all instances of old_file on a page.
		 */
		private void makeRegex()
		{
			for (String s : new String[] { "(", ")", "[", "]", "{", "}", "^", "-", "=", "$", "!", "|", "?", "*", "+", ".", "<",
					">" })
				regex = regex.replace(s, "\\" + s);
			regex = regex.replaceAll("( |_)", "( |_)");
		}
	}
}