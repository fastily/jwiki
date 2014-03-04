package fbot.lib.util;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import fbot.lib.core.W;

/**
 * Static GUI factories to make building tools easier.
 * 
 * @author Fastily
 * 
 */
public class FGUI
{
	/**
	 * Hiding from javadoc
	 */
	private FGUI()
	{
		
	}
	
	/**
	 * Creates a form in the form of a JPanel. Fields are dynamically resized when the window size is modified by the
	 * user.
	 * 
	 * @param title Title to use in the border. Specify null if you don't want one. Specify empty string if you want
	 *            just border.
	 * @param cl The list of containers to work with. Elements should be in the order, e.g. JLabel1, JTextField1, JLabel
	 *            2, JTextField2, etc.
	 * 
	 * @return A JPanel with a SpringLayout in a form.
	 * @throws UnsupportedOperationException If cl.length == 0 || cl.length % 2 == 1
	 */
	public static JPanel buildForm(String title, JComponent... cl)
	{
		JPanel pl = new JPanel(new GridBagLayout());
		
		// Sanity check. There must be at least two elements in cl
		if (cl.length == 0 || cl.length % 2 == 1)
			throw new UnsupportedOperationException("Either cl is empty or has an odd number of elements!");
		
		if (title != null)
			pl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		for (int i = 0; i < cl.length; i += 2)
		{
			c.gridx = 0;
			c.gridy = i;
			c.anchor = GridBagConstraints.EAST; // should anchor East
			pl.add(cl[i], c);
			
			c.anchor = GridBagConstraints.CENTER; // reset anchor to default
			
			c.weightx = 0.5; // Fill weights
			c.gridx = 1;
			c.gridy = i;
			c.ipady = 5; // sometimes components render funky when there is no extra vertical buffer
			pl.add(cl[i + 1], c);
			
			// reset default values for next iteration
			c.weightx = 0;
			c.ipady = 0;
		}
		
		return pl;
	}
	
	/**
	 * Display a login dialog for the user. Allows for 3 bad logins before exiting program.
	 * 
	 * @param domain The domain (in shorthand) to use.
	 * @return The resulting wiki object.
	 */
	public static W login(String domain)
	{
		
		JTextField tf = new JTextField(12);
		JPasswordField pf = new JPasswordField(12);
		
		for (int i = 0; i < 3; i++)
		{
			if (JOptionPane.showConfirmDialog(null, buildForm("Login", new JLabel("User: "), tf, new JLabel("Password: "), pf),
					"Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
				System.exit(0);
			
			W wiki = new W(tf.getText().trim(), new String(pf.getPassword()), domain);
			if (wiki.isVerified(domain))
				return wiki;
			
			JOptionPane.showConfirmDialog(null, "User/Password not recognized. Try again?");
		}
		
		JOptionPane.showConfirmDialog(null, "Failed login 3 times.  Program exiting");
		System.exit(0);
		return null; // dead code to shut up compiler
	}
	
	/**
	 * Display a login dialog for the user. Allows for 3 bad logins before exiting program. Auto-set to log us into
	 * Wikimedia Commons.
	 * 
	 * @return The resulting login object.
	 */
	public static W login()
	{
		return login("commons.wikimedia.org");
	}
	
	/**
	 * Creates a simple JFrame with the given settings.
	 * @param title The title of the JFrame.
	 * @param exitmode The exit mode (e.g. JFrame.EXIT_ON_CLOSE)
	 * @param resizable Should the window be resizable?
	 * @return The specified JFrame
	 */
	public static JFrame simpleJFrame(String title, int exitmode, boolean resizable)
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame f = new JFrame(title);
		f.setDefaultCloseOperation(exitmode);
		f.setResizable(resizable);

		return f;
	}
	
	/**
	 * Sets a JFrame to be visible, packs it, and centers it.
	 * @param f The frame to perform this operation on.
	 */
	public static void setJFrameVisible(JFrame f)
	{
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	/**
	 * Load Components into a JPanel using a FlowLayout.
	 * @param items The items to load
	 * @return The JPanel.
	 */
	public static JPanel simpleJPanel(Component... items)
	{
		JPanel p = new JPanel();
		for (Component c : items)
			p.add(c);
		return p;
	}
	
	/**
	 * Make a JPanel with a box layout, with the given items.
	 * @param axis The direction to go in.  See BoxLayout fields.
	 * @param items The Components to add.  Components will be added in the order passed in.
	 * @return The JPanel.
	 */
	public static JPanel boxLayout(int axis, Component... items)
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, axis));
		for (Component c : items)
			p.add(c);
		return p;
	}
	
}