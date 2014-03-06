package fbot.ft;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import fbot.lib.core.Namespace;
import fbot.lib.core.W;
import fbot.lib.core.aux.Tuple;
import fbot.lib.util.FGUI;
import fbot.lib.util.WikiFile;

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
	 * TextFields for old filename, new filename, and reason
	 */
	private static final JTextField old_tf = new JTextField(30), new_tf = new JTextField(30), r_tf = new JTextField(30);
	
	/**
	 * Our progress bar
	 */
	private static final JProgressBar bar = new JProgressBar(0, 100);
	
	/**
	 * The start stop button
	 */
	private static final JButton button = new JButton("Start/Stop");
	
	/**
	 * Flag indicating if we're active.
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
	 * Negates the status of <tt>activated</tt> in a thread-safe way.
	 */
	private static synchronized void negateActivated()
	{
		activated = !activated;
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
		 * Old name, new name, edit summary.
		 */
		private String old_name, new_name, reason;
		
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
			reason = r_tf.getText().trim().replace("%s", "%%s") + " ([[%sCommons:GlobalReplace|%s]])";
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
				if(!sanityCheck())
					return;
				
				button.setText("Stop");
				negateActivated();
				doJob();
				wiki.switchDomain("commons.wikimedia.org"); // Time to go home.
				button.setText("Start");
			}
			else
			{
				button.setEnabled(false);
				negateActivated();
			}
		}
		
		/**
		 * Performs the replace job & updates UI. Routinely checks activated flag to see if we're still authorized to
		 * execute
		 */
		private void doJob()
		{
			bar.setValue(0);
			bar.setString("Fetching global usage for '" + old_name + "'.  This might take awhile...");
			button.setEnabled(false);
			setTextFieldState(false);
			ArrayList<Tuple<String, String>> l = wiki.globalUsage("File:" + old_name);
			button.setEnabled(true);
			
			if (l == null || l.size() == 0)
				bar.setString(String.format("'%s' is not globally used", old_name));
			else
			{
				bar.setMaximum(l.size());
				String domain = null, text = null;
				for (int i = 0; i < l.size(); i++)
				{
					if (!updateStatus(i, l.get(i)))
						return;
					
					if (domain != l.get(i).y)
						wiki.switchDomain((domain = l.get(i).y));
					
					if ((text = wiki.getPageText(l.get(i).x)) != null)
						wiki.edit(l.get(i).x, text.replaceAll(regex, new_name),
								String.format(reason, (domain.contains("commons") ? "" : "Commons:"), title));
				}
				bar.setString("Done!");
			}
			
			setTextFieldState(true);
			negateActivated();
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
				button.setEnabled(true);
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
			regex = old_name;
			for (String s : new String[] { "(", ")", "[", "]", "{", "}", "^", "-", "=", "$", "!", "|", "?", "*", "+", ".", "<",
					">" })
				regex = regex.replace(s, "\\" + s);
			regex = regex.replaceAll("( |_)", "( |_)");
		}
		
		/**
		 * Checks the legitimacy of the user entered filenames.
		 * @return True if they're ok.
		 */
		private boolean sanityCheck()
		{
		   boolean status = WikiFile.canUpload(old_name) && WikiFile.canUpload(new_name);
		   if(!status)
			   JOptionPane.showMessageDialog(null, "You can only replace valid file names");
		   return status;
		}
		
	}
}