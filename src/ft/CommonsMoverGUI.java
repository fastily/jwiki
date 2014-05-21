package ft;

import static ft.CommonsMover.*;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import jwiki.core.Namespace;
import jwiki.util.FGUI;
import jwiki.util.FString;

/**
 * GUI Wrapper for CommonsMover.
 * 
 * @see ft.CommonsMover
 * @author Fastily
 */
public class CommonsMoverGUI
{
	/**
	 * Form fields
	 */
	private static JTextField user_t = new JTextField(25), cat_t = new JTextField(25), single_t = new JTextField(25);

	/**
	 * Buttons to activate form fields
	 */
	private static JRadioButton user_b = new JRadioButton("User: "), cat_b = new JRadioButton("Category: "),
			single_b = new JRadioButton("Single: ");

	/**
	 * Progress bar to track completed items
	 */
	private static JProgressBar pb = new JProgressBar(0, 100);

	/**
	 * Button to start uploading
	 */
	private static JButton go = new JButton("Go");

	/**
	 * Main method
	 * 
	 * @param args Prog args. (Doesn't accept any)
	 * @throws LoginException If bad login credentials.
	 */
	public static void main(String[] args) throws LoginException
	{
		com = FGUI.login();
		enwp = com.getWiki("en.wikipedia.org");
		
		String signtitle = "Commons:CommonsMover/Sign-in";
		String signtext = com.getPageText(signtitle); 
		if(!signtext.toLowerCase().contains(com.whoami().toLowerCase()))
			com.edit(signtitle, signtext + "\n#~~~~", "Signing in");
		
		SwingUtilities.invokeLater(() -> createAndShowGUI());
	}

	/**
	 * Creates the GUI and adds our settings.
	 */
	private static void createAndShowGUI()
	{
		// Set some simple tool tips
		user_t.setToolTipText("Enter a username to transfer all their freely licensed uploads.");
		cat_t.setToolTipText("Specifiy a category to transfer all freely licensed files within.");
		single_t.setToolTipText("Specify a filename to transfer it.");

		// Make only one radio button active at a time
		ButtonGroup bg = new ButtonGroup();
		bg.add(user_b);
		bg.add(cat_b);
		bg.add(single_b);

		// unchecked radio buttons should disable respective text fields.
		user_t.setEditable(false);
		cat_t.setEditable(false);
		single_t.setEditable(false);

		user_b.addActionListener(e -> quickButtonFlip(user_t, cat_t, single_t));
		cat_b.addActionListener(e -> quickButtonFlip(cat_t, user_t, single_t));
		single_b.addActionListener(e -> quickButtonFlip(single_t, cat_t, user_t));

		// Progress bar should show printed strings
		pb.setStringPainted(true);
		pb.setString(String.format("Welcome, %s! :D", com.whoami()));

		go.addActionListener(e -> new Thread(() -> doJob()).start()); // logic happens in its own thread, or GUI freezes

		// Generate the JFrame itself
		JFrame f = FGUI.simpleJFrame("CommonsMover 0.1a", JFrame.EXIT_ON_CLOSE, true);
		f.getContentPane().add(FGUI.boxLayout(BoxLayout.Y_AXIS, FGUI.simpleJPanel(go), pb), BorderLayout.SOUTH);
		f.getContentPane().add(FGUI.buildForm("CommonsMover", user_b, user_t, cat_b, cat_t, single_b, single_t),
				BorderLayout.CENTER);
		FGUI.setJFrameVisible(f);
	}

	/**
	 * Toggles the editiablity of our text fields.
	 * 
	 * @param t The textfield to enable
	 * @param a A text field to disable
	 * @param b Another text field to disable
	 */
	private static void quickButtonFlip(JTextField t, JTextField a, JTextField b)
	{
		t.setEditable(true);
		a.setEditable(false);
		b.setEditable(false);
	}

	/**
	 * Performs the actual transfer, when the go button is pressed.
	 */
	private static void doJob()
	{
		go.setEnabled(false); // disable button until we're done.

		// Determine what we've been asked to transfer, and collect filenames.
		String[] tl;
		if (cat_b.isSelected())
			tl = enwp.getCategoryMembers(cat_t.getText().trim(), "File");
		else if (user_b.isSelected())
			tl = enwp.getUserUploads(Namespace.nss(user_t.getText().trim()));
		else
		{
			String temp = "File:" + Namespace.nss(single_t.getText().trim());
			if (!enwp.exists(temp) || temp.equals("File:"))
			{
				JOptionPane.showMessageDialog(null, String.format("'%s'does not exist on en.wp", temp));
				go.setEnabled(true);
				return;
			}
			tl = new String[] { temp };
		}

		// Do the transfer, with hooks to progress bar.
		ArrayList<String> fails = new ArrayList<String>();
		pb.setValue(0);
		pb.setMaximum(tl.length);
		int i = 0;
		for (String s : tl)
		{
			pb.setString(String.format("Transferring %s (%d/%d)", s, i, tl.length));
			if (!new TransferItem(s).doJob(com))
				fails.add(s);
			pb.setValue(++i);
		}

		// reset GUI and note anything we didn't transfer.
		pb.setString("Done!");
		go.setEnabled(true);
		if (fails.size() > 0)
			JOptionPane.showMessageDialog(null, "Did not upload:\n" + FString.fenceMaker("\n", fails.toArray(new String[0])));
	}
}